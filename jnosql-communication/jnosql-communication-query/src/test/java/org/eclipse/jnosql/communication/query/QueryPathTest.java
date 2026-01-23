/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("QueryPath")
class QueryPathTest {

    @Test
    @DisplayName("should store and expose the path expression")
    void shouldExposePathExpression() {
        QueryPath path = QueryPath.of("floorOfSquareRoot");

        assertThat(path.get())
                .isEqualTo("floorOfSquareRoot");
    }

    @Test
    @DisplayName("should implement the QueryOperand contract")
    void shouldImplementQueryOperandContract() {
        QueryPath path = QueryPath.of("numBitsRequired");

        assertThat(path)
                .isInstanceOf(QueryValue.class);
    }

    @Test
    @DisplayName("factory method should create an equivalent instance")
    void factoryMethodShouldCreateEquivalentInstance() {
        QueryPath fromFactory = QueryPath.of("id");
        QueryPath fromConstructor = new QueryPath("id");

        assertThat(fromFactory)
                .isEqualTo(fromConstructor);
    }
}