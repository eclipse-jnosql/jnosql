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

import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactory;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;
import org.eclipse.jnosql.mapping.timeseries.entities.CounterReading;
import org.eclipse.jnosql.mapping.timeseries.entities.DailyReading;
import org.eclipse.jnosql.mapping.timeseries.entities.LocalReading;
import org.eclipse.jnosql.mapping.timeseries.entities.OffsetReading;
import org.eclipse.jnosql.mapping.timeseries.entities.SensorReading;
import org.eclipse.jnosql.mapping.timeseries.entities.UnidentifiedReading;
import org.eclipse.jnosql.mapping.timeseries.entities.ZonedReading;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, TimeSeriesTemplate.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
@DisplayName("Time series fluent query")
class TimeSeriesQueryTest {

    private static final Instant NOW = Instant.parse("2026-01-10T12:00:00Z");
    private static final Instant T0 = NOW.minus(Duration.ofHours(4));
    private static final Instant T1 = NOW.minus(Duration.ofHours(3));
    private static final Instant T2 = NOW.minus(Duration.ofHours(2));
    private static final Instant T3 = NOW.minus(Duration.ofHours(1));
    private static final Instant T4 = NOW;

    @Inject
    private EntityConverterFactory converterFactory;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private InMemoryDatabaseManager database;

    private TimeSeriesTemplate template;

    @BeforeEach
    void setUp() {
        database = new InMemoryDatabaseManager();
        template = templateWith(Clock.fixed(NOW, ZoneOffset.UTC));
        // sensor-1 readings at T0..T4 and one sensor-2 reading at T2
        database.store(reading(T3, "sensor-1", 13));
        database.store(reading(T0, "sensor-1", 10));
        database.store(reading(T4, "sensor-1", 14));
        database.store(reading(T1, "sensor-1", 11));
        database.store(reading(T2, "sensor-1", 12));
        database.store(reading(T2, "sensor-2", 22));
    }

    private TimeSeriesTemplate templateWith(Clock clock) {
        return new DefaultTimeSeriesTemplate(converterFactory, database.manager(),
                Mockito.mock(EventPersistManager.class), entities, converters, clock);
    }

    private static CommunicationEntity reading(Object time, String sensor, double temperature) {
        return reading("SensorReading", time, sensor, temperature);
    }

    private static CommunicationEntity reading(String entity, Object time, String sensor, double temperature) {
        var communication = CommunicationEntity.of(entity);
        communication.add(Element.of(entity.equals("SensorReading") ? "time" : "_id", time));
        communication.add(Element.of("sensor", sensor));
        communication.add(Element.of("temperature", temperature));
        return communication;
    }

    private static List<Instant> times(List<SensorReading> readings) {
        return readings.stream().map(SensorReading::getTimestamp).toList();
    }

    private static List<CriteriaCondition> conjuncts(SelectQuery query) {
        CriteriaCondition condition = query.condition().orElseThrow();
        if (condition.condition() == Condition.AND) {
            return condition.element().get(new TypeReference<List<CriteriaCondition>>() {
            });
        }
        return List.of(condition);
    }

    private static Optional<CriteriaCondition> find(SelectQuery query, String name, Condition operator) {
        return conjuncts(query).stream()
                .filter(c -> c.condition() == operator && c.element().name().equals(name))
                .findFirst();
    }

    private static List<String> sorts(SelectQuery query) {
        return query.sorts().stream()
                .map(s -> s.property() + " " + (s.isAscending() ? "ASC" : "DESC"))
                .toList();
    }

    @Nested
    @DisplayName("When requesting the latest observations")
    class WhenTheLatestObservationsAreRequested {

        @Test
        @DisplayName("Should return the newest observations first")
        void shouldReturnNewestFirst() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .latest(3);

            // Then
            assertThat(times(readings)).containsExactly(T4, T3, T2);
        }

        @Test
        @DisplayName("Should translate to descending identifier ordering, limit and preserved filter")
        void shouldTranslateToSelectQuery() {

            // When
            template.select(SensorReading.class).where("sensor").eq("sensor-1").latest(3);

            // Then
            SelectQuery query = database.lastQuery();
            assertSoftly(softly -> {
                softly.assertThat(query.name()).as("entity name").isEqualTo("SensorReading");
                softly.assertThat(sorts(query)).as("ordering uses the mapped @Id column")
                        .containsExactly("time DESC");
                softly.assertThat(query.limit()).as("limit").isEqualTo(3);
                softly.assertThat(find(query, "sensor", Condition.EQUALS)).as("existing filter is kept")
                        .isPresent();
            });
        }

        @Test
        @DisplayName("Should return every observation when the count exceeds the available ones")
        void shouldReturnAllWhenCountIsLarger() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .latest(100);

