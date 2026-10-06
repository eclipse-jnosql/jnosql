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

/**
 * Identifies a vector value independently of its numerical representation.
 * <p>
 * A vector entity uses the existing {@link jakarta.nosql.Entity}, {@link jakarta.nosql.Id},
 * and {@link jakarta.nosql.Column} annotations. Its single persisted vector is identified by
 * this type hierarchy, not by a particular property name or by a raw Java array.
 * Other persisted columns represent payload.
 * </p>
 * <p>
 * The initial supported representation is {@link DenseVector}. Vector generation and
 * conversion to database-native representations belong to applications and providers,
 * respectively.
 * </p>
 */
public interface Vector {
}
