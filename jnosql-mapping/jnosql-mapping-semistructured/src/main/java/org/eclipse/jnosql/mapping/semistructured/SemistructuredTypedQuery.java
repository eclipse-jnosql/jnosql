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

import jakarta.nosql.Query;
import jakarta.nosql.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

final class SemistructuredTypedQuery<T> implements TypedQuery<T> {

    private final Class<T> type;
    private final String query;
    private final SemistructuredQuery semistructuredQuery;

    private PreparedStatement preparedStatement;
    private SemistructuredTypedQuery(Class<T> type, String query, SemistructuredQuery semistructuredQuery, PreparedStatement preparedStatement) {
        this.type = type;
        this.query = query;
        this.semistructuredQuery = semistructuredQuery;
        this.preparedStatement = preparedStatement;
    }

    @Override
    public List<T> result() {
        return this.semistructuredQuery.result();
    }

    @Override
    public Stream<T> stream() {
        return this.semistructuredQuery.stream();
    }

    @Override
    public Optional<T> singleResult() {
        return this.semistructuredQuery.singleResult();
    }

    @Override
    public void executeUpdate() {
        this.semistructuredQuery.executeUpdate();
    }

    @Override
    public TypedQuery<T> bind(String name, Object value) {
        this.preparedStatement.bind(name, value);
        return this;
    }

    @Override
    public TypedQuery<T> bind(int position, Object value) {
        this.preparedStatement.bind(position, value);
        return this;
    }

    static <T> TypedQuery<T> of(String query, Class<T> type, PreparedStatement preparedStatement) {
        var semistructuredQuery = SemistructuredQuery.of(query, preparedStatement);
        return new SemistructuredTypedQuery<>(type, query, semistructuredQuery, preparedStatement);
    }
}
