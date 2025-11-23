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

/**
 * Provides the execution engine for repository methods in semi-structured
 * databases. This package resolves repository operations based on method
 * patterns such as "findBy", "deleteBy", "countBy", and "existsBy",
 * and on annotations including {@link jakarta.data.repository.Query},
 * {@link jakarta.data.repository.Insert}, {@link jakarta.data.repository.Update},
 * {@link jakarta.data.repository.Delete}, {@link jakarta.data.repository.Save},
 * and {@link jakarta.data.repository.Find}.
 * The types in this package replace the deprecated execution logic previously
 * located in {@code org.eclipse.jnosql.mapping.semistructured.query}. New
 * applications should rely on this package as the standard repository execution
 * layer for Jakarta NoSQL.
 *
 * @since 1.2
 */
package org.eclipse.jnosql.mapping.semistructured.repository;