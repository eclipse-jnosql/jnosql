/*
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
 */
package org.eclipse.jnosql.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class IdNotFoundExceptionTest {

    @Nested
    @DisplayName("When the exception is created")
    class WhenTheExceptionIsCreated {

        @Test
        @DisplayName("Should preserve the provided message")
        void shouldPreserveTheProvidedMessage() {
            String errorMessage = "Test error message";
            IdNotFoundException exception = new IdNotFoundException(errorMessage);

            assertThat(exception.getMessage()).isEqualTo(errorMessage);
        }
    }

    @Nested
    @DisplayName("When the exception factory is used")
    class WhenTheExceptionFactoryIsUsed {

        @Test
        @DisplayName("Should create a message with the entity class name")
        void shouldCreateMessageWithTheEntityClassName() {
            Class<?> entityType = MyClass.class;
            String expectedMessage = "The entity " + entityType.getName() + " must have a field annotated with @Id";
            IdNotFoundException exception = IdNotFoundException.newInstance(entityType);

            assertThat(exception.getMessage()).isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("Should provide a reusable missing key exception supplier")
        void shouldProvideAReusableMissingKeyExceptionSupplier() {
            Supplier<IdNotFoundException> supplier = IdNotFoundException.KEY_NOT_FOUND_EXCEPTION_SUPPLIER;
            IdNotFoundException exception = supplier.get();

            assertSoftly(softly -> {
                softly.assertThat(supplier).isNotNull();
                softly.assertThat(exception.getMessage())
                        .isEqualTo("To use this resource you must annotated a field with @Id");
            });
        }
    }

    private static class MyClass {
    }
}