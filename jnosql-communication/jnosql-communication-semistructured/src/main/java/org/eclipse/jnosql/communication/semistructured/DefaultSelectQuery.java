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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.ofNullable;

/**
 * The default implementation of column query.
 */
public record DefaultSelectQuery(long limit, long skip, String name,
                          List<String> columns, List<Sort<?>> sorts, CriteriaCondition criteriaCondition, boolean count)
        implements SelectQuery {


    @Override
    public Optional<CriteriaCondition> condition() {
        return ofNullable(criteriaCondition).map(CriteriaCondition::readOnly);
    }

    @Override
    public List<String> columns() {
        return unmodifiableList(columns);
    }

    @Override
    public List<Sort<?>> sorts() {
        return unmodifiableList(sorts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SelectQuery that)) {
            return false;
        }
        return limit == that.limit() &&
                skip == that.skip() &&
                Objects.equals(name, that.name()) &&
                Objects.equals(columns, that.columns()) &&
                Objects.equals(sorts, that.sorts()) &&
                Objects.equals(criteriaCondition, that.condition().orElse(null));
    }

    @Override
    public boolean isCount() {
        return count;
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, skip, name, columns, sorts, criteriaCondition);
    }


    static SelectQuery countBy(SelectQuery query) {
        return new DefaultSelectQuery(0, 0, query.name(), query.columns(),
                Collections.emptyList(), query.condition().orElse(null), true);
    }

    static SelectQuery existsBy(SelectQuery query) {
        return new DefaultSelectQuery(1, 0, query.name(), query.columns(),
                Collections.emptyList(), query.condition().orElse(null), false);
    }
}
