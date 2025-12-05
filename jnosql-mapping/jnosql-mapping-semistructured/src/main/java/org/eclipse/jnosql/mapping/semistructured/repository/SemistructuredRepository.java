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
package org.eclipse.jnosql.mapping.semistructured.repository;

import jakarta.data.Direction;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.semistructured.MappingQuery;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Repository implementation specialized for semi-structured databases using
 * the {@link SemiStructuredTemplate}.
 * <p>This implementation supports the core persistence operations delegated
 * to a {@link SemiStructuredTemplate}. Query derivation, projections, and
 * other advanced Jakarta Data features may not be available unless
 * explicitly supported by the underlying engine. Unsupported operations are
 * reported using a descriptive error message indicating that the
 * semi-structured type does not provide the requested method.</p>
 *
 * <p>Instances of this repository are created through the
 * {@link #of(SemiStructuredTemplate, EntityMetadata)} factory method, which
 * validates all required components before construction.</p>
 *
 * @param <T> the entity type managed by this repository
 * @param <K> the type of the entity's identifier
 */
public class SemistructuredRepository<T, K>  extends AbstractRepository<T, K> {

    private final SemiStructuredTemplate repository;

    private final EntityMetadata metadata;

    private SemistructuredRepository(EntityMetadata metadata, SemiStructuredTemplate repository) {
        this.repository = repository;
        this.metadata = metadata;
    }

    @Override
    protected SemiStructuredTemplate template() {
        return repository;
    }

    @Override
    protected EntityMetadata entityMetadata() {
        return metadata;
    }

    @Override
    protected String getErrorMessage() {
        return "The Semistructured type does not support %s method";
    }

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


    /**
     * Creates a new {@link SemistructuredRepository} instance backed by the
     * given {@link SemiStructuredTemplate} and associated {@link EntityMetadata}.
     * This factory method ensures that all required components are provided
     * and performs null validation before constructing the repository.
     *
     * <p>The returned repository provides CRUD operations for the specified
     * entity type using the semi-structured mapping model. Additional Jakarta
     * Data features may depend on the capabilities of the underlying
     * {@link SemiStructuredTemplate} implementation.</p>
     *
     * @param template  the semi-structured template used to execute persistence operations
     * @param metadata  metadata describing the entity managed by the repository
     * @param <T>       the entity type
     * @param <K>       the identifier type
     * @return a new {@code SemistructuredRepository} instance configured with the given template and metadata
     * @throws NullPointerException if {@code template} or {@code metadata} is {@code null}
     */
    public static <T, K> SemistructuredRepository<T, K> of(SemiStructuredTemplate template, EntityMetadata metadata) {
        Objects.requireNonNull(template, "template is required");
        Objects.requireNonNull(metadata, "metadata is required");
        return new SemistructuredRepository<>(metadata, template);
    }
}
