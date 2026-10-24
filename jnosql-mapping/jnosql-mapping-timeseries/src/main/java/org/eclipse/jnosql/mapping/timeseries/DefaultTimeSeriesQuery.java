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

import jakarta.data.Direction;
import jakarta.nosql.QueryMapper.MapperFrom;
import jakarta.nosql.QueryMapper.MapperLimit;
import jakarta.nosql.QueryMapper.MapperNameCondition;
import jakarta.nosql.QueryMapper.MapperNameOrder;
import jakarta.nosql.QueryMapper.MapperOrder;
import jakarta.nosql.QueryMapper.MapperQueryBuild;
import jakarta.nosql.QueryMapper.MapperSkip;
import jakarta.nosql.QueryMapper.MapperWhere;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Records the fluent calls and replays them against the regular {@link MapperFrom} when the query is executed.
 * Replaying allows the chronological ordering to be placed before any explicit ordering and the reference time of
 * {@link #last(Duration)} to be resolved at execution time, while reusing the existing query building and execution.
 */
final class DefaultTimeSeriesQuery implements TimeSeriesQuery.From, TimeSeriesQuery.Where,
        TimeSeriesQuery.NotCondition, TimeSeriesQuery.Order, TimeSeriesQuery.NameOrder,
        MapperLimit, MapperSkip {

    private static final Set<Class<?>> TEMPORAL_TYPES = Set.of(Instant.class, LocalDateTime.class,
            LocalDate.class, OffsetDateTime.class, ZonedDateTime.class);

    private static final Set<Class<?>> RELATIVE_TYPES = Set.of(Instant.class, OffsetDateTime.class,
            ZonedDateTime.class);

    private final EntityMetadata mapping;
    private final Supplier<MapperFrom> source;
    private final Clock clock;

    private final List<Condition> conditions = new ArrayList<>();
    private final List<Sorting> sortings = new ArrayList<>();

    private Logic pendingLogic = Logic.WHERE;
    private String pendingName;
    private boolean pendingNegate;
    private String pendingOrder;

    private long skip;
    private long limit;

    private Temporal since;
    private Temporal until;
    private Duration last;

    DefaultTimeSeriesQuery(EntityMetadata mapping, Supplier<MapperFrom> source, Clock clock) {
        this.mapping = requireNonNull(mapping, "mapping is required");
        this.source = requireNonNull(source, "source is required");
        this.clock = requireNonNull(clock, "clock is required");
    }

    @Override
    public TimeSeriesQuery.NameCondition where(String name) {
        return pending(Logic.WHERE, name);
    }

    @Override
    public TimeSeriesQuery.NameCondition and(String name) {
        return pending(Logic.AND, name);
    }

    @Override
    public TimeSeriesQuery.NameCondition or(String name) {
        return pending(Logic.OR, name);
    }

    private TimeSeriesQuery.NameCondition pending(Logic logic, String name) {
        requireNonNull(name, "name is required");
        this.pendingLogic = logic;
        this.pendingName = name;
        return this;
    }

    @Override
    public TimeSeriesQuery.NotCondition not() {
        this.pendingNegate = true;
        return this;
    }

    @Override
    public <T> TimeSeriesQuery.Where eq(T value) {
        requireNonNull(value, "value is required");
        return condition((condition, reference) -> condition.eq(value));
    }

    @Override
    public TimeSeriesQuery.Where like(String value) {
        requireNonNull(value, "value is required");
        return condition((condition, reference) -> condition.like(value));
    }

    @Override
    public TimeSeriesQuery.Where contains(String value) {
        requireNonNull(value, "value is required");
        return condition((condition, reference) -> condition.contains(value));
    }

    @Override
    public TimeSeriesQuery.Where startsWith(String value) {
        requireNonNull(value, "value is required");
        return condition((condition, reference) -> condition.startsWith(value));
    }

    @Override
    public TimeSeriesQuery.Where endsWith(String value) {
        requireNonNull(value, "value is required");
        return condition((condition, reference) -> condition.endsWith(value));
    }

    @Override
    public <T> TimeSeriesQuery.Where gt(T value) {
        requireNonNull(value, "value is required");
        return condition((condition, reference) -> condition.gt(value));
    }

    @Override
    public <T> TimeSeriesQuery.Where gte(T value) {
        requireNonNull(value, "value is required");
        return condition((condition, reference) -> condition.gte(value));
    }

    @Override
    public <T> TimeSeriesQuery.Where lt(T value) {
        requireNonNull(value, "value is required");
        return condition((condition, reference) -> condition.lt(value));
    }

    @Override
    public <T> TimeSeriesQuery.Where lte(T value) {
        requireNonNull(value, "value is required");
        return condition((condition, reference) -> condition.lte(value));
    }

    @Override
    public <T> TimeSeriesQuery.Where between(T valueA, T valueB) {
        requireNonNull(valueA, "valueA is required");
        requireNonNull(valueB, "valueB is required");
        return condition((condition, reference) -> condition.between(valueA, valueB));
    }

    @Override
    public <T> TimeSeriesQuery.Where in(Iterable<T> values) {
        requireNonNull(values, "values is required");
        return condition((condition, reference) -> condition.in(values));
    }

    private TimeSeriesQuery.Where condition(BiFunction<MapperNameCondition, Instant, MapperWhere> operator) {
        conditions.add(new Condition(pendingLogic, pendingName, pendingNegate, operator));
        this.pendingName = null;
        this.pendingNegate = false;
        return this;
    }

    @Override
    public TimeSeriesQuery.Order orderBy(String name) {
        requireNonNull(name, "name is required");
        this.pendingOrder = name;
        return this;
    }

    @Override
    public TimeSeriesQuery.NameOrder asc() {
        sortings.add(new Sorting(pendingOrder, Direction.ASC));
        return this;
    }

    @Override
    public TimeSeriesQuery.NameOrder desc() {
        sortings.add(new Sorting(pendingOrder, Direction.DESC));
        return this;
    }

    @Override
    public MapperSkip skip(long start) {
        this.skip = start;
        return this;
    }

    @Override
    public MapperLimit limit(long limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public TimeSeriesQuery.Where since(Temporal start) {
        requireNonNull(start, "start is required");
        FieldMetadata id = temporalId();
        requireSameType(id, start, "start");
        if (since != null) {
            throw new IllegalStateException("since was already defined for this query");
        }
        requireNoRelativeInterval("since");
        if (until != null && isAfter(start, until)) {
            throw new IllegalArgumentException("since " + start + " is after until " + until);
        }
        this.since = start;
        return temporalCondition(id, (condition, reference) -> condition.gte(start));
    }

    @Override
    public TimeSeriesQuery.Where until(Temporal end) {
        requireNonNull(end, "end is required");
        FieldMetadata id = temporalId();
        requireSameType(id, end, "end");
        if (until != null) {
            throw new IllegalStateException("until was already defined for this query");
        }
        requireNoRelativeInterval("until");
        if (since != null && isAfter(since, end)) {
            throw new IllegalArgumentException("until " + end + " is before since " + since);
        }
        this.until = end;
        return temporalCondition(id, (condition, reference) -> condition.lte(end));
    }

    @Override
    public TimeSeriesQuery.Where last(Duration duration) {
        requireNonNull(duration, "duration is required");
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("duration must be positive but was " + duration);
        }
        FieldMetadata id = temporalId();
        Class<?> type = id.type();
        if (!RELATIVE_TYPES.contains(type)) {
            throw new IllegalStateException("last(Duration) requires an identifier of type Instant, OffsetDateTime"
                    + " or ZonedDateTime to avoid guessing a time zone, but "
                    + mapping.name() + "." + id.fieldName() + " is " + type.getName());
        }
        if (last != null) {
            throw new IllegalStateException("last was already defined for this query");
        }
        if (since != null || until != null) {
            throw new IllegalStateException("last cannot be combined with since or until");
        }
        this.last = duration;
        temporalCondition(id, (condition, reference) -> condition.gte(convert(type, reference.minus(duration))));
        return temporalCondition(id, (condition, reference) -> condition.lte(convert(type, reference)));
    }

    @Override
    public <T> List<T> latest(int count) {
        return chronological(count, Direction.DESC);
    }

    @Override
    public <T> List<T> earliest(int count) {
        return chronological(count, Direction.ASC);
    }

    @Override
    public long count() {
        return build(null, limit).count();
    }

    @Override
    public <T> List<T> result() {
        return build(null, limit).<T>result();
    }

    @Override
    public <T> Stream<T> stream() {
        return build(null, limit).<T>stream();
    }

    @Override
    public <T> Optional<T> singleResult() {
        return build(null, limit).<T>singleResult();
    }

    private <T> List<T> chronological(int count, Direction direction) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive but was " + count);
        }
        temporalId();
        if (limit > 0) {
            throw new IllegalStateException("latest and earliest define the limit, "
                    + "so a limit cannot be configured before them");
        }
        return build(direction, count).<T>result();
    }

    private TimeSeriesQuery.Where temporalCondition(FieldMetadata id,
                                                    BiFunction<MapperNameCondition, Instant, MapperWhere> operator) {
        conditions.add(new Condition(Logic.AND, id.fieldName(), false, operator));
        return this;
    }

    private FieldMetadata temporalId() {
        FieldMetadata id = mapping.id().orElseThrow(() -> new IllegalStateException(
                "The entity " + mapping.name() + " has no attribute annotated with @Id, "
                        + "which is required by temporal operations"));
        if (!TEMPORAL_TYPES.contains(id.type())) {
            throw new IllegalStateException("The @Id attribute " + mapping.name() + "." + id.fieldName()
                    + " is of type " + id.type().getName() + ", which does not support temporal operations. "
                    + "Supported types: Instant, LocalDateTime, LocalDate, OffsetDateTime and ZonedDateTime");
        }
        return id;
    }

    private void requireSameType(FieldMetadata id, Temporal value, String argument) {
        if (!id.type().equals(value.getClass())) {
            throw new IllegalArgumentException("The " + argument + " argument is of type "
                    + value.getClass().getName() + " but the @Id attribute " + mapping.name() + "."
                    + id.fieldName() + " is of type " + id.type().getName());
        }
    }

    private void requireNoRelativeInterval(String operation) {
        if (last != null) {
            throw new IllegalStateException(operation + " cannot be combined with last");
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean isAfter(Temporal first, Temporal second) {
        return ((Comparable) first).compareTo(second) > 0;
    }

    private static Temporal convert(Class<?> type, Instant instant) {
        if (OffsetDateTime.class.equals(type)) {
            return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        if (ZonedDateTime.class.equals(type)) {
            return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        return instant;
    }

    private MapperQueryBuild build(Direction chronological, long maxResults) {
        Instant reference = last == null ? null : clock.instant();
        MapperFrom from = source.get();
        MapperWhere where = null;
        for (Condition condition : conditions) {
            MapperNameCondition name;
            if (where == null) {
                name = from.where(condition.name());
            } else if (condition.logic() == Logic.OR) {
                name = where.or(condition.name());
            } else {
                name = where.and(condition.name());
            }
            if (condition.negate()) {
                name = name.not();
            }
            where = condition.operator().apply(name, reference);
        }

        MapperNameOrder order = null;
        for (Sorting sorting : sorting(chronological)) {
            MapperOrder next;
            if (order != null) {
                next = order.orderBy(sorting.name());
            } else if (where != null) {
                next = where.orderBy(sorting.name());
            } else {
                next = from.orderBy(sorting.name());
            }
            order = sorting.direction() == Direction.ASC ? next.asc() : next.desc();
        }

        if (maxResults > 0) {
            MapperLimit limited;
            if (order != null) {
                limited = order.limit(maxResults);
            } else if (where != null) {
                limited = where.limit(maxResults);
            } else {
                limited = from.limit(maxResults);
            }
            return skip > 0 ? limited.skip(skip) : limited;
        }
        if (skip > 0) {
            if (order != null) {
                return order.skip(skip);
            } else if (where != null) {
                return where.skip(skip);
            }
            return from.skip(skip);
        }
        if (order != null) {
            return order;
        }
        return where != null ? where : from;
    }

    private List<Sorting> sorting(Direction chronological) {
        if (chronological == null) {
            return sortings;
        }
        FieldMetadata id = temporalId();
        String idColumn = mapping.columnField(id.fieldName());
        List<Sorting> result = new ArrayList<>();
        result.add(new Sorting(id.fieldName(), chronological));
        for (Sorting sorting : sortings) {
            if (idColumn.equals(mapping.columnField(sorting.name()))) {
                if (sorting.direction() != chronological) {
                    throw new IllegalStateException("The explicit " + sorting.direction() + " ordering by "
                            + sorting.name() + " conflicts with the " + chronological + " chronological ordering");
                }
            } else {
                result.add(sorting);
            }
        }
        return result;
    }

    private enum Logic {
        WHERE, AND, OR
    }

    private record Condition(Logic logic, String name, boolean negate,
                             BiFunction<MapperNameCondition, Instant, MapperWhere> operator) {
    }

    private record Sorting(String name, Direction direction) {
    }
}
