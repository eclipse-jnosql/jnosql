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
package org.eclipse.jnosql.mapping.keyvalue;

import jakarta.nosql.Query;
import jakarta.nosql.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

final class KeyValueTypedQuery<T> implements TypedQuery<T> {

    private final KeyValueQuery query;

    KeyValueTypedQuery(KeyValueQuery query) {
        this.query = query;
    }

    @Override
    public List<T> result() {
        return query.result();
    }

    @Override
    public Stream<T> stream() {
        return query.stream();
    }

    @Override
    public Optional<T> singleResult() {
        return query.singleResult();
    }

    @Override
    public void executeUpdate() {
        query.executeUpdate();
    }

    @Override
    public Query bind(String name, Object value) {
      this.query.bind(name, value);
      return this;
    }

    @Override
    public Query bind(int position, Object value) {
        this.query.bind(position, value);
        return this;
    }
}
