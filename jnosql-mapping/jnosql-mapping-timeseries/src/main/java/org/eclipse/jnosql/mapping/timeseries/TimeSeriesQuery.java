/*
 *
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
package org.eclipse.jnosql.mapping.timeseries;

import jakarta.nosql.QueryMapper;

import java.time.Duration;
import java.util.List;

/**
 * A fluent query for time-series entities. It extends the {@link QueryMapper} stages with temporal operations that
 * are always applied to the attribute annotated with {@link jakarta.nosql.Id}, the temporal identifier of the entity.
 *
 * <p>Every operation is translated into an ordinary
 * {@link org.eclipse.jnosql.communication.semistructured.SelectQuery}: conditions, ordering and limit.</p>
 *
 * <p>The mapped identifier type must be one of {@link java.time.Instant}, {@link java.time.LocalDateTime},
 * {@link java.time.LocalDate}, {@link java.time.OffsetDateTime} or {@link java.time.ZonedDateTime}.
 * Temporal arguments must have exactly the same type as the identifier; no conversion or time-zone guessing
 * is ever performed.</p>
 *
 * <pre>{@code
 * List<SensorReading> readings = template.select(SensorReading.class)
 *         .where("sensor").eq("sensor-1")
 *         .last(Duration.ofHours(24))
 *         .latest(10);
 * }</pre>
 *
 * @see TimeSeriesTemplate#select(Class)
 */
public final class TimeSeriesQuery {

    private TimeSeriesQuery() {
    }

    /**
     * Terminal operations that order the observations chronologically and limit the result.
     * The chronological ordering always has the highest priority; any explicit ordering becomes secondary.
     * A limit cannot be configured explicitly before these operations.
     */
    public interface Chronological {

        /**
         * Executes the query returning the most recent observations, ordered by the identifier descending.
         *
         * @param count the maximum number of observations, must be positive
         * @param <T>   the entity type
         * @return the matching entities, newest first
         * @throws IllegalArgumentException if {@code count} is not positive
         * @throws IllegalStateException    if the identifier is not a supported temporal attribute or a limit is
         *                                  already configured
         */
        <T> List<T> latest(int count);

        /**
         * Executes the query returning the oldest observations, ordered by the identifier ascending.
         *
         * @param count the maximum number of observations, must be positive
         * @param <T>   the entity type
         * @return the matching entities, oldest first
         * @throws IllegalArgumentException if {@code count} is not positive
         * @throws IllegalStateException    if the identifier is not a supported temporal attribute or a limit is
         *                                  already configured
         */
        <T> List<T> earliest(int count);
    }

    /**
     * Intermediate temporal conditions on the identifier, combined with existing conditions using {@code AND}.
     * Each of {@code since}, {@code until} and {@code last} can be used once per query, and
     * {@code last} cannot be combined with {@code since} or {@code until}.
     */
    public interface Interval extends Chronological {

        /**
         * Keeps the observations whose identifier is greater than or equal to {@code start} (inclusive).
         *
         * @param start the lower boundary
         * @return the next stage
         * @throws NullPointerException     if {@code start} is null
         * @throws IllegalArgumentException if the type does not match the identifier type or {@code start}
         *                                  is after a previously defined {@code until}
         * @throws IllegalStateException    if the identifier is not a supported temporal attribute, or
         *                                  {@code since} or {@code last} was already used
         */
        Where since(java.time.temporal.Temporal start);

        /**
         * Keeps the observations whose identifier is less than or equal to {@code end} (inclusive).
         *
         * @param end the upper boundary
         * @return the next stage
         * @throws NullPointerException     if {@code end} is null
         * @throws IllegalArgumentException if the type does not match the identifier type or {@code end}
         *                                  is before a previously defined {@code since}
         * @throws IllegalStateException    if the identifier is not a supported temporal attribute, or
         *                                  {@code until} or {@code last} was already used
         */
        Where until(java.time.temporal.Temporal end);

        /**
         * Keeps the observations within the interval {@code [now - duration, now]}, both inclusive.
         * The reference time {@code now} is read once, each time the query is executed, and used to compute both
         * boundaries. It is supported for {@link java.time.Instant}, {@link java.time.OffsetDateTime} and
         * {@link java.time.ZonedDateTime} identifiers (the last two expressed in UTC);
         * local types are rejected because they would require guessing a time zone.
         *
         * @param duration the length of the interval, must be positive
         * @return the next stage
         * @throws NullPointerException     if {@code duration} is null
         * @throws IllegalArgumentException if {@code duration} is zero or negative
         * @throws IllegalStateException    if the identifier does not support relative intervals, or
         *                                  {@code last}, {@code since} or {@code until} was already used
         */
        Where last(Duration duration);
    }

    /**
     * Entry stage, returned by {@link TimeSeriesTemplate#select(Class)}.
     */
    public interface From extends QueryMapper.MapperFrom, Interval {

        @Override
        NameCondition where(String name);

        @Override
        Order orderBy(String name);
    }

    /**
     * Stage after at least one condition was defined.
     */
    public interface Where extends QueryMapper.MapperWhere, Interval {

        @Override
        NameCondition and(String name);

        @Override
        NameCondition or(String name);

        @Override
        Order orderBy(String name);
    }

    /**
     * Stage waiting for a condition operator on a named attribute.
     */
    public interface NameCondition extends QueryMapper.MapperNameCondition {

        @Override
        <T> Where eq(T value);

        @Override
        Where like(String value);

        @Override
        <T> Where gt(T value);

        @Override
        <T> Where gte(T value);

        @Override
        <T> Where lt(T value);

        @Override
        <T> Where lte(T value);

        @Override
        <T> Where between(T valueA, T valueB);

        @Override
        <T> Where in(Iterable<T> values);

        @Override
        NotCondition not();
    }

    /**
     * Stage after a negation.
     */
    public interface NotCondition extends QueryMapper.MapperNotCondition, NameCondition {

        @Override
        NotCondition not();
    }

    /**
     * Stage waiting for the sort direction.
     */
    public interface Order extends QueryMapper.MapperOrder {

        @Override
        NameOrder asc();

        @Override
        NameOrder desc();
    }

    /**
     * Stage after a sort direction. Explicit ordering defined here is secondary to
     * {@link Chronological#latest(int)} and {@link Chronological#earliest(int)}.
     */
    public interface NameOrder extends QueryMapper.MapperNameOrder, Chronological {

        @Override
        Order orderBy(String name);
    }
}
