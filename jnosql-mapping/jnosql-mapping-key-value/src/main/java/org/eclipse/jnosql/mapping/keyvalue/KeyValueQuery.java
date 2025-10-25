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


import jakarta.nosql.MappingException;
import jakarta.nosql.Query;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.DeleteQuery;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.SelectQuery;
import org.eclipse.jnosql.communication.query.Where;
import org.eclipse.jnosql.communication.query.data.DeleteProvider;
import org.eclipse.jnosql.communication.query.data.QueryType;
import org.eclipse.jnosql.communication.query.data.SelectProvider;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.util.ConverterUtil;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

final class KeyValueQuery implements Query {

    private final String query;

    private final AbstractKeyValueTemplate template;

    private final QueryType type;

    private final SelectQuery selectQuery;

    private final DeleteQuery deleteQuery;

    private final Object value;

    private final String param;

    private final EntityMetadata entityMetadata;
    private final FieldMetadata id;

    private final QueryCondition condition;


    private KeyValueQuery(String query,
                          AbstractKeyValueTemplate template,
                          QueryType type,
                          SelectQuery selectQuery,
                          DeleteQuery deleteQuery,
                          Object value,
                          String param,
                          FieldMetadata id,
                          QueryCondition condition,
                          EntityMetadata entityMetadata) {
        this.query = query;
        this.template = template;
        this.type = type;
        this.selectQuery = selectQuery;
        this.deleteQuery = deleteQuery;
        this.value = value;
        this.param = param;
        this.id = id;
        this.condition = condition;
        this.entityMetadata = entityMetadata;
    }

    @Override
    public void executeUpdate() {
        if(QueryType.SELECT.equals(type)) {
            throw new UnsupportedOperationException("the executeUpdate does not support the SELECT query, the query is: " + query);
        }
    }

    @Override
    public <T> List<T> result() {
        if(Condition.EQUALS.equals(condition.condition())){
            Optional<T> optional = equals();
            return optional.map(List::of).orElseGet(List::of);
        }
        return List.of();
    }


    @Override
    public <T> Stream<T> stream() {
        if(Condition.EQUALS.equals(condition.condition())){
            Optional<T> optional = equals();
            return optional.map(Stream::of).orElseGet(Stream::empty);
        }
        return Stream.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> singleResult() {
        if(Condition.EQUALS.equals(condition.condition())){
            return equals();
        } else {

        }
        return Optional.empty();
    }

    private <T> Optional<T> equals() {
        QueryValue<?> queryValue = condition.value();
        Object keyValueConverted = ConverterUtil.getValue(queryValue.get(), template.getConverter().getConverters(), id);
        return template.find((Class<T>) entityMetadata.type(), keyValueConverted);
    }

    @Override
    public Query bind(String name, Object value) {
        return null;
    }

    @Override
    public Query bind(int position, Object value) {
        return null;
    }


    static KeyValueQuery of(String query, AbstractKeyValueTemplate template, QueryType type) {
        SelectQuery selectQuery = null;
        DeleteQuery deleteQuery = null;
        Object value = null;
        String param = null;
        EntitiesMetadata entities = template.getConverter().getEntities();
        FieldMetadata id = null;
        QueryCondition condition = null;
        EntityMetadata entityMetadata = null;

        if(QueryType.SELECT.equals(type)) {
            selectQuery = selectQuery(query);
            var entityName = selectQuery.entity();
            entityMetadata = entities.findByName(entityName);
            id = entityMetadata.id().orElseThrow(() -> new MappingException("The entity does not have an " +
                    "attribute with jakarta.nosql.Id annotation"));
            Where where = selectQuery.where().orElseThrow();
            condition = where.condition();
            if (!(Condition.EQUALS.equals(condition.condition())
                    || Condition.IN.equals(condition.condition()))) {
                throw new UnsupportedOperationException("the condition must be equals or in on key-value databases, " +
                        "the query is: " + query);
            }
            String name = condition.name();
            if(!id.fieldName().equals(name)) {
                throw new UnsupportedOperationException("The key-value Mapper query only support the id attribute: " + id.name() + " at the entity: " + entityName);
            }
        } else {
            deleteQuery = DeleteProvider.INSTANCE.apply(query);
        }
        return new KeyValueQuery(query, template, type, selectQuery, deleteQuery, value, param, id, condition, entityMetadata);
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
}
