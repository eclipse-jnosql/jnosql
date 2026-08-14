/*
 *
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.communication.writer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class TemporalValueWriterTest {

    @Nested
    @DisplayName("When the supported type is checked")
    class WhenTheSupportedTypeIsChecked {

        @Test
        @DisplayName("Should accept temporal types and reject other types")
        void shouldTestTemporal() {
            TemporalValueWriter writer = new TemporalValueWriter();

            assertSoftly(softly -> {
                softly.assertThat(writer.test(Temporal.class)).isTrue();
                softly.assertThat(writer.test(String.class)).isFalse();
            });
        }
    }

    @Nested
    @DisplayName("When the temporal value is written")
    class WhenTheTemporalValueIsWritten {

        @Test
        @DisplayName("Should write temporal values using their string representation")
        void shouldWriteTemporal() {
            TemporalValueWriter writer = new TemporalValueWriter();
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            assertThat(writer.write(now)).isEqualTo(now.toString());

            Temporal customTemporal = LocalDateTime.parse("2022-01-01 12:00:00", formatter);
            assertThat(writer.write(customTemporal)).isEqualTo("2022-01-01T12:00");
        }
    }
}