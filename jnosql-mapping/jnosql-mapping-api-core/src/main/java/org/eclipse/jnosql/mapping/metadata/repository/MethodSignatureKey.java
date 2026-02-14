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

import java.util.List;

/**
 * A {@link MethodKey} representing a method by its name and erased parameter types, providing
 * a stable, overload-safe identifier usable in both reflection-based and annotation-processor–
 * generated repository implementations.
 */
public record MethodSignatureKey(String name, List<Class<?>> parameters) implements MethodKey {}