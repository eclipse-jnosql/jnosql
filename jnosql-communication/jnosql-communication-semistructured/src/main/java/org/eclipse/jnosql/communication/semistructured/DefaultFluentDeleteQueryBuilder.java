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


import org.eclipse.jnosql.communication.semistructured.DeleteQuery.DeleteFrom;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery.DeleteNotCondition;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery.DeleteWhere;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery.EntityDelete;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * The default implementation to Delete query
 */
class DefaultFluentDeleteQueryBuilder extends BaseQueryBuilder implements EntityDelete, DeleteFrom,
        DeleteWhere, DeleteNotCondition {

    private String entity;


    private final List<String> columns;


    DefaultFluentDeleteQueryBuilder(List<String> columns) {
        super();
        this.columns = columns;
    }

    @Override
    public DeleteFrom from(String entity) {
        requireNonNull(entity, "entity is required");
        this.entity = entity;
        return this;
    }


    @Override
    public DeleteQuery.DeleteNameCondition where(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        return this;
    }


    @Override
    public DeleteQuery.DeleteNameCondition and(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.and = true;
        return this;
    }

    @Override
    public DeleteQuery.DeleteNameCondition or(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.and = false;
        return this;
    }


    @Override
    public DeleteNotCondition not() {
        this.negate = true;
        return this;
    }

    @Override
    public <T> DeleteWhere eq(T value) {
        eqImpl(value);
        return this;
    }

    @Override
    public DeleteWhere like(String value) {
        likeImpl(value);
        return this;
    }

    @Override
    public <T> DeleteWhere gt(T value) {
        gtImpl(value);
        return this;
    }

    @Override
    public <T> DeleteWhere gte(T value) {
        gteImpl(value);
        return this;
    }

    @Override
    public <T> DeleteWhere lt(T value) {
        ltImpl(value);
        return this;
    }

    @Override
    public <T> DeleteWhere lte(T value) {
        lteImpl(value);
        return this;
    }

    @Override
    public <T> DeleteWhere between(T valueA, T valueB) {
        betweenImpl(valueA, valueB);
        return this;
    }

    @Override
    public <T> DeleteWhere in(Iterable<T> values) {
        inImpl(values);
        return this;
    }

    @Override
    public DeleteQuery build() {
        return new DefaultDeleteQuery(entity, condition, columns);
    }

    @Override
    public void delete(DatabaseManager manager) {
        requireNonNull(manager, "manager is required");
        manager.delete(this.build());
    }

}
