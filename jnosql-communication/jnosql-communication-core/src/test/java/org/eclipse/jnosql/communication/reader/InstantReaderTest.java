/*
 *
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
 *
 */
package org.eclipse.jnosql.communication.reader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class InstantReaderTest {

    private final InstantReader instantReader = new InstantReader();

    @Test
    @DisplayName("Should be compatible")
    void shouldValidateCompatibility() {
        assertThat(instantReader.test(Instant.class)).isTrue();
    }

    @Test
    @DisplayName("Should be incompatible")
    void shouldValidateIncompatibility() {
        assertThat(instantReader.test(LocalDateTime.class)).isFalse();
    }

    @Test
    @DisplayName("Should be able to convert to Instant")
    void shouldConvert() {
        final Instant now = Instant.now();
        final Date date = new Date();
        final Calendar calendar = Calendar.getInstance();

        assertSoftly(softly -> {
            softly.assertThat(instantReader.read(Instant.class, now)).as("Instant conversion")
                    .isEqualTo(now);

            softly.assertThat(instantReader.read(Instant.class, date)).as("Date conversion")
                    .isEqualTo(date.toInstant());

            softly.assertThat(instantReader.read(Instant.class, calendar)).as("Calendar conversion")
                    .isEqualTo(calendar.toInstant());

            softly.assertThat(instantReader.read(Instant.class, date.getTime())).as("Number conversion")
                    .isEqualTo(Instant.ofEpochMilli(date.getTime()));

            softly.assertThat(instantReader.read(Instant.class, now.toString())).as("String ISO-8601 conversion")
                    .isEqualTo(Instant.parse(now.toString()));
        });
    }
}