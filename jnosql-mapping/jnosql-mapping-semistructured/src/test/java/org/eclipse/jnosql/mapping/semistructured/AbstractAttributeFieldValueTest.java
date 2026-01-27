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
 *   Michele Rastelli
 */
package org.eclipse.jnosql.mapping.semistructured;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.metadata.FieldValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AbstractAttributeFieldValueTest {

    @Test
    @DisplayName("isNotEmpty should return true when value is not null")
    void shouldReturnTrueWhenValueIsNotNull() {
        FieldValue fieldMetadata = Mockito.mock(FieldValue.class);
        Mockito.when(fieldMetadata.isNotEmpty()).thenReturn(true);

        FieldValue fieldValue = new AbstractAttributeFieldValue(fieldMetadata) {
            @Override
            protected String name() {
                return "";
            }

            @Override
            public Object value() {
                return "Ada";
            }
        };

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(fieldValue.isNotEmpty()).isTrue();
        });
    }

    @Test
    @DisplayName("isNotEmpty should return false when value is null")
    void shouldReturnFalseWhenValueIsNull() {
        FieldValue fieldMetadata = Mockito.mock(FieldValue.class);

        FieldValue fieldValue = new AbstractAttributeFieldValue(fieldMetadata) {
            @Override
            protected String name() {
                return "";
            }

            @Override
            public Object value() {
                return "Ada";
            }
        };
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(fieldValue.isNotEmpty()).isFalse();
        });
    }

    @Test
    @DisplayName("toString should include field name and value")
    void shouldGenerateToStringWithFieldNameAndValue() {
        FieldValue fieldMetadata = Mockito.mock(FieldValue.class);

        FieldValue fieldValue = new AbstractAttributeFieldValue(fieldMetadata) {
            @Override
            protected String name() {
                return "name";
            }

            @Override
            public Object value() {
                return "Ada";
            }
        };

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(fieldValue.toString())
                    .contains("fieldValue");
        });
    }
}