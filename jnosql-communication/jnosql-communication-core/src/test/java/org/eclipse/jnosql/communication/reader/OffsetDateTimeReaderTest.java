/*
 *
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 *   Elias Nogueira
 *
 */
package org.eclipse.jnosql.communication.reader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class OffsetDateTimeReaderTest {

    private final OffsetDateTimeReader dateReader = new OffsetDateTimeReader();

    @Test
    @DisplayName("Should be compatible")
    void shouldValidateCompatibility() {
        assertThat(dateReader.test(OffsetDateTime.class)).isTrue();
    }

    @Test
    @DisplayName("Should be incompatible")
    void shouldValidateIncompatibility() {
        assertThat(dateReader.test(LocalTime.class)).isFalse();
    }

    //TODO: double-check if the conversions must be done using OffSetDate (previous code) or using OffSetDateTime
    @Test
    @DisplayName("Should be abe to convert to OffsetTime")
    void shouldConvert() {
        final OffsetDateTime offsetDateTime = OffsetDateTime.now();
        final String text = offsetDateTime.toString();
        final Date date = new Date();
        final Calendar calendar = Calendar.getInstance();

        assertSoftly(softly -> {
            softly.assertThat(dateReader.read(OffsetDateTime.class, offsetDateTime)).as("OffsetDateTime conversion")
                    .isEqualToIgnoringSeconds(offsetDateTime);

            softly.assertThat(dateReader.read(OffsetDateTime.class, date)).as("Date conversion")
                    .isEqualToIgnoringSeconds(calendar.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime());

            softly.assertThat(dateReader.read(OffsetDateTime.class, calendar)).as("Calendar conversion")
                    .isEqualTo(calendar.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime());

            softly.assertThat(dateReader.read(OffsetDateTime.class, date)).as("Number conversion")
                    .isEqualTo(date.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime());

            softly.assertThat(dateReader.read(OffsetDateTime.class, date.getTime())).as("Default conversion")
                    .isEqualToIgnoringSeconds(OffsetDateTime.parse(offsetDateTime.toString()));

            softly.assertThat(dateReader.read(OffsetDateTime.class,text)).as("Default conversion")
                    .isEqualToIgnoringSeconds(OffsetDateTime.parse(offsetDateTime.toString()));
        });
    }
}
