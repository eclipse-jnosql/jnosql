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
package org.eclipse.jnosql.mapping.metadata.repository;

/**
 * A marker interface representing a normalized lookup key used to identify repository methods
 * within the metadata, allowing different invocation strategies—such as reflective methods,
 * precomputed signatures, or simplified test-only names—to participate in a unified resolution
 * mechanism.
 */
public sealed interface MethodKey permits MethodSignatureKey, ReflectionMethodKey, NameKey {}