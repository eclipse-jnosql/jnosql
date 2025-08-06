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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class ElementsTest {

    @Test
    void shouldCreateColumn() {
        Element element = Elements.of("name", "Ada");
        assertEquals("name", element.name());
        assertEquals("Ada", element.get());
    }

    @Test
    void shouldCreateColumnsFromMap() {
        Map<String, String> map = singletonMap("name", "Ada");
        List<Element> elements = Elements.of(map);
        assertFalse(elements.isEmpty());
        assertThat(elements).contains(Element.of("name", "Ada"));
    }


    @Test
    void shouldCreateRecursiveMap() {
        List<List<Map<String, String>>> list = new ArrayList<>();
        Map<String, String> map = singletonMap("mobile", "55 1234-4567");
        list.add(singletonList(map));

        List<Element> elements = Elements.of(singletonMap("contact", list));
        assertEquals(1, elements.size());
        Element element = elements.getFirst();
        assertEquals("contact", element.name());
        List<List<Element>> result = (List<List<Element>>) element.get();
        assertEquals(Element.of("mobile", "55 1234-4567"), result.getFirst().getFirst());

    }
}