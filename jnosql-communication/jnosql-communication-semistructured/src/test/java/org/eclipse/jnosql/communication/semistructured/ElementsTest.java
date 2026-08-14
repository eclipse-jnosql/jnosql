/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */

package org.eclipse.jnosql.communication.semistructured;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;


class ElementsTest {


    @Nested
    @DisplayName("When the elements is used")
    class WhenTheElementsIsUsed {

        @DisplayName("Should Create Column")
        @Test
        void shouldCreateColumn() {
            Element element = Elements.of("name", "Ada");
            assertThat(element.name()).isEqualTo("name");
            assertThat(element.get()).isEqualTo("Ada");
        }

        @DisplayName("Should Create Columns From Map")
        @Test
        void shouldCreateColumnsFromMap() {
            Map<String, String> map = singletonMap("name", "Ada");
            List<Element> elements = Elements.of(map);
            assertThat(elements.isEmpty()).isFalse();
            assertThat(elements).contains(Element.of("name", "Ada"));
        }


        @DisplayName("Should Create Recursive Map")
        @Test
        void shouldCreateRecursiveMap() {
            List<List<Map<String, String>>> list = new ArrayList<>();
            Map<String, String> map = singletonMap("mobile", "55 1234-4567");
            list.add(singletonList(map));

            List<Element> elements = Elements.of(singletonMap("contact", list));
            assertThat(elements.size()).isEqualTo(1);
            Element element = elements.getFirst();
            assertThat(element.name()).isEqualTo("contact");
            List<List<Element>> result = (List<List<Element>>) element.get();
            assertThat(result.getFirst().getFirst()).isEqualTo(Element.of("mobile", "55 1234-4567"));

        }
    }

}
