/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *   Matheus Oliveira
 */
package org.eclipse.jnosql.mapping.semistructured;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnsupportedFunctionExceptionTest {

    @DisplayName("Should create with function and database name")
    @Test
    void shouldCreateWithFunctionAndDatabaseName() {
        var ex = new UnsupportedFunctionException("UPPER", "TestDB");
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(ex.getMessage()).contains("UPPER");
            soft.assertThat(ex.getMessage()).contains("TestDB");
        });
    }

    @DisplayName("Should create with message")
    @Test
    void shouldCreateWithMessage() {
        var ex = new UnsupportedFunctionException("function not supported");
        assertThat(ex.getMessage()).isEqualTo("function not supported");
    }

    @DisplayName("Should create with message and cause")
    @Test
    void shouldCreateWithMessageAndCause() {
        var cause = new RuntimeException("root cause");
        var ex = new UnsupportedFunctionException("function not supported", cause);
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(ex.getMessage()).isEqualTo("function not supported");
            soft.assertThat(ex.getCause()).isSameAs(cause);
        });
    }

    @Nested
    @DisplayName("When the unsupported function exception is tested")
    class WhenTheUnsupportedFunctionExceptionIsTested {
    }
}
