/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.method;

import org.eclipse.jnosql.communication.query.SelectQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SelectMethodProviderTest {

    @Nested
    @DisplayName("When the select method provider is used")
    class WhenTheSelectMethodProvider {
    }


    @Test
    @DisplayName("Should Create From Provider")
    void shouldCreateFromProvider() {
        Method method = PersonRepository.class.getDeclaredMethods()[0];
        SelectQuery query = SelectMethodProvider.INSTANCE.apply(method, "Person");
        assertThat(query).isNotNull();
        assertThat(query.entity()).isEqualTo("Person");
    }


    interface PersonRepository{
        List<String> findByAge(Integer age);
    }
}