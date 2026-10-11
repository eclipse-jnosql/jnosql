/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.vector;

import jakarta.data.Limit;
import jakarta.data.Sort;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

record DefaultVectorSelectQuery(
        String name,
        CriteriaCondition conditionValue,
        Limit limitValue,
        Vector vector,
        Float thresholdValue) implements VectorSelectQuery {

    DefaultVectorSelectQuery {
        requireNonNull(name, "name is required");
        requireNonNull(limitValue, "limit is required");
        requireNonNull(vector, "vector is required");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }

        if (thresholdValue != null && !Float.isFinite(thresholdValue)) {
            throw new IllegalArgumentException("threshold must be finite");
        }
    }

    @Override
    public long limit() {
        return limitValue.maxResults();
    }

    @Override
    public long skip() {
        return limitValue.startAt() - 1;
    }

    @Override
    public Optional<CriteriaCondition> condition() {
        return Optional.ofNullable(conditionValue);
    }

    @Override
    public List<String> columns() {
        return List.of();
    }

    @Override
    public List<Sort<?>> sorts() {
        return List.of();
    }

    @Override
    public Optional<Float> threshold() {
        return Optional.ofNullable(thresholdValue);
    }
}