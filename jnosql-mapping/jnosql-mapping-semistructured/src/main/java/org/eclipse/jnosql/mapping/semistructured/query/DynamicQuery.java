/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.data.Limit;
import jakarta.data.Sort;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.semistructured.MappingQuery;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.SpecialParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A query converter to update dynamic query to a {@link SelectQuery}
 */
public class DynamicQuery implements Supplier<SelectQuery> {

    private final SpecialParameters special;
    private final SelectQuery query;

    DynamicQuery(SpecialParameters special, SelectQuery query) {
        this.special = special;
        this.query = query;
    }

    @Override
    public SelectQuery get() {
        if (special.isEmpty()) {
            return query;
        }
        Optional<Limit> limit = special.limit();
        if (special.hasOnlySort()) {
            List<Sort<?>> sorts = new ArrayList<>();
            sorts.addAll(query.sorts());
            sorts.addAll(special.sorts());
            long skip = limit.map(l -> l.startAt() - 1).orElse(query.skip());
            long max = limit.map(Limit::maxResults).orElse((int) query.limit());
            return new MappingQuery(sorts, max,
                    skip,
                    query.condition().orElse(null),
                    query.name(), query.columns());
        }

        if (limit.isPresent()) {
            long skip = limit.map(l -> l.startAt() - 1).orElse(query.skip());
            long max = limit.map(Limit::maxResults).orElse((int) query.limit());
            List<Sort<?>> sorts = query.sorts();
            if (!special.sorts().isEmpty()) {
                sorts = new ArrayList<>(query.sorts());
                sorts.addAll(special.sorts());
            }
            return new MappingQuery(sorts, max,
                    skip,
                    query.condition().orElse(null),
                    query.name(),
                    query.columns());
        }

        return special.pageRequest().<SelectQuery>map(p -> {
            long size = p.size();
            long skip = NoSQLPage.skip(p);
            List<Sort<?>> sorts = query.sorts();
            if (!special.sorts().isEmpty()) {
                sorts = new ArrayList<>(query.sorts());
                sorts.addAll(special.sorts());
            }
            return new MappingQuery(sorts, size, skip,
                    query.condition().orElse(null), query.name(), query.columns());
        }).orElse(query);
    }

    /**
     * Creates a {@link DynamicQuery} instance
     * @param args the method parameters
     * @param query the column query
     * @return the {@link DynamicQuery} instance
     * @throws NullPointerException when either args or query are null
     */
    public static DynamicQuery of(Object[] args, SelectQuery query) {
        Objects.requireNonNull(args, "args is required");
        Objects.requireNonNull(query, "query is required");
        return new DynamicQuery(DynamicReturn.findSpecialParameters(args, Function.identity()), query);
    }
}
