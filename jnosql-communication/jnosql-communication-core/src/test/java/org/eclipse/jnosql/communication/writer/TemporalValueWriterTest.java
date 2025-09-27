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

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemporalValueWriterTest {

    @Test
    void shouldTestTemporal() {
        TemporalValueWriter writer = new TemporalValueWriter();

        assertTrue(writer.test(Temporal.class));
        assertFalse(writer.test(String.class));
    }

    @Test
    void shouldWriteTemporal() {
        TemporalValueWriter writer = new TemporalValueWriter();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        assertEquals(now.toString(), writer.write(now));

        Temporal customTemporal = LocalDateTime.parse("2022-01-01 12:00:00", formatter);
        assertEquals("2022-01-01T12:00", writer.write(customTemporal));
    }
}