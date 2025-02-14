/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.data.Direction;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;

import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.MappingQuery;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The {@link org.eclipse.jnosql.mapping.NoSQLRepository} template method
 */
public abstract class AbstractSemiStructuredRepository<T, K> extends AbstractRepository<T, K> {

    @Override
    protected abstract SemiStructuredTemplate template();

    @Override
    public long countBy() {
        return template().count(type());
    }


    @Override
    public Page<T> findAll(PageRequest pageRequest, Order<T> order) {
        Objects.requireNonNull(pageRequest, "pageRequest is required");
        EntityMetadata metadata = entityMetadata();
        List<Sort<?>> sorts = new ArrayList<>();
        order.forEach(sort -> {
            Sort<?> sortQuery = Sort.of(metadata.columnField(sort.property()), sort.isAscending() ? Direction.ASC : Direction.DESC, false);
            sorts.add(sortQuery);
        });
        SelectQuery query = new MappingQuery(sorts,
                pageRequest.size(), NoSQLPage.skip(pageRequest)
                , null ,metadata.name(), List.of());

        List<T> entities = template().<T>select(query).toList();
        return NoSQLPage.of(entities, pageRequest);
    }

    @Override
    public Stream<T> findAll() {
        return template().findAll(type());
    }

    @Override
    public void deleteAll() {
        template().deleteAll(type());
    }

}
