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


import jakarta.data.exceptions.NonUniqueResultException;
import jakarta.nosql.Query;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.query.ParamQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.SelectQuery;
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
import java.util.stream.Stream;

final class KeyValueQuery implements Query {

    private final String query;

    private final AbstractKeyValueTemplate template;

    private final QueryType type;

    private final EntityMetadata entityMetadata;
    private final FieldMetadata id;

    private final QueryCondition condition;

    private final KeyValueParameterState parameterState;


    private KeyValueQuery(String query,
                          AbstractKeyValueTemplate template,
                          QueryType type,
                          FieldMetadata id,
                          QueryCondition condition,
                          EntityMetadata entityMetadata,
                          KeyValueParameterState parameterState) {
        this.query = query;
        this.template = template;
        this.type = type;
        this.id = id;
        this.condition = condition;
        this.entityMetadata = entityMetadata;
        this.parameterState = parameterState;
    }

    @Override
    public void executeUpdate() {
        checkParamsLeft();
        if(QueryType.SELECT.equals(type)) {
            throw new UnsupportedOperationException("the executeUpdate does not support the SELECT query, the query is: " + query);
        }
        parameterState.values().forEach(value -> {
            Object keyValueConverted = value.get();
            template.deleteByKey(keyValueConverted);
        });
    }

    @Override
    public <T> List<T> result() {
        checkParamsLeft();
        verifyIsNotDeleteType();
        if(Condition.EQUALS.equals(condition.condition())){
            Optional<T> optional = equals();
            return optional.map(List::of).orElseGet(List::of);
        }
        return in();
    }


    @Override
    public <T> Stream<T> stream() {
        checkParamsLeft();
        verifyIsNotDeleteType();
        if(Condition.EQUALS.equals(condition.condition())){
            Optional<T> optional = equals();
            return optional.stream();
        }
        List<T> entities = in();
        return entities.stream();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> singleResult() {
        checkParamsLeft();
        verifyIsNotDeleteType();

        if(Condition.EQUALS.equals(condition.condition())){
            return equals();
        } else {
            List<T> entities = in();
            if(entities.size() == 1) {
                return Optional.of(entities.getFirst());
            } else if(entities.isEmpty()) {
                return Optional.empty();
            }
            throw new NonUniqueResultException("Expected one result but found: " + entities.size());
        }
    }

    private void verifyIsNotDeleteType() {
        if(QueryType.DELETE.equals(type)) {
            throw new UnsupportedOperationException("The delete query does not support the singleResult method, the query: " + query);
        }
    }

    @Override
    public Query bind(String name, Object value) {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(value, "value is required");

        updateBind(name, value);
        return this;
    }

    @Override
    public Query bind(int position, Object value) {
        Objects.requireNonNull(value, "value is required");

        if(position < 1) {
            throw new IllegalArgumentException("The index should be greater than zero");
        }

        var name = "?" + position;
        updateBind("?" + position, value);
        return this;
    }

    private void checkParamsLeft(){
        if (!parameterState.paramsLeft.isEmpty()) {
            throw new QueryException("Check all the parameters before execute the query, params left: " + parameterState.paramsLeft);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> equals() {
        var keyValueConverted = parameterState.values().getFirst().get();
        return template.find((Class<T>) entityMetadata.type(), keyValueConverted);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> in() {
        List<T> entities = new ArrayList<>();
        parameterState.values().forEach(keyValueConverted -> {
            Optional<T> optional = template.find((Class<T>) entityMetadata.type(), keyValueConverted.get());
            optional.ifPresent(entities::add);
        });
        return entities;
    }

    private void updateBind(String name, Object value) {
        parameterState.paramsLeft.remove(name);
        parameterState.params.bind(name, ConverterUtil.getValue(value, template.getConverter().getConverters(), id));
    }

    static KeyValueQuery of(String query, AbstractKeyValueTemplate template, QueryType type) {
        var entities = template.getConverter().getEntities();

        var entityName = getEntityName(query, type);
        var entityMetadata = entities.findByName(entityName);
        var id = entityMetadata.id()
                .orElseThrow(() -> new UnsupportedOperationException(
                        "The entity does not have an attribute annotated with jakarta.nosql.Id"));

        var condition = extractCondition(query, type)
                .orElseThrow(() -> new UnsupportedOperationException("Missing WHERE clause in query: " + query));

        validateCondition(condition, id, entityName, query);

        var params = params(condition, template, id);

        return new KeyValueQuery(query, template, type, id, condition, entityMetadata, params);
    }

    private static String getEntityName(String query, QueryType type) {
        return switch (type) {
            case SELECT -> selectQuery(query).entity();
            case DELETE -> DeleteProvider.INSTANCE.apply(query).entity();
            default -> throw new UnsupportedOperationException(
                    "Unsupported query type for key-value databases: " + type);
        };
    }
    private static Optional<QueryCondition> extractCondition(String query, QueryType type) {
        return switch (type) {
            case SELECT -> selectQuery(query).where().map(Where::condition);
            case DELETE -> DeleteProvider.INSTANCE.apply(query).where().map(Where::condition);
            default -> Optional.empty();
        };
    }

    private static void validateCondition(QueryCondition condition, FieldMetadata id,
                                          String entityName, String query) {

        if (!(Condition.EQUALS.equals(condition.condition()) || Condition.IN.equals(condition.condition()))) {
            throw new UnsupportedOperationException(
                    "Only EQUALS or IN conditions are supported for key-value databases: " + query);
        }

        if (!id.fieldName().equals(condition.name())) {
            throw new UnsupportedOperationException(
                    "Key-value queries only support the ID attribute: " + id.name() +
                            " in entity: " + entityName);
        }
    }

    @SuppressWarnings("rawtypes")
    private static KeyValueParameterState params(QueryCondition condition, AbstractKeyValueTemplate template, FieldMetadata id) {
        Params params = Params.newParams();
        List<Value> values = new ArrayList<>();
        List<String> paramsLeft = new ArrayList<>();
        if(Condition.IN.equals(condition.condition())) {
            QueryValue<?> queryValue = condition.value();
            for (QueryValue item : (QueryValue[]) queryValue.get()) {
                extractItem(template, id, item, values, params, paramsLeft);
            }

        } else if(Condition.EQUALS.equals(condition.condition())) {
            extractItem(template, id, condition.value(), values, params, paramsLeft);
        }
        return new KeyValueParameterState(params, values, paramsLeft);
    }

    private static void extractItem(AbstractKeyValueTemplate template, FieldMetadata id, QueryValue item, List<Value> values, Params params, List<String> paramsLeft) {
        if(item instanceof ParamQueryValue paramQueryValue){
            String paramName = paramQueryValue.get();
            values.add(params.add(paramName));
            paramsLeft.add(paramName);
        } else {
            Object keyValueConverted = ConverterUtil.getValue(item.get(), template.getConverter().getConverters(), id);
            values.add(Value.of(keyValueConverted));
        }
    }

    private static SelectQuery selectQuery(String query) {
        var selectQuery = SelectProvider.INSTANCE.apply(query, null);
        if(selectQuery.isCount()){
            throw new UnsupportedOperationException("the count method is not supported on key-value databases");
        }
        if(selectQuery.where().isEmpty()){
            throw new UnsupportedOperationException("the query must have a where condition on key-value databases");
        }
        if(!selectQuery.orderBy().isEmpty()){
            throw new UnsupportedOperationException("the orderBy method is not supported on key-value databases");
        }
        return selectQuery;
    }

    record KeyValueParameterState(Params params, List<Value> values, List<String> paramsLeft){}
}
