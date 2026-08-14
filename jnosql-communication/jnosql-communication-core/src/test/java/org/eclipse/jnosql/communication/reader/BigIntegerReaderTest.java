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

import org.eclipse.jnosql.communication.ValueReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class BigIntegerReaderTest {

    private static final String LARGE_INTEGER = "12345678901234567890";

    private final ValueReader valueReader = new BigIntegerReader();

    @Test
    @DisplayName("Should be compatible")
    void shouldValidateCompatibility() {
        assertThat(valueReader.test(BigInteger.class)).isTrue();
    }

    @Test
    @DisplayName("Should be incompatible")
    void shouldValidateIncompatibility() {
        assertSoftly(softly -> {
            softly.assertThat(valueReader.test(AtomicBoolean.class)).as("AtomicBoolean is not compatible").isFalse();
            softly.assertThat(valueReader.test(Boolean.class)).as("Boolean is not compatible").isFalse();
        });
    }

    @Test
    @DisplayName("Should be able to convert the value to BigInteger")
    void shouldConvert() {
        BigInteger bigInteger = BigInteger.TEN;

        assertSoftly(softly -> {
            softly.assertThat(valueReader.read(BigInteger.class, bigInteger)).as("BigInteger conversion").isEqualTo(bigInteger);
            softly.assertThat(valueReader.read(BigInteger.class, 10.99D)).as("Number conversion").isEqualTo(bigInteger);
            softly.assertThat(valueReader.read(BigInteger.class, "10")).as("String conversion").isEqualTo(bigInteger);
        });
    }

    @Test
    @DisplayName("Should preserve a BigDecimal beyond the Long range")
    void shouldPreserveLargeBigDecimal() {
        BigInteger expected = new BigInteger(LARGE_INTEGER);

        assertThat(valueReader.read(BigInteger.class, new BigDecimal(LARGE_INTEGER))).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should parse a String beyond the Long range")
    void shouldParseLargeString() {
        BigInteger expected = new BigInteger(LARGE_INTEGER);

        assertThat(valueReader.read(BigInteger.class, LARGE_INTEGER)).isEqualTo(expected);
    }
}
