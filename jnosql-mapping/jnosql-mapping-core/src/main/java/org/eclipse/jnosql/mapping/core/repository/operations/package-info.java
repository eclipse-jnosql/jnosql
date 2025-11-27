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
 * Provides the default core implementations for the semantic repository
 * operations defined by the Jakarta Data integration layer. Each class in
 * this package implements a specific repository operation, such as
 * {@code findBy}, {@code findAll}, {@code countBy}, annotated queries, or
 * cursor-based pagination.
 *
 * <p>
 * These implementations serve as the baseline execution engine used by the
 * repository proxy. Storage providers or specialized engines may override
 * any of these components using CDI alternatives. Operations that are not
 * supported by the core engine provide implementations that consistently
 * throw {@link java.lang.UnsupportedOperationException}.
 * </p>
 */
package org.eclipse.jnosql.mapping.core.repository.operations;