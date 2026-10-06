/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.vector.spi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("Named vector template bean")
class TemplateBeanTest {

    @Nested
    @DisplayName("When identifying passivation-capable template beans")
    class WhenTheIdentification {

        @Test
        @DisplayName("Should supply stable nonempty identifiers that distinguish providers")
        void shouldIdentifyProviders() {
            var first = new TemplateBean("first");
            var same = new TemplateBean("first");
            var second = new TemplateBean("second");

            assertSoftly(softly -> {
                softly.assertThat(first.getId()).as("bean identifier").isNotBlank();
                softly.assertThat(same.getId()).as("stable identifier").isEqualTo(first.getId());
                softly.assertThat(second.getId()).as("distinct provider identifier").isNotEqualTo(first.getId());
            });
        }
    }
}
