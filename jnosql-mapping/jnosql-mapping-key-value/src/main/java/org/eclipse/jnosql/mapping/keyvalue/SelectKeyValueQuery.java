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

import jakarta.data.exceptions.NonUniqueResultException;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.data.QueryType;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

final class SelectKeyValueQuery extends KeyValueQuery {

    SelectKeyValueQuery(String query,
                        AbstractKeyValueTemplate template,
                        QueryType type,
                        FieldMetadata id,
                        QueryCondition condition,
                        EntityMetadata entityMetadata,
                        KeyValueParameterState parameterState) {
        super(query, template, type, id, condition, entityMetadata, parameterState);
    }

    @Override
    public void executeUpdate() {
        throw new UnsupportedOperationException("SELECT queries cannot perform updates: " + query);
    }

    @Override
    public <T> List<T> result() {
        checkParamsLeft();
        if (Condition.EQUALS.equals(condition.condition())) {
            Optional<T> entity = findEqual();
            return entity.map(List::of).orElseGet(List::of);
        }
        return findIn();
    }

    @Override
    public <T> Stream<T> stream() {
        List<T> entities = result();
        return entities.stream();
    }

    @Override
    public <T> Optional<T> singleResult() {
        checkParamsLeft();
        if (Condition.EQUALS.equals(condition.condition())) {
            return findEqual();
        }
        List<T> entities = findIn();
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        if (entities.size() == 1){
            return Optional.of(entities.getFirst());
        }
        throw new NonUniqueResultException("Expected one result but found: " + entities.size());
    }
}