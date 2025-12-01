/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 * This package is deprecated and preserved only for backward compatibility.
 * It contains the previous query execution infrastructure for semi-structured
 * databases, including legacy components used before Jakarta Data integration.
 * The new execution model is now provided in
 * {@code org.eclipse.jnosql.mapping.semistructured.repository}, which offers a
 * clearer and more consistent API aligned with Jakarta Data. Applications and
 * extensions should migrate to the new package, as this legacy structure will
 * not receive new features.
 * @deprecated migrate to {@link org.eclipse.jnosql.mapping.semistructured.repository}
 */
@Deprecated
package org.eclipse.jnosql.mapping.semistructured.query;