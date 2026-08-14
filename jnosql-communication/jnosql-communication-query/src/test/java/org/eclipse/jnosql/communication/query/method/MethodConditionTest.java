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

import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.BooleanQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodConditionTest {

    @Nested
    @DisplayName("When the method condition is used")
    class WhenTheMethodCondition {
    }
    private QueryValue<Boolean> queryValue;

    @BeforeEach
    public void setUp() {
        this.queryValue = BooleanQueryValue.TRUE;
    }

    @Test
    @DisplayName("Should Create Condition")
    void shouldCreateCondition() {
        QueryCondition condition = new MethodCondition("active", Condition.EQUALS, queryValue);
        assertThat(condition).isNotNull();
        assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
        assertThat(condition.name()).isEqualTo("active");
        assertThat(condition.value()).isEqualTo(queryValue);
    }

    @Test
    @DisplayName("Should Equals")
    void shouldEquals() {
        QueryCondition condition = new MethodCondition("active", Condition.EQUALS, queryValue);
        QueryCondition conditionB = new MethodCondition("active", Condition.EQUALS, queryValue);
        assertThat(conditionB).isEqualTo(condition);
    }

    @Test
    @DisplayName("Should Hash Code")
    void shouldHashCode() {
        QueryCondition condition = new MethodCondition("active", Condition.EQUALS, queryValue);
        QueryCondition conditionB = new MethodCondition("active", Condition.EQUALS, queryValue);
        assertThat(conditionB).hasSameHashCodeAs(condition);
    }

    @Test
    @DisplayName("Should Create With Query Param")
    void shouldCreateWithQueryParam(){
        QueryCondition condition = new MethodCondition("active", Condition.EQUALS);
        assertThat(condition).isNotNull();
        assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
        assertThat(condition.name()).isEqualTo("active");
        Assertions.assertThat(condition.value()).isInstanceOf(MethodParamQueryValue.class);
    }
}
