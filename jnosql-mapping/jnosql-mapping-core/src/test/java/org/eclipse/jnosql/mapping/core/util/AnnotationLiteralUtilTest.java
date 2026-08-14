/*
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
 */
package org.eclipse.jnosql.mapping.core.util;

import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;


class AnnotationLiteralUtilTest {





    @Nested
    @DisplayName("When the annotation literal util operates")
    class WhenTheAnnotationLiteralUtilOperates {

        @DisplayName("Should default annotation literal")
        @Test
        void shouldDefaultAnnotationLiteral() {
            AnnotationLiteral<Default> defaultAnnotation = AnnotationLiteralUtil.DEFAULT_ANNOTATION;
            assertThat(defaultAnnotation.annotationType()).isEqualTo(Default.class);
        }
        @DisplayName("Should any annotation literal")
        @Test
        void shouldAnyAnnotationLiteral() {
            AnnotationLiteral<Any> anyAnnotation = AnnotationLiteralUtil.ANY_ANNOTATION;

            assertThat(anyAnnotation.annotationType()).isEqualTo(Any.class);
        }
        @DisplayName("Should custom annotation literal")
        @Test
        void shouldCustomAnnotationLiteral() {
            AnnotationLiteral<Named> namedLiteral = NamedLiteral.of("test");

            assertThat(namedLiteral.annotationType()).isEqualTo(Named.class);
        }
    }
}
