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
package org.eclipse.jnosql.communication.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.Objects;
import java.util.function.Function;

/**
 * Represents the Jakarta Query temporal special expressions {@code LOCAL DATE}, {@code LOCAL TIME}
 * and {@code LOCAL DATETIME}.
 * <p>
 * The constants only describe the expression; they never capture a date or time while the query is parsed.
 * The value is obtained from a reference instant, either through {@link #from(LocalDateTime)} or through
 * {@link #get()}, which uses the application clock and its default time zone.
 * The database server clock is not consulted.
 */
public enum TemporalQueryValue implements QueryValue<Temporal> {

    /**
     * {@code LOCAL DATE}, resolved as a {@link LocalDate}.
     */
    LOCAL_DATE(LocalDateTime::toLocalDate),
    /**
     * {@code LOCAL TIME}, resolved as a {@link LocalTime}.
     */
    LOCAL_TIME(LocalDateTime::toLocalTime),
    /**
     * {@code LOCAL DATETIME}, resolved as a {@link LocalDateTime}.
     */
    LOCAL_DATETIME(reference -> reference);

    private final Function<LocalDateTime, ? extends Temporal> resolver;

    TemporalQueryValue(Function<LocalDateTime, ? extends Temporal> resolver) {
        this.resolver = resolver;
    }

    /**
     * Resolves this expression from a reference date and time.
     *
     * @param reference the reference date and time
     * @return a {@link LocalDate}, {@link LocalTime} or {@link LocalDateTime} according to this expression
     * @throws NullPointerException when the reference is null
     */
    public Temporal from(LocalDateTime reference) {
        return resolver.apply(Objects.requireNonNull(reference, "reference is required"));
    }

    /**
     * Resolves this expression using the current date and time of the application clock.
     *
     * @return a {@link LocalDate}, {@link LocalTime} or {@link LocalDateTime} according to this expression
     */
    @Override
    public Temporal get() {
        return from(LocalDateTime.now());
    }

    @Override
    public ValueType type() {
        return ValueType.TEMPORAL;
    }
}
