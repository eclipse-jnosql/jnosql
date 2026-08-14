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

import org.eclipse.jnosql.communication.query.DeleteQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class DeleteMethodProviderTest {

    @Nested
    @DisplayName("When the delete method provider is used")
    class WhenTheDeleteMethodProvider {
    }

    @Test
    @DisplayName("Should Create")
    void shouldCreate() {
        Method method = PersonRepository.class.getDeclaredMethods()[0];
        DeleteQuery query = DeleteMethodProvider.INSTANCE.apply(method, "Person");
        assertThat(query).isNotNull();
        assertThat(query.entity()).isEqualTo("Person");
    }

    interface PersonRepository{
        void deleteByAge(Integer age);
    }
}