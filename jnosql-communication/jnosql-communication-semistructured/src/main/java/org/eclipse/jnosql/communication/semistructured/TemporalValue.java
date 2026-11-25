/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.semistructured;

import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.query.TemporalQueryValue;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A {@link Value} that resolves a temporal expression from the {@link TemporalReference} each time it is read.
 * Readers always receive a {@link java.time.LocalDate}, {@link java.time.LocalTime} or
 * {@link java.time.LocalDateTime}, converted exactly like any ordinary value.
 */
final class TemporalValue implements Value {

    private final TemporalQueryValue expression;

    private final TemporalReference reference;

    TemporalValue(TemporalQueryValue expression, TemporalReference reference) {
        this.expression = Objects.requireNonNull(expression, "expression is required");
        this.reference = Objects.requireNonNull(reference, "reference is required");
    }

    @Override
    public Object get() {
        return resolve().get();
    }

    @Override
    public <T> T get(Class<T> type) {
        return resolve().get(type);
    }

    @Override
    public <T> T get(TypeSupplier<T> typeSupplier) {
        return resolve().get(typeSupplier);
    }

    @Override
    public boolean isInstanceOf(Class<?> typeClass) {
        return resolve().isInstanceOf(typeClass);
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public String toString() {
        return expression.name();
    }

    Object resolve(LocalDateTime instant) {
        return expression.from(instant);
    }

    private Value resolve() {
        return Value.of(resolve(reference.get()));
    }
}
