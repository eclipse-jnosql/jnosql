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
 * Defines vector values and the vector mapping template contract.
 * Entities retain Jakarta NoSQL's existing identifier and column mapping, with one
 * persisted vector and remaining columns as payload. The shared semi-structured
 * entity converter preserves vector values for provider-native conversion.
 * <p>
 * This initial API module does not supply a database manager, a concrete template,
 * or vector-specific CDI repository registration. Those require provider integration.
 * </p>
 */
package org.eclipse.jnosql.mapping.vector;
