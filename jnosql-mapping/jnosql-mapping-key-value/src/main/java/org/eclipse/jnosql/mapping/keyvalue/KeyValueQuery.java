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


import jakarta.nosql.Query;
import org.eclipse.jnosql.communication.query.DeleteQuery;
import org.eclipse.jnosql.communication.query.SelectQuery;
import org.eclipse.jnosql.communication.query.data.DeleteProvider;
import org.eclipse.jnosql.communication.query.data.QueryType;
import org.eclipse.jnosql.communication.query.data.SelectProvider;

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


    private KeyValueQuery(String query,
                          AbstractKeyValueTemplate template,
                          QueryType type,
                          SelectQuery selectQuery,
                          DeleteQuery deleteQuery,
                          Object value,
                          String param) {
        this.query = query;
        this.template = template;
        this.type = type;
        this.selectQuery = selectQuery;
        this.deleteQuery = deleteQuery;
        this.value = value;
        this.param = param;
    }

    static KeyValueQuery of(String query, AbstractKeyValueTemplate template, QueryType type) {
        SelectQuery selectQuery = null;
        DeleteQuery deleteQuery = null;
        Object value = null;
        String param = null;
        if(QueryType.SELECT.equals(type)) {
            selectQuery = selectQuery(query);
        } else {
            deleteQuery = DeleteProvider.INSTANCE.apply(query);
        }
        return new KeyValueQuery(query, template, type, selectQuery, deleteQuery, value, param);
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

    @Override
    public void executeUpdate() {
        if(QueryType.SELECT.equals(type)) {
            throw new UnsupportedOperationException("the executeUpdate does not support the SELECT query, the query is: " + query);
        }
    }

    @Override
    public <T> List<T> result() {

        return List.of();
    }

    @Override
    public <T> Stream<T> stream() {
        return Stream.empty();
    }

    @Override
    public <T> Optional<T> singleResult() {
        return Optional.empty();
    }

    @Override
    public Query bind(String name, Object value) {
        return null;
    }

    @Override
    public Query bind(int position, Object value) {
        return null;
    }
}
