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

import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.query.TemporalQueryValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TemporalValue contract")
class TemporalValueTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 4, 10, 15, 30);

    private final Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    private TemporalValue value(TemporalQueryValue expression) {
        return new TemporalValue(expression, TemporalReference.live(clock));
    }

    @Test
    @DisplayName("should resolve the Java type of each expression")
    void shouldResolveJavaType() {
        assertThat(value(TemporalQueryValue.LOCAL_DATE).get()).isEqualTo(LocalDate.of(2026, 3, 4));
        assertThat(value(TemporalQueryValue.LOCAL_TIME).get()).isEqualTo(LocalTime.of(10, 15, 30));
        assertThat(value(TemporalQueryValue.LOCAL_DATETIME).get()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("should never expose the expression to drivers")
    void shouldNotLeakExpression() {
        for (var expression : TemporalQueryValue.values()) {
            assertThat(value(expression).get()).isNotInstanceOf(TemporalQueryValue.class)
                    .isNotInstanceOf(java.util.function.Supplier.class);
        }
    }

    @Test
    @DisplayName("should support typed access")
    void shouldGetTyped() {
        var date = value(TemporalQueryValue.LOCAL_DATE);
        assertThat(date.get(LocalDate.class)).isEqualTo(LocalDate.of(2026, 3, 4));
        assertThat(date.get(String.class)).isEqualTo(Value.of(LocalDate.of(2026, 3, 4)).get(String.class));
        var reference = new TypeReference<List<LocalDate>>() { };
        var ordinary = Value.of(LocalDate.of(2026, 3, 4));
        assertThat(captureOutcome(() -> date.get(reference))).isEqualTo(captureOutcome(() -> ordinary.get(reference)));
        assertThat(value(TemporalQueryValue.LOCAL_DATETIME).get(LocalDateTime.class)).isEqualTo(NOW);
    }

    private static Object captureOutcome(Supplier<Object> action) {
        try {
            return action.get();
        } catch (RuntimeException exception) {
            return exception.getClass();
        }
    }

    @Test
    @DisplayName("should support instance checks")
    void shouldCheckInstance() {
        assertThat(value(TemporalQueryValue.LOCAL_TIME).isInstanceOf(LocalTime.class)).isTrue();
        assertThat(value(TemporalQueryValue.LOCAL_TIME).isInstanceOf(LocalDate.class)).isFalse();
        assertThat(value(TemporalQueryValue.LOCAL_DATE).isInstanceOf(LocalDate.class)).isTrue();
        assertThat(value(TemporalQueryValue.LOCAL_DATETIME).isInstanceOf(LocalDateTime.class)).isTrue();
    }

    @Test
    @DisplayName("should never be null")
    void shouldNotBeNull() {
        for (var expression : TemporalQueryValue.values()) {
            assertThat(value(expression).isNull()).isFalse();
        }
    }

    @Test
    @DisplayName("should behave like an ordinary value")
    void shouldMatchOrdinaryValue() {
        var ordinary = Value.of(NOW);
        var temporal = value(TemporalQueryValue.LOCAL_DATETIME);
        assertThat(temporal.get()).isEqualTo(ordinary.get());
        assertThat(temporal.get(String.class)).isEqualTo(ordinary.get(String.class));
        assertThat(temporal.isInstanceOf(LocalDateTime.class)).isEqualTo(ordinary.isInstanceOf(LocalDateTime.class));
        assertThat(temporal.isNull()).isEqualTo(ordinary.isNull());
    }

    @Test
    @DisplayName("should reject null arguments")
    void shouldRejectNulls() {
        var reference = TemporalReference.live(clock);
        assertThatThrownBy(() -> new TemporalValue(null, reference)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new TemporalValue(TemporalQueryValue.LOCAL_DATE, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should stay consistent after a refresh and follow the clock otherwise")
    void shouldPinOnRefresh() {
        var mutable = new MutableClock(NOW);
        var reference = TemporalReference.live(mutable);
        var date = new TemporalValue(TemporalQueryValue.LOCAL_DATE, reference);
        var time = new TemporalValue(TemporalQueryValue.LOCAL_TIME, reference);

        reference.refresh();
        mutable.set(NOW.plusDays(1));
        assertThat(date.get()).isEqualTo(NOW.toLocalDate());
        assertThat(time.get()).isEqualTo(NOW.toLocalTime());

        reference.refresh();
        assertThat(date.get()).isEqualTo(NOW.plusDays(1).toLocalDate());
    }

    static final class MutableClock extends Clock {

        private LocalDateTime current;

        MutableClock(LocalDateTime current) {
            this.current = current;
        }

        void set(LocalDateTime current) {
            this.current = current;
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return current.toInstant(ZoneOffset.UTC);
        }
    }
}
