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
package org.eclipse.jnosql.mapping.core.repository;

import org.eclipse.jnosql.communication.Condition;

/**
 * Represents a parameter value with its condition that can be used in repository queries that has the {@link jakarta.data.repository.Find} annotation at method.
 * It will get the {@link jakarta.data.repository.Param} to each parameter value and combine it with {@link jakarta.data.repository.Is}
 * by default value is {@link Condition#EQUALS}
 */
public record ParamValue(Condition condition, Object value, boolean negate){}