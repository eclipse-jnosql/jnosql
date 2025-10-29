/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.keyvalue;


import jakarta.data.exceptions.MappingException;
import jakarta.nosql.Query;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.query.ParamQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.Where;
import org.eclipse.jnosql.communication.query.data.DeleteProvider;
import org.eclipse.jnosql.communication.query.data.QueryType;
import org.eclipse.jnosql.communication.query.data.SelectProvider;
import org.eclipse.jnosql.mapping.core.util.ConverterUtil;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

abstract sealed class KeyValueQuery implements Query
        permits SelectKeyValueQuery, DeleteKeyValueQuery {

    protected final String query;
    protected final AbstractKeyValueTemplate template;
    protected final QueryType type;
    protected final EntityMetadata entityMetadata;
    protected final FieldMetadata id;
    protected final QueryCondition condition;
    protected final KeyValueQueryParameters parameterState;

    protected KeyValueQuery(String query,
                            AbstractKeyValueTemplate template,
                            QueryType type,
                            FieldMetadata id,
                            QueryCondition condition,
                            EntityMetadata entityMetadata,
                            KeyValueQueryParameters parameterState) {
        this.query = query;
        this.template = template;
        this.type = type;
        this.id = id;
        this.condition = condition;
        this.entityMetadata = entityMetadata;
        this.parameterState = parameterState;
    }

    protected void checkParamsLeft() {
        if (!parameterState.paramsLeft.isEmpty()) {
            throw new QueryException("Check all the parameters before execute the query, params left: "
                    + parameterState.paramsLeft);
        }
    }

    @Override
    public Query bind(String name, Object value) {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(value, "value is required");
        parameterState.paramsLeft.remove(name);
        parameterState.params.bind(name,
                ConverterUtil.getValue(value, template.getConverter().getConverters(), id));
        return this;
    }

    @Override
    public Query bind(int position, Object value) {
        Objects.requireNonNull(value, "value is required");
        if (position < 1) {
            throw new IllegalArgumentException("The index should be greater than zero");
        }
        String name = "?" + position;
        return bind(name, value);
    }

    @SuppressWarnings("unchecked")
    protected <T> Optional<T> findEqual() {
        var keyValueConverted = parameterState.values().getFirst().get();
        return template.find((Class<T>) entityMetadata.type(), keyValueConverted);
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> findIn() {
        List<T> entities = new ArrayList<>();
        parameterState.values().forEach(keyValueConverted -> {
            Optional<T> optional = template.find((Class<T>) entityMetadata.type(),
                    keyValueConverted.get());
            optional.ifPresent(entities::add);
        });
        return entities;
    }

    static KeyValueQuery of(String query, AbstractKeyValueTemplate template, QueryType type) {
        var entities = template.getConverter().getEntities();
        var entityName = switch (type) {
            case SELECT -> SelectProvider.INSTANCE.apply(query, null).entity();
            case DELETE -> DeleteProvider.INSTANCE.apply(query).entity();
            default -> throw new UnsupportedOperationException("Unsupported query type: " + type);
        };
        var entityMetadata = entities.findByName(entityName);
        var id = entityMetadata.id()
                .orElseThrow(() -> new MappingException("The entity has no jakarta.nosql.Id attribute"));
        Optional<QueryCondition> conditionOptional;
        switch (type) {
            case SELECT -> conditionOptional = SelectProvider.INSTANCE
                    .apply(query, null)
                    .where()
                    .map(Where::condition);

            case DELETE -> conditionOptional = DeleteProvider.INSTANCE
                    .apply(query)
                    .where()
                    .map(Where::condition);

            default ->
                    throw new UnsupportedOperationException("Unsupported query type for key-value databases: " + type);
        }
        var condition = conditionOptional.orElseThrow(() -> new MappingException("Missing WHERE clause in query: " + query));
        validateCondition(condition, id, entityName, query);
        var params = params(condition, template, id);

        return switch (type) {
            case SELECT -> new SelectKeyValueQuery(query, template, type, id, condition, entityMetadata, params);
            case DELETE -> new DeleteKeyValueQuery(query, template, type, id, condition, entityMetadata, params);
            default -> throw new UnsupportedOperationException("Unsupported query type: " + type);
        };
    }

    private static void validateCondition(QueryCondition condition, FieldMetadata id,
                                          String entityName, String query) {
        if (!(Condition.EQUALS.equals(condition.condition())
                || Condition.IN.equals(condition.condition()))) {
            throw new UnsupportedOperationException(
                    "Only EQUALS or IN are supported for key-value queries: " + query);
        }
        if (!id.fieldName().equals(condition.name())) {
            throw new UnsupportedOperationException(
                    "Only ID attribute supported: " + id.name() + " in entity: " + entityName);
        }
    }

    @SuppressWarnings("rawtypes")
    private static KeyValueQueryParameters params(QueryCondition condition, AbstractKeyValueTemplate template, FieldMetadata id) {
        Params params = Params.newParams();
        List<Value> values = new ArrayList<>();
        List<String> paramsLeft = new ArrayList<>();
        if (Condition.IN.equals(condition.condition())) {
            QueryValue<?> queryValue = condition.value();
            for (QueryValue item : (QueryValue[]) queryValue.get()) {
                extractItem(template, id, item, values, params, paramsLeft);
            }

        } else if (Condition.EQUALS.equals(condition.condition())) {
            extractItem(template, id, condition.value(), values, params, paramsLeft);
        }
        return new KeyValueQueryParameters(params, values, paramsLeft);
    }

    private static void extractItem(AbstractKeyValueTemplate template, FieldMetadata id, QueryValue<?> item,
                                    List<Value> values, Params params, List<String> paramsLeft) {

        if (item instanceof ParamQueryValue paramQueryValue) {
            String paramName = paramQueryValue.get();
            values.add(params.add(paramName));
            paramsLeft.add(paramName);
        } else {
            Object keyValueConverted = ConverterUtil.getValue(item.get(), template.getConverter().getConverters(), id);
            values.add(Value.of(keyValueConverted));
        }
    }

    record KeyValueQueryParameters(Params params, List<Value> values, List<String> paramsLeft) {
    }

}
