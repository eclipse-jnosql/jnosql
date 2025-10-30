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

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

final class SemistructuredQuery implements Query {

    private static final Logger LOGGER = Logger.getLogger(SemistructuredQuery.class.getName());

    private final String query;

    private final PreparedStatement preparedStatement;

    SemistructuredQuery(String query, PreparedStatement preparedStatement) {
        this.query = query;
        this.preparedStatement = preparedStatement;
    }

    @Override
    public void executeUpdate() {
        long count = this.preparedStatement.result().count();
        LOGGER.fine(() -> "The query " + query + " has been executed with " + count + " results");
    }

    @Override
    public <T> List<T> result() {
        Stream<T> entities = this.preparedStatement.result();
        return entities.toList();
    }

    @Override
    public <T> Stream<T> stream() {
        return this.preparedStatement.result();
    }

    @Override
    public <T> Optional<T> singleResult() {
        return this.preparedStatement.singleResult();
    }

    @Override
    public Query bind(String name, Object value) {
        this.preparedStatement.bind(name, value);
        return this;
    }

    @Override
    public Query bind(int position, Object value) {
        this.preparedStatement.bind(position, value);
        return this;
    }
}
