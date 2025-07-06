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

import org.eclipse.jnosql.communication.ValueWriter;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptionalValueWriterTest {

    private final ValueWriter<Optional<String>, String> writer = new OptionalValueWriter<>();


    @Test
    void shouldReturnSupportedOptional() {

        assertTrue(writer.test(Optional.class));
        assertFalse(writer.test(String.class));
    }

    @Test
    void shouldReturnValueFromOptional() {

        Optional<String> nonEmptyOptional = Optional.of("TestValue");
        assertEquals("TestValue", writer.write(nonEmptyOptional));

        Optional<String> emptyOptional = Optional.empty();
        assertNull(writer.write(emptyOptional));
    }

}