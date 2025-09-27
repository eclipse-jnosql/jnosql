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


import jakarta.data.Sort;
import org.eclipse.jnosql.communication.semistructured.SelectQuery.SelectElements;
import org.eclipse.jnosql.communication.semistructured.SelectQuery.SelectFrom;
import org.eclipse.jnosql.communication.semistructured.SelectQuery.SelectLimit;
import org.eclipse.jnosql.communication.semistructured.SelectQuery.SelectNameCondition;
import org.eclipse.jnosql.communication.semistructured.SelectQuery.SelectNameOrder;
import org.eclipse.jnosql.communication.semistructured.SelectQuery.SelectNotCondition;
import org.eclipse.jnosql.communication.semistructured.SelectQuery.SelectOrder;
import org.eclipse.jnosql.communication.semistructured.SelectQuery.SelectQueryBuild;
import org.eclipse.jnosql.communication.semistructured.SelectQuery.SelectSkip;
import org.eclipse.jnosql.communication.semistructured.SelectQuery.SelectWhere;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * The default implementation of the Select in the column
 */
class DefaultFluentSelectQueryBuilderElements extends BaseQueryBuilder implements
        SelectElements, SelectFrom, SelectLimit, SelectSkip,
        SelectOrder, SelectNameCondition, SelectNotCondition, SelectNameOrder, SelectWhere, SelectQueryBuild {


    private String entity;

    private long skip;

    private long limit;

    private final List<Sort<?>> sorts = new ArrayList<>();

    private final List<String> columns;


    DefaultFluentSelectQueryBuilderElements(List<String> columns) {
        super();
        this.columns = columns;
    }


    @Override
    public SelectFrom from(String entity) {
        requireNonNull(entity, "entity is required");
        this.entity = entity;
        return this;
    }


    @Override
    public SelectNameCondition where(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        return this;
    }

    @Override
    public SelectNameCondition and(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.and = true;
        return this;
    }

    @Override
    public SelectNameCondition or(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.and = false;
        return this;
    }

    @Override
    public SelectSkip skip(long skip) {
        if (skip < 0) {
            throw new IllegalArgumentException("The skip should not be negative, skip: " + skip);
        }
        this.skip = skip;
        return this;
    }

    @Override
    public SelectLimit limit(long limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("The limit should not be negative, limit: " + limit);
        }
        this.limit = limit;
        return this;
    }

    @Override
    public SelectOrder orderBy(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        return this;
    }


    @Override
    public SelectNotCondition not() {
        this.negate = true;
        return this;
    }

    @Override
    public <T> SelectWhere eq(T value) {
        eqImpl(value);
        return this;
    }

    @Override
    public SelectWhere like(String value) {
        likeImpl(value);
        return this;
    }

    @Override
    public <T> SelectWhere gt(T value) {
        gtImpl(value);
        return this;
    }

    @Override
    public <T> SelectWhere gte(T value) {
        gteImpl(value);
        return this;
    }

    @Override
    public <T> SelectWhere lt(T value) {
        ltImpl(value);
        return this;
    }


    @Override
    public <T> SelectWhere lte(T value) {
        lteImpl(value);
        return this;
    }

    @Override
    public <T> SelectWhere between(T valueA, T valueB) {
        betweenImpl(valueA, valueB);
        return this;
    }


    @Override
    public <T> SelectWhere in(Iterable<T> values) {
        inImpl(values);
        return this;
    }


    @Override
    public SelectNameOrder asc() {
        this.sorts.add(Sort.asc(name));
        return this;
    }

    @Override
    public SelectNameOrder desc() {
        this.sorts.add(Sort.desc(name));
        return this;
    }


    @Override
    public SelectQuery build() {
        return new DefaultSelectQuery(limit, skip, entity, columns, sorts, condition, false);
    }

    @Override
    public Stream<CommunicationEntity> getResult(DatabaseManager manager) {
        requireNonNull(manager, "manager is required");
        return manager.select(this.build());
    }

    @Override
    public Optional<CommunicationEntity> getSingleResult(DatabaseManager manager) {
        requireNonNull(manager, "manager is required");
        return manager.singleResult(this.build());
    }

}
