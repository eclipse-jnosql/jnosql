/*
 *  Copyright (c) 2022 Otávio Santana and others
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
 *   Alessandro Moscatelli
 */
package org.eclipse.jnosql.communication.criteria;

import jakarta.nosql.criteria.Order;
import jakarta.nosql.criteria.SelectQuery;
import java.util.List;

public class DefaultSelectQuery<
        T extends Object,
        R extends DefaultSelectQueryResult<T>,
        Q extends DefaultSelectQuery<T, R, Q>
    > extends AbstractRestrictedQuery<T, DefaultSelectQueryResult<T>, Q> implements SelectQuery<T> {

    private List<Order<T>> sortings;
    private Integer maxResults;
    private Integer firstResult;

    public DefaultSelectQuery(Class<T> type) {
        super(type);
    }

    @Override
    public SelectQuery<T> orderBy(List<Order<T>> sortings) {
        this.sortings = sortings;
        return this;
    }

    public List<Order<T>> getSortings() {
        return sortings;
    }

    @Override
    public SelectQuery<T> setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    @Override
    public SelectQuery<T> setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

}
