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
 *   Elias Nogueira
 *
 */
package org.eclipse.jnosql.communication.reader;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ArrayReaderTest {


    @Test
    void shouldConvertListToArray() {
        List<Integer> elements = List.of(97,98,99,100);
        Value value = Value.of(elements);
        byte[] bytes = value.get(byte[].class);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(bytes).as("Should be able to convert List to byte[]").isNotNull();
            softly.assertThat(bytes.length).as("Should be able to convert List to byte[]").isEqualTo(elements.size());
            for (int i = 0; i < elements.size(); i++) {
                softly.assertThat(bytes[i]).as("Should be able to convert List to byte[]").isEqualTo(elements.get(i));
            }
        });
    }
}
