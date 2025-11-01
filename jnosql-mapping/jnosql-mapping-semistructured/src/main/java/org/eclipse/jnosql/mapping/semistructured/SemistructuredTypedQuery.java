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
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.nosql.TypedQuery;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

final class SemistructuredTypedQuery<T> implements TypedQuery<T> {
    private final SemistructuredQuery semistructuredQuery;
    private final AbstractSemiStructuredTemplate template;
    private final ProjectionMetadata projectionMetadata;

    private SemistructuredTypedQuery(SemistructuredQuery semistructuredQuery,
                                     AbstractSemiStructuredTemplate template,
                                     ProjectionMetadata projectionMetadata) {
        this.semistructuredQuery = semistructuredQuery;
        this.template = template;
        this.projectionMetadata = projectionMetadata;
    }

    @Override
    public List<T> result() {
        if(isProjection()) {
            return stream().map(mapProjection()).toList();
        }
        return this.semistructuredQuery.result();
    }

    @Override
    public Stream<T> stream() {
        if(isProjection()) {
            return this.semistructuredQuery.stream().map(mapProjection());
        }
        return this.semistructuredQuery.stream();
    }

    @Override
    public Optional<T> singleResult() {
        if(isProjection()) {
            return this.semistructuredQuery.singleResult().map(mapProjection());
        }

        return this.semistructuredQuery.singleResult();
    }

    @Override
    public void executeUpdate() {
        this.semistructuredQuery.executeUpdate();
    }

    @Override
    public TypedQuery<T> bind(String name, Object value) {
        this.semistructuredQuery.bind(name, value);
        return this;
    }

    @Override
    public TypedQuery<T> bind(int position, Object value) {
        this.semistructuredQuery.bind(position, value);
        return this;
    }

    private boolean isProjection() {
        return this.projectionMetadata != null;
    }

    private Function<Object, T> mapProjection() {
        var projectorConverter = this.template.converter().projectorConverter();
        return e -> projectorConverter.map(e, projectionMetadata);
    }

    static <T> TypedQuery<T> of(String query,
                                PreparedStatement preparedStatement,
                                AbstractSemiStructuredTemplate template,
                                ProjectionMetadata projectionMetadata) {
        var semistructuredQuery = SemistructuredQuery.of(query, preparedStatement);
        return new SemistructuredTypedQuery<>(semistructuredQuery, template, projectionMetadata);
    }
}
