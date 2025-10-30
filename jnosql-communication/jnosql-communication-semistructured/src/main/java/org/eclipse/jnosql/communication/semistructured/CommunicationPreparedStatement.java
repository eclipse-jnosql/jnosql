/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;


import jakarta.data.exceptions.NonUniqueResultException;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.QueryException;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Represents a precompiled query statement.
 */
public final class CommunicationPreparedStatement {

    private static final UnaryOperator<SelectQuery> SELECT_MAPPER_DEFAULT = s -> s;

    private final SelectQuery selectQuery;

    private final DeleteQuery deleteQuery;

    private final UpdateQuery updateQuery;

    private final PreparedStatementType type;

    private final Params params;

    private final String query;

    private final List<String> paramsLeft;

    private final DatabaseManager manager;

    private UnaryOperator<SelectQuery> selectMapper = SELECT_MAPPER_DEFAULT;

    private CommunicationPreparedStatement(SelectQuery selectQuery,
                                           DeleteQuery deleteQuery,
                                           UpdateQuery updateQuery,
                                           PreparedStatementType type,
                                           Params params,
                                           String query,
                                           List<String> paramsLeft,
                                           DatabaseManager manager) {
        this.selectQuery = selectQuery;
        this.deleteQuery = deleteQuery;
        this.updateQuery = updateQuery;
        this.type = type;
        this.params = params;
        this.query = query;
        this.paramsLeft = paramsLeft;
        this.manager = manager;
    }

    /**
     * Binds an argument to a positional parameter.
     *
     * @param name  the parameter name
     * @param value the parameter value
     * @return the same query instance
     * @throws NullPointerException when either name or value is null
     */
    public CommunicationPreparedStatement bind(String name, Object value) {
        Objects.requireNonNull(name, "name is required");
        Objects.requireNonNull(value, "value is required");

        paramsLeft.remove(name);
        params.bind(name, value);
        return this;
    }

    /**
     * Binds an argument to a positional parameter.
     *
     * @param index the parameter index
     * @param value the parameter value
     * @return the same query instance
     * @throws NullPointerException when either name or value is null
     * The first parameter is 1, the second is 2, ...
     */
    public CommunicationPreparedStatement bind(int index, Object value) {
        Objects.requireNonNull(value, "value is required");

        if(index < 1) {
            throw new IllegalArgumentException("The index should be greater than zero");
        }

        var name = "?" + index;
        paramsLeft.remove("?" + index);
        params.bind(name, value);
        return this;
    }

    public PreparedStatementType getType() {
        return type;
    }

    /**
     * Returns the select query if present.
     *
     * @return the select query
     */
    public Optional<SelectQuery> select() {
        return Optional.ofNullable(selectQuery);
    }

    /**
     * Returns the result as a stream of entities.
     *
     * @return the stream of entities
     * @throws QueryException if there are parameters left to bind
     */
    public Stream<CommunicationEntity> result() {
        if (!paramsLeft.isEmpty()) {
            throw new QueryException("Check all the parameters before execute the query, params left: " + paramsLeft);
        }
        switch (type) {
            case SELECT -> {
                return manager.select(operator().apply(selectQuery));
            }
            case DELETE -> {
                manager.delete(deleteQuery);
                return Stream.empty();
            }
            case UPDATE -> {
                return StreamSupport.stream(manager.update(updateQuery).spliterator(), false);
            }
            default -> throw new UnsupportedOperationException("there is not support to operation type: " + type);
        }
    }

    /**
     * Returns the operator to be used in the query.
     *
     * @return the operator
     */
    public UnaryOperator<SelectQuery> operator() {
        return this.selectMapper;
    }

    /**
     * Sets the operator to be used in the query.
     *
     * @param selectMapper the operator
     */
    public void setSelectMapper(UnaryOperator<SelectQuery> selectMapper) {
        Objects.requireNonNull(selectMapper, "selectMapper is required");
        this.selectMapper = selectMapper;
    }

    /**
     * Returns the number of elements in the result.
     *
     * @return the number of elements
     * @throws QueryException if there are parameters left to bind
     * @throws IllegalArgumentException if the operation is not a count operation
     */
    public long count(){
        if (!paramsLeft.isEmpty()) {
            throw new QueryException("Check all the parameters before execute the query, params left: " + paramsLeft);
        }
        if (PreparedStatementType.COUNT.equals(type)) {
            return manager.count(selectQuery);
        }
        throw new IllegalArgumentException("The count operation is only allowed for COUNT queries");

    }



    /**
     * Returns the single result as an optional entity.
     *
     * @return the optional entity
     * @throws NonUniqueResultException if the result contains more than one entity
     */
    public Optional<CommunicationEntity> singleResult() {
        Stream<CommunicationEntity> entities = result();
        final Iterator<CommunicationEntity> iterator = entities.iterator();

        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        final CommunicationEntity next = iterator.next();
        if (!iterator.hasNext()) {
            return Optional.of(next);
        }

        throw new NonUniqueResultException("The select returns more than one entity, select: " + query);
    }

    public enum PreparedStatementType {
        SELECT, DELETE, UPDATE, COUNT
    }


    @Override
    public String toString() {
        return query;
    }

    static CommunicationPreparedStatement select(
            SelectQuery selectQuery,
            Params params,
            String query,
            DatabaseManager manager) {
        if (selectQuery.isCount()) {
            return new CommunicationPreparedStatement(selectQuery,
                    null, null, PreparedStatementType.COUNT, params, query,
                    params.getParametersNames(), manager);
        } else {
            return new CommunicationPreparedStatement(selectQuery,
                    null, null, PreparedStatementType.SELECT, params, query,
                    params.getParametersNames(), manager);
        }

    }

    static CommunicationPreparedStatement delete(DeleteQuery deleteQuery,
                                                 Params params,
                                                 String query,
                                                 DatabaseManager manager) {

        return new CommunicationPreparedStatement(null,
                deleteQuery, null, PreparedStatementType.DELETE, params,
                query,
                params.getParametersNames(),
                manager);

    }

    static CommunicationPreparedStatement update(UpdateQuery updateQuery,
                                                 Params params,
                                                 String query,
                                                 DatabaseManager manager) {
        return new CommunicationPreparedStatement(null, null,
                updateQuery,
                PreparedStatementType.UPDATE, params, query,
                params.getParametersNames(),  manager);

    }
}
