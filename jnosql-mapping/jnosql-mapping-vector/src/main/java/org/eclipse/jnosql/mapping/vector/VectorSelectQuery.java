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

import org.eclipse.jnosql.communication.semistructured.SelectQuery;

import java.util.Optional;

/**
 * Represents a vector search query.
 *
 * <p>
 * A vector query extends the regular semi-structured {@link SelectQuery} with
 * the vector used as the search reference and optional vector-specific search
 * parameters.
 * </p>
 */
public interface VectorSelectQuery extends SelectQuery {

    /**
     * Returns the vector used as the search reference.
     *
     * @return the query vector
     */
    Vector vector();

    /**
     * Returns the similarity or distance threshold.
     *
     * @return the threshold, or {@link Optional#empty()} when no threshold is defined
     */
    Optional<Float> threshold();
}