            // Then
            assertThat(times(readings)).containsExactly(T4, T3, T2, T1, T0);
        }

        @Test
        @DisplayName("Should return a single observation for a count of one")
        void shouldReturnOneObservation() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class).latest(1);

            // Then
            assertThat(times(readings)).containsExactly(T4);
        }

        @Test
        @DisplayName("Should return an empty list when nothing matches")
        void shouldReturnEmptyWhenNothingMatches() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("unknown")
                    .latest(3);

            // Then
            assertThat(readings).isEmpty();
        }

        @Test
        @DisplayName("Should reject zero and negative counts")
        void shouldRejectInvalidCounts() {

            // Then
            assertSoftly(softly -> {
                softly.assertThatThrownBy(() -> template.select(SensorReading.class).latest(0))
                        .as("zero").isInstanceOf(IllegalArgumentException.class).hasMessageContaining("0");
                softly.assertThatThrownBy(() -> template.select(SensorReading.class).latest(-1))
                        .as("negative").isInstanceOf(IllegalArgumentException.class).hasMessageContaining("-1");
            });
            assertThat(database.executions()).as("no query was executed").isZero();
        }
    }

    @Nested
    @DisplayName("When requesting the earliest observations")
    class WhenTheEarliestObservationsAreRequested {

        @Test
        @DisplayName("Should return the oldest observations first")
        void shouldReturnOldestFirst() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .earliest(3);

            // Then
            assertThat(times(readings)).containsExactly(T0, T1, T2);
        }

        @Test
        @DisplayName("Should translate to ascending identifier ordering and limit")
        void shouldTranslateToSelectQuery() {

            // When
            template.select(SensorReading.class).earliest(3);

            // Then
            SelectQuery query = database.lastQuery();
            assertSoftly(softly -> {
                softly.assertThat(sorts(query)).as("ordering").containsExactly("time ASC");
                softly.assertThat(query.limit()).as("limit").isEqualTo(3);
                softly.assertThat(query.condition()).as("no condition").isEmpty();
            });
        }

        @Test
        @DisplayName("Should return every observation when the count exceeds the available ones")
        void shouldReturnAllWhenCountIsLarger() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-2")
                    .earliest(10);

            // Then
            assertThat(times(readings)).containsExactly(T2);
        }

        @Test
        @DisplayName("Should reject zero and negative counts")
        void shouldRejectInvalidCounts() {

            // Then
            assertSoftly(softly -> {
                softly.assertThatThrownBy(() -> template.select(SensorReading.class).earliest(0))
                        .as("zero").isInstanceOf(IllegalArgumentException.class);
                softly.assertThatThrownBy(() -> template.select(SensorReading.class).earliest(-5))
                        .as("negative").isInstanceOf(IllegalArgumentException.class);
            });
        }
    }

    @Nested
    @DisplayName("When ordering explicitly")
    class WhenTheOrderingIsExplicit {

        @Test
        @DisplayName("Should keep the chronological ordering first and the explicit one second")
        void shouldPlaceChronologicalOrderingFirst() {

            // When
            template.select(SensorReading.class)
                    .orderBy("sensor").asc()
                    .latest(3);

            // Then
            SelectQuery query = database.lastQuery();
            assertSoftly(softly -> {
                softly.assertThat(sorts(query)).as("ordering").containsExactly("time DESC", "sensor ASC");
                softly.assertThat(query.limit()).as("limit").isEqualTo(3);
            });
        }

        @Test
        @DisplayName("Should preserve the relative order of several secondary orderings")
        void shouldPreserveSecondaryOrder() {

            // When
            template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .orderBy("temperature").desc()
                    .orderBy("sensor").asc()
                    .earliest(2);

            // Then
            assertThat(sorts(database.lastQuery()))
                    .containsExactly("time ASC", "temperature DESC", "sensor ASC");
        }

        @Test
        @DisplayName("Should use the secondary ordering to break identifier ties")
        void shouldBreakTiesWithSecondaryOrdering() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").in(List.of("sensor-1", "sensor-2"))
                    .orderBy("sensor").desc()
                    .latest(100);

            // Then
            assertThat(readings).extracting(SensorReading::getTemperature)
                    .containsExactly(14d, 13d, 22d, 12d, 11d, 10d);
        }

        @Test
        @DisplayName("Should not repeat an explicit ordering identical to the chronological one")
        void shouldNotDuplicateIdenticalOrdering() {

            // When
            template.select(SensorReading.class).orderBy("timestamp").desc().latest(2);

            // Then
            assertThat(sorts(database.lastQuery())).containsExactly("time DESC");
        }

        @Test
        @DisplayName("Should reject an explicit identifier ordering that contradicts the chronological one")
        void shouldRejectContradictoryOrdering() {

            // Then
            assertThatIllegalStateException()
                    .isThrownBy(() -> template.select(SensorReading.class).orderBy("timestamp").asc().latest(2))
                    .withMessageContaining("conflicts");
        }

        @Test
        @DisplayName("Should keep the explicit ordering when no chronological operation is used")
        void shouldKeepExplicitOrderingAlone() {

            // When
            template.select(SensorReading.class).orderBy("sensor").desc().result();

            // Then
            assertThat(sorts(database.lastQuery())).containsExactly("sensor DESC");
        }
    }

    @Nested
    @DisplayName("When limiting explicitly")
    class WhenTheLimitIsExplicit {

        @Test
        @DisplayName("Should keep the explicit limit on regular queries")
        void shouldKeepExplicitLimit() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .since(T1)
                    .limit(2)
                    .result();

            // Then
            assertThat(readings).hasSize(2);
            assertThat(database.lastQuery().limit()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should apply the skip configured before the chronological operation")
        void shouldKeepSkip() {

            // When
            template.select(SensorReading.class).skip(2).limit(3).result();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(database.lastQuery().skip()).as("skip").isEqualTo(2);
                softly.assertThat(database.lastQuery().limit()).as("limit").isEqualTo(3);
            });
        }

        @Test
        @DisplayName("Should reject the chronological operations when a limit was already configured")
        void shouldRejectLatestAfterLimit() {

            // Given
            var stage = (TimeSeriesQuery.Chronological) template.select(SensorReading.class).limit(2);

            // Then
            assertThatIllegalStateException().isThrownBy(() -> stage.latest(1))
                    .withMessageContaining("limit");
        }
    }

    @Nested
    @DisplayName("When filtering since a moment")
    class WhenFilteringSince {

        @Test
        @DisplayName("Should include the boundary observation")
        void shouldIncludeBoundary() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .since(T2)
                    .earliest(10);

            // Then
            assertThat(times(readings)).containsExactly(T2, T3, T4);
        }

        @Test
        @DisplayName("Should translate to an inclusive greater-than condition on the mapped identifier")
        void shouldTranslateToSelectQuery() {

            // When
            template.select(SensorReading.class).since(T2).result();

            // Then
            CriteriaCondition condition = database.lastQuery().condition().orElseThrow();
            assertSoftly(softly -> {
                softly.assertThat(condition.condition()).as("operator").isEqualTo(Condition.GREATER_EQUALS_THAN);
                softly.assertThat(condition.element().name()).as("mapped column").isEqualTo("time");
                softly.assertThat(condition.element().get()).as("value").isEqualTo(T2);
            });
        }

        @Test
        @DisplayName("Should combine with existing conditions using AND")
        void shouldCombineWithExistingConditionsUsingAnd() {

            // When
            template.select(SensorReading.class).where("sensor").eq("sensor-1").since(T2).result();

            // Then
            SelectQuery query = database.lastQuery();
            assertSoftly(softly -> {
                softly.assertThat(query.condition().orElseThrow().condition()).as("conjunction")
                        .isEqualTo(Condition.AND);
                softly.assertThat(find(query, "sensor", Condition.EQUALS)).as("existing").isPresent();
                softly.assertThat(find(query, "time", Condition.GREATER_EQUALS_THAN)).as("since").isPresent();
            });
        }

        @Test
        @DisplayName("Should reject a null boundary")
        void shouldRejectNull() {

            // Then
            assertThatNullPointerException()
                    .isThrownBy(() -> template.select(SensorReading.class).since(null));
        }

        @Test
        @DisplayName("Should reject a repeated condition")
        void shouldRejectRepeated() {

            // Given
            var stage = template.select(SensorReading.class).since(T1);

            // Then
            assertThatIllegalStateException().isThrownBy(() -> stage.since(T2))
                    .withMessageContaining("since");
        }
    }

    @Nested
    @DisplayName("When filtering until a moment")
    class WhenFilteringUntil {

        @Test
        @DisplayName("Should include the boundary observation")
        void shouldIncludeBoundary() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .until(T2)
                    .latest(10);

            // Then
            assertThat(times(readings)).containsExactly(T2, T1, T0);
        }

        @Test
        @DisplayName("Should translate to an inclusive less-than condition on the mapped identifier")
        void shouldTranslateToSelectQuery() {

            // When
            template.select(SensorReading.class).until(T2).result();

            // Then
            CriteriaCondition condition = database.lastQuery().condition().orElseThrow();
            assertSoftly(softly -> {
                softly.assertThat(condition.condition()).as("operator").isEqualTo(Condition.LESSER_EQUALS_THAN);
                softly.assertThat(condition.element().name()).as("mapped column").isEqualTo("time");
                softly.assertThat(condition.element().get()).as("value").isEqualTo(T2);
            });
        }

        @Test
        @DisplayName("Should reject a null boundary")
        void shouldRejectNull() {

            // Then
            assertThatNullPointerException()
                    .isThrownBy(() -> template.select(SensorReading.class).until(null));
        }

        @Test
        @DisplayName("Should reject a repeated condition")
        void shouldRejectRepeated() {

            // Given
            var stage = template.select(SensorReading.class).until(T3);

            // Then
            assertThatIllegalStateException().isThrownBy(() -> stage.until(T2))
                    .withMessageContaining("until");
        }
    }

    @Nested
    @DisplayName("When filtering an interval")
    class WhenFilteringAnInterval {

        @Test
        @DisplayName("Should return the observations within both inclusive boundaries")
        void shouldReturnInterval() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .since(T1)
                    .until(T3)
                    .result();

            // Then
            assertThat(times(readings)).containsExactlyInAnyOrder(T1, T2, T3);
        }

        @Test
        @DisplayName("Should return the single observation when both boundaries are equal")
        void shouldAcceptEqualBoundaries() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .since(T2)
                    .until(T2)
                    .result();

            // Then
            assertThat(times(readings)).containsExactly(T2);
        }

        @Test
        @DisplayName("Should accept the boundaries in either order")
        void shouldAcceptUntilBeforeSince() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .until(T3)
                    .since(T1)
                    .and("sensor").eq("sensor-1")
                    .result();

            // Then
            assertThat(times(readings)).containsExactlyInAnyOrder(T1, T2, T3);
        }

        @Test
        @DisplayName("Should reject a start after the end")
        void shouldRejectInvertedInterval() {

            // Then
            assertSoftly(softly -> {
                softly.assertThatThrownBy(() -> template.select(SensorReading.class).since(T3).until(T1))
                        .as("until after since").isInstanceOf(IllegalArgumentException.class);
                softly.assertThatThrownBy(() -> template.select(SensorReading.class).until(T1).since(T3))
                        .as("since after until").isInstanceOf(IllegalArgumentException.class);
            });
        }

        @Test
        @DisplayName("Should translate to both inclusive conditions")
        void shouldTranslateToSelectQuery() {

            // When
            template.select(SensorReading.class).since(T1).until(T3).result();

            // Then
            SelectQuery query = database.lastQuery();
            assertSoftly(softly -> {
                softly.assertThat(find(query, "time", Condition.GREATER_EQUALS_THAN).orElseThrow()
                        .element().get()).as("since").isEqualTo(T1);
                softly.assertThat(find(query, "time", Condition.LESSER_EQUALS_THAN).orElseThrow()
                        .element().get()).as("until").isEqualTo(T3);
            });
        }

        @Test
        @DisplayName("Should keep the position of a temporal condition relative to OR conditions")
        void shouldKeepConditionOrder() {

            // When
            template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .since(T1)
                    .or("sensor").eq("sensor-2")
                    .result();

            // Then
            assertThat(database.lastQuery().condition().orElseThrow().condition()).isEqualTo(Condition.OR);
        }
    }

    @Nested
    @DisplayName("When filtering the last duration")
    class WhenFilteringTheLastDuration {

        @Test
        @DisplayName("Should return observations within the inclusive interval ending now")
        void shouldReturnObservationsInTheInterval() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .last(Duration.ofHours(2))
                    .latest(10);

            // Then
            assertThat(times(readings)).containsExactly(T4, T3, T2);
        }

        @Test
        @DisplayName("Should translate to inclusive boundaries calculated from the same reference")
        void shouldTranslateToSelectQuery() {

            // When
            template.select(SensorReading.class).last(Duration.ofHours(24)).result();

            // Then
            SelectQuery query = database.lastQuery();
            assertSoftly(softly -> {
                softly.assertThat(find(query, "time", Condition.GREATER_EQUALS_THAN).orElseThrow()
                        .element().get()).as("start").isEqualTo(NOW.minus(Duration.ofHours(24)));
                softly.assertThat(find(query, "time", Condition.LESSER_EQUALS_THAN).orElseThrow()
                        .element().get()).as("end").isEqualTo(NOW);
            });
        }

        @Test
        @DisplayName("Should read the clock once per execution")
        void shouldReadTheClockOnce() {

            // Given
            var calls = new int[1];
            Clock ticking = new Clock() {
                @Override
                public ZoneId getZone() {
                    return ZoneOffset.UTC;
                }

                @Override
                public Clock withZone(ZoneId zone) {
                    return this;
                }

                @Override
                public Instant instant() {
                    return NOW.plusSeconds(calls[0]++);
                }
            };
            var query = templateWith(ticking).select(SensorReading.class).last(Duration.ofHours(1));

            // When
            query.result();

            // Then
            SelectQuery executed = database.lastQuery();
            Instant start = (Instant) find(executed, "time", Condition.GREATER_EQUALS_THAN).orElseThrow()
                    .element().get();
            Instant end = (Instant) find(executed, "time", Condition.LESSER_EQUALS_THAN).orElseThrow()
                    .element().get();
            assertSoftly(softly -> {
                softly.assertThat(calls[0]).as("clock reads").isEqualTo(1);
                softly.assertThat(Duration.between(start, end)).as("interval length")
                        .isEqualTo(Duration.ofHours(1));
            });
        }

        @Test
        @DisplayName("Should evaluate the reference again on every execution")
        void shouldEvaluateEveryExecution() {

            // Given
            var calls = new int[1];
            Clock ticking = new Clock() {
                @Override
                public ZoneId getZone() {
                    return ZoneOffset.UTC;
                }

                @Override
                public Clock withZone(ZoneId zone) {
                    return this;
                }

                @Override
                public Instant instant() {
                    return NOW.plus(Duration.ofHours(calls[0]++));
                }
            };
            var query = templateWith(ticking).select(SensorReading.class).last(Duration.ofHours(1));

            // When
            query.result();
            Object first = find(database.lastQuery(), "time", Condition.LESSER_EQUALS_THAN).orElseThrow()
                    .element().get();
            query.result();
            Object second = find(database.lastQuery(), "time", Condition.LESSER_EQUALS_THAN).orElseThrow()
                    .element().get();

            // Then
            assertThat(second).isNotEqualTo(first);
        }

        @Test
        @DisplayName("Should preserve existing conditions")
        void shouldPreserveConditions() {

            // When
            template.select(SensorReading.class)
                    .where("sensor").eq("sensor-2")
                    .last(Duration.ofHours(24))
                    .result();

            // Then
            SelectQuery query = database.lastQuery();
            assertSoftly(softly -> {
                softly.assertThat(find(query, "sensor", Condition.EQUALS)).as("existing").isPresent();
                softly.assertThat(conjuncts(query)).as("three conjuncts").hasSize(3);
            });
        }

        @Test
        @DisplayName("Should reject null, zero and negative durations")
        void shouldRejectInvalidDurations() {

            // Then
            assertSoftly(softly -> {
                softly.assertThatThrownBy(() -> template.select(SensorReading.class).last(null))
                        .as("null").isInstanceOf(NullPointerException.class);
                softly.assertThatThrownBy(() -> template.select(SensorReading.class).last(Duration.ZERO))
                        .as("zero").isInstanceOf(IllegalArgumentException.class);
                softly.assertThatThrownBy(() -> template.select(SensorReading.class)
                                .last(Duration.ofSeconds(-1)))
                        .as("negative").isInstanceOf(IllegalArgumentException.class);
            });
        }

        @Test
        @DisplayName("Should reject a repeated condition")
        void shouldRejectRepeated() {

            // Given
            var stage = template.select(SensorReading.class).last(Duration.ofHours(1));

            // Then
            assertThatIllegalStateException().isThrownBy(() -> stage.last(Duration.ofHours(2)))
                    .withMessageContaining("last");
        }

        @Test
        @DisplayName("Should reject combining relative and absolute boundaries")
        void shouldRejectConflictingBoundaries() {

            // Then
            assertSoftly(softly -> {
                softly.assertThatThrownBy(() -> template.select(SensorReading.class).since(T1)
                                .last(Duration.ofHours(1)))
                        .as("last after since").isInstanceOf(IllegalStateException.class);
                softly.assertThatThrownBy(() -> template.select(SensorReading.class).until(T1)
                                .last(Duration.ofHours(1)))
                        .as("last after until").isInstanceOf(IllegalStateException.class);
                softly.assertThatThrownBy(() -> template.select(SensorReading.class)
                                .last(Duration.ofHours(1)).since(T1))
                        .as("since after last").isInstanceOf(IllegalStateException.class);
                softly.assertThatThrownBy(() -> template.select(SensorReading.class)
                                .last(Duration.ofHours(1)).until(T1))
                        .as("until after last").isInstanceOf(IllegalStateException.class);
            });
        }
    }

    @Nested
    @DisplayName("When composing temporal operations")
    class WhenComposingOperations {

        @Test
        @DisplayName("Should apply since and latest")
        void shouldComposeSinceAndLatest() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .since(T1).latest(3);

            // Then
            assertThat(times(readings)).containsExactly(T4, T3, T2);
        }

        @Test
        @DisplayName("Should apply until and earliest")
        void shouldComposeUntilAndEarliest() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .until(T3).earliest(3);

            // Then
            assertThat(times(readings)).containsExactly(T0, T1, T2);
        }

        @Test
        @DisplayName("Should apply since, until and latest")
        void shouldComposeIntervalAndLatest() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .since(T1).until(T3).latest(2);

            // Then
            assertThat(times(readings)).containsExactly(T3, T2);
        }

        @Test
        @DisplayName("Should apply last and earliest")
        void shouldComposeLastAndEarliest() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .last(Duration.ofHours(3)).earliest(2);

            // Then
            assertThat(times(readings)).containsExactly(T1, T2);
        }

        @Test
        @DisplayName("Should apply conditions defined after a temporal one")
        void shouldAcceptConditionsAfterTemporal() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .since(T2)
                    .and("sensor").eq("sensor-2")
                    .latest(5);

            // Then
            assertThat(times(readings)).containsExactly(T2);
        }

        @Test
        @DisplayName("Should apply where, last, explicit ordering and latest together")
        void shouldComposeEverything() {

            // When
            template.select(SensorReading.class)
                    .where("sensor").eq("sensor-1")
                    .last(Duration.ofHours(24))
                    .orderBy("temperature").asc()
                    .latest(10);

            // Then
            SelectQuery query = database.lastQuery();
            assertSoftly(softly -> {
                softly.assertThat(sorts(query)).as("ordering").containsExactly("time DESC", "temperature ASC");
                softly.assertThat(query.limit()).as("limit").isEqualTo(10);
                softly.assertThat(conjuncts(query)).as("conditions").hasSize(3);
            });
        }

        @Test
        @DisplayName("Should execute through stream, single result and count")
        void shouldExecuteThroughOtherTerminals() {

            // Given
            var stage = template.select(SensorReading.class).where("sensor").eq("sensor-2").since(T0);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(stage.<SensorReading>stream().count()).as("stream").isEqualTo(1);
                softly.assertThat(stage.<SensorReading>singleResult()).as("single result").isNotNull();
                softly.assertThat(database.executions()).as("executions").isEqualTo(2);
            });
        }

        @Test
        @DisplayName("Should keep skip with and without conditions or ordering")
        void shouldKeepSkipInEveryStage() {

            // When
            template.select(SensorReading.class).skip(1).result();
            long fromStage = database.lastQuery().skip();
            template.select(SensorReading.class).where("sensor").eq("sensor-1").skip(2).result();
            long whereStage = database.lastQuery().skip();
            template.select(SensorReading.class).orderBy("sensor").asc().skip(3).result();
            long orderStage = database.lastQuery().skip();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(fromStage).as("from").isEqualTo(1);
                softly.assertThat(whereStage).as("where").isEqualTo(2);
                softly.assertThat(orderStage).as("order").isEqualTo(3);
            });
        }

        @Test
        @DisplayName("Should keep limit with and without conditions or ordering")
        void shouldKeepLimitInEveryStage() {

            // When
            template.select(SensorReading.class).limit(1).result();
            long fromStage = database.lastQuery().limit();
            template.select(SensorReading.class).where("sensor").eq("sensor-1").limit(2).result();
            long whereStage = database.lastQuery().limit();
            template.select(SensorReading.class).orderBy("sensor").asc().limit(3).result();
            long orderStage = database.lastQuery().limit();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(fromStage).as("from").isEqualTo(1);
                softly.assertThat(whereStage).as("where").isEqualTo(2);
                softly.assertThat(orderStage).as("order").isEqualTo(3);
            });
        }

        @Test
        @DisplayName("Should count the matching observations")
        void shouldCount() {

            // When
            long count = template.select(SensorReading.class).where("sensor").eq("sensor-1").since(T2).count();

            // Then
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return the single matching observation")
        void shouldReturnSingleResult() {

            // When
            Optional<SensorReading> reading = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-2").singleResult();

            // Then
            assertThat(reading).get().extracting(SensorReading::getTemperature).isEqualTo(22d);
        }

        @Test
        @DisplayName("Should query without any condition, ordering or limit")
        void shouldQueryEverything() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class).result();

            // Then
            assertThat(readings).hasSize(6);
            assertThat(database.lastQuery().condition()).isEmpty();
        }

        @Test
        @DisplayName("Should keep non-temporal queries untouched")
        void shouldKeepPlainQueriesUntouched() {

            // When
            List<SensorReading> readings = template.select(SensorReading.class)
                    .where("sensor").eq("sensor-2").result();

            // Then
            SelectQuery query = database.lastQuery();
            assertSoftly(softly -> {
                softly.assertThat(readings).as("result").hasSize(1);
                softly.assertThat(query.sorts()).as("no sorts").isEmpty();
                softly.assertThat(query.limit()).as("no limit").isZero();
                softly.assertThat(conjuncts(query)).as("one condition").hasSize(1);
            });
        }

        @Test
        @DisplayName("Should apply negated conditions")
        void shouldApplyNegatedCondition() {

            // When
            template.select(SensorReading.class).where("sensor").not().eq("sensor-1").since(T0).result();

            // Then
            assertThat(find(database.lastQuery(), "_NOT", Condition.NOT)).isPresent();
        }
    }

    @Nested
    @DisplayName("When mapping the temporal identifier")
    class WhenMappingTheIdentifier {

        @Test
        @DisplayName("Should use the mapped identifier column instead of a hardcoded name")
        void shouldUseMappedColumn() {

            // When
            template.select(SensorReading.class).since(T1).latest(1);

            // Then
            assertThat(find(database.lastQuery(), "time", Condition.GREATER_EQUALS_THAN)).isPresent();
        }

        @Test
        @DisplayName("Should support LocalDateTime identifiers")
        void shouldSupportLocalDateTime() {

            // Given
            var start = LocalDateTime.of(2026, 1, 10, 10, 0);
            database.store(reading("LocalReading", start, "s", 1));

            // When
            template.select(LocalReading.class).since(start).until(start.plusHours(1)).latest(1);

            // Then
            SelectQuery query = database.lastQuery();
            assertSoftly(softly -> {
                softly.assertThat(find(query, "_id", Condition.GREATER_EQUALS_THAN).orElseThrow()
                        .element().get()).as("start").isEqualTo(start);
                softly.assertThat(sorts(query)).as("ordering").containsExactly("_id DESC");
            });
        }

        @Test
        @DisplayName("Should support LocalDate identifiers")
        void shouldSupportLocalDate() {

            // Given
            var day = LocalDate.of(2026, 1, 10);
            database.store(reading("DailyReading", day, "s", 1));

            // When
            List<DailyReading> readings = template.select(DailyReading.class).since(day).until(day).earliest(5);

            // Then
            assertThat(readings).extracting(DailyReading::getTimestamp).containsExactly(day);
        }

        @Test
        @DisplayName("Should support OffsetDateTime identifiers")
        void shouldSupportOffsetDateTime() {

            // Given
            var moment = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);
            database.store(reading("OffsetReading", moment, "s", 1));

            // When
            List<OffsetReading> readings = template.select(OffsetReading.class)
                    .last(Duration.ofMinutes(5)).latest(5);

            // Then
            assertThat(readings).extracting(OffsetReading::getTimestamp).containsExactly(moment);
        }

        @Test
        @DisplayName("Should support ZonedDateTime identifiers")
        void shouldSupportZonedDateTime() {

            // Given
            var moment = ZonedDateTime.ofInstant(NOW, ZoneOffset.UTC);
            database.store(reading("ZonedReading", moment, "s", 1));

            // When
            List<ZonedReading> readings = template.select(ZonedReading.class)
                    .last(Duration.ofMinutes(5)).latest(5);

            // Then
            assertThat(readings).extracting(ZonedReading::getTimestamp).containsExactly(moment);
        }

        @Test
        @DisplayName("Should compute relative boundaries as UTC points of the same instant")
        void shouldComputeRelativeBoundariesInUtc() {

            // When
            template.select(OffsetReading.class).last(Duration.ofHours(1)).result();

            // Then
            SelectQuery query = database.lastQuery();
            assertSoftly(softly -> {
                softly.assertThat(find(query, "_id", Condition.GREATER_EQUALS_THAN).orElseThrow()
                        .element().get()).as("start")
                        .isEqualTo(OffsetDateTime.ofInstant(NOW.minus(Duration.ofHours(1)), ZoneOffset.UTC));
                softly.assertThat(find(query, "_id", Condition.LESSER_EQUALS_THAN).orElseThrow()
                        .element().get()).as("end").isEqualTo(OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC));
            });
        }

        @Test
        @DisplayName("Should reject relative intervals on local identifiers instead of guessing a time zone")
        void shouldRejectRelativeIntervalOnLocalTypes() {

            // Then
            assertSoftly(softly -> {
                softly.assertThatThrownBy(() -> template.select(LocalReading.class).last(Duration.ofHours(1)))
                        .as("LocalDateTime").isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("time zone");
                softly.assertThatThrownBy(() -> template.select(DailyReading.class).last(Duration.ofHours(1)))
                        .as("LocalDate").isInstanceOf(IllegalStateException.class);
            });
        }

        @Test
        @DisplayName("Should reject identifiers that are not temporal")
        void shouldRejectNonTemporalIdentifier() {

            // Given
            var stage = template.select(CounterReading.class);

            // Then
            assertSoftly(softly -> {
                softly.assertThatThrownBy(() -> stage.latest(1)).as("latest")
                        .isInstanceOf(IllegalStateException.class).hasMessageContaining("Long");
                softly.assertThatThrownBy(() -> stage.earliest(1)).as("earliest")
                        .isInstanceOf(IllegalStateException.class);
                softly.assertThatThrownBy(() -> stage.since(NOW)).as("since")
                        .isInstanceOf(IllegalStateException.class);
                softly.assertThatThrownBy(() -> stage.until(NOW)).as("until")
                        .isInstanceOf(IllegalStateException.class);
                softly.assertThatThrownBy(() -> stage.last(Duration.ofHours(1))).as("last")
                        .isInstanceOf(IllegalStateException.class);
            });
        }

        @Test
        @DisplayName("Should reject entities without an identifier")
        void shouldRejectMissingIdentifier() {

            // Then
            assertThatIllegalStateException()
                    .isThrownBy(() -> template.select(UnidentifiedReading.class).latest(1))
                    .withMessageContaining("@Id");
        }

        @Test
        @DisplayName("Should reject arguments whose type differs from the identifier type")
        void shouldRejectIncompatibleArguments() {

            // Then
            assertSoftly(softly -> {
                softly.assertThatThrownBy(() -> template.select(SensorReading.class)
                                .since(LocalDateTime.of(2026, 1, 1, 0, 0)))
                        .as("LocalDateTime on Instant").isInstanceOf(IllegalArgumentException.class)
                        .hasMessageContaining("Instant");
                softly.assertThatThrownBy(() -> template.select(SensorReading.class)
                                .until(ZonedDateTime.ofInstant(NOW, ZoneOffset.UTC)))
                        .as("ZonedDateTime on Instant").isInstanceOf(IllegalArgumentException.class);
                softly.assertThatThrownBy(() -> template.select(OffsetReading.class).since(NOW))
                        .as("Instant on OffsetDateTime").isInstanceOf(IllegalArgumentException.class);
                softly.assertThatThrownBy(() -> template.select(LocalReading.class).since(LocalDate.now()))
                        .as("LocalDate on LocalDateTime").isInstanceOf(IllegalArgumentException.class);
            });
        }

        @Test
        @DisplayName("Should not require a temporal identifier for non-temporal queries")
        void shouldNotRequireTemporalIdentifierForPlainQueries() {

            // Then
            assertThatCode(() -> template.select(UnidentifiedReading.class)
                    .where("sensor").eq("x").result()).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("When selecting an entity")
    class WhenTheSelectionStarts {

        @Test
        @DisplayName("Should reject a null entity type")
        void shouldRejectNullType() {

            // Then
            assertThatNullPointerException().isThrownBy(() -> template.select((Class<?>) null));
        }

        @Test
        @DisplayName("Should reject null condition and ordering names")
        void shouldRejectNullNames() {

            // Then
            assertSoftly(softly -> {
                softly.assertThatThrownBy(() -> template.select(SensorReading.class).where(null))
                        .as("where").isInstanceOf(NullPointerException.class);
                softly.assertThatThrownBy(() -> template.select(SensorReading.class).orderBy(null))
                        .as("orderBy").isInstanceOf(NullPointerException.class);
            });
        }

        @Test
        @DisplayName("Should translate every condition operator")
        void shouldTranslateEveryOperator() {

            // When
            template.select(SensorReading.class).where("sensor").like("s%").result();
            var like = find(database.lastQuery(), "sensor", Condition.LIKE);
            template.select(SensorReading.class).where("sensor").contains("s").result();
            var contains = find(database.lastQuery(), "sensor", Condition.CONTAINS);
            template.select(SensorReading.class).where("sensor").startsWith("s").result();
            var startsWith = find(database.lastQuery(), "sensor", Condition.STARTS_WITH);
            template.select(SensorReading.class).where("sensor").endsWith("1").result();
            var endsWith = find(database.lastQuery(), "sensor", Condition.ENDS_WITH);
            template.select(SensorReading.class).where("temperature").gt(1).result();
            var gt = find(database.lastQuery(), "temperature", Condition.GREATER_THAN);
            template.select(SensorReading.class).where("temperature").gte(1).result();
            var gte = find(database.lastQuery(), "temperature", Condition.GREATER_EQUALS_THAN);
            template.select(SensorReading.class).where("temperature").lt(1).result();
            var lt = find(database.lastQuery(), "temperature", Condition.LESSER_THAN);
            template.select(SensorReading.class).where("temperature").lte(1).result();
            var lte = find(database.lastQuery(), "temperature", Condition.LESSER_EQUALS_THAN);
            template.select(SensorReading.class).where("temperature").between(1, 2).result();
            var between = find(database.lastQuery(), "temperature", Condition.BETWEEN);
            template.select(SensorReading.class).where("sensor").in(List.of("a")).result();
            var in = find(database.lastQuery(), "sensor", Condition.IN);

            // Then
            assertSoftly(softly -> {
                softly.assertThat(like).as("like").isPresent();
                softly.assertThat(contains).as("contains").isPresent();
                softly.assertThat(startsWith).as("startsWith").isPresent();
                softly.assertThat(endsWith).as("endsWith").isPresent();
                softly.assertThat(gt).as("gt").isPresent();
                softly.assertThat(gte).as("gte").isPresent();
                softly.assertThat(lt).as("lt").isPresent();
                softly.assertThat(lte).as("lte").isPresent();
                softly.assertThat(between).as("between").isPresent();
                softly.assertThat(in).as("in").isPresent();
            });
        }

        @Test
        @DisplayName("Should support every condition operator")
        void shouldSupportEveryOperator() {

            // When
            template.select(SensorReading.class)
                    .where("sensor").like("s%")
                    .and("sensor").contains("s")
                    .and("sensor").startsWith("s")
                    .and("sensor").endsWith("1")
                    .and("temperature").gt(1)
                    .and("temperature").gte(1)
                    .and("temperature").lt(100)
                    .and("temperature").lte(100)
                    .and("temperature").between(1, 100)
                    .and("sensor").in(List.of("sensor-1"))
                    .and("sensor").not().eq("none")
                    .result();

            // Then
            assertThat(conjuncts(database.lastQuery())).hasSize(11);
        }
    }
}
