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

/**
 * Maps vector entities and integrates templates and repositories with CDI.
 * Entities retain Jakarta NoSQL's existing identifier and column mapping, with one
 * persisted vector and remaining columns as payload. The shared semi-structured
 * entity converter preserves vector values for provider-native conversion.
 * <p>
 * The default database and named providers are selected through {@code @Database}.
 * Standard persistence reuses the existing semi-structured manager. Vector searches are
 * delegated to the configured provider; unsupported representations or capabilities may
 * result in {@link java.lang.UnsupportedOperationException}.
 * </p>
 */
package org.eclipse.jnosql.mapping.vector;
