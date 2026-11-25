/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.data;

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryPath;
import org.eclipse.jnosql.communication.query.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("QueryCondition operands")
class QueryConditionOperandTest {

    private final SelectParser parser = new SelectParser();

    private QueryCondition where(String predicate) {
        return parser.apply("FROM Person WHERE " + predicate, "Person").where().orElseThrow().condition();
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "name = 'Otavio'|PATH|STRING|false|false",
            "name = :name|PATH|PARAMETER|false|false",
            "name = ?1|PATH|PARAMETER|false|false",
            "name = UPPER(:name)|PATH|FUNCTION|false|false",
            "UPPER(name) = 'OTAVIO'|FUNCTION|STRING|true|false",
            "UPPER(name) = UPPER(:name)|FUNCTION|FUNCTION|true|false",
            "name = UPPER(otherName)|PATH|FUNCTION|false|true",
            "UPPER(firstName) = UPPER(lastName)|FUNCTION|FUNCTION|true|true",
            "ABS(balance) > 100|FUNCTION|NUMBER|true|false",
            "LEFT(name, LENGTH(:prefix)) = :prefix|FUNCTION|PARAMETER|true|false",
            "name = 'name'|PATH|STRING|false|false",
            "name = UPPER('name')|PATH|FUNCTION|false|false",
            "name = UPPER(name)|PATH|FUNCTION|false|true"
    })
    @DisplayName("should represent both operands by type")
    void shouldRepresentOperands(String predicate, ValueType left, ValueType right,
                                 boolean leftNative, boolean rightNative) {
        var condition = where(predicate);
        assertThat(condition.leftOperand().type()).isEqualTo(left);
        assertThat(condition.rightOperand().type()).isEqualTo(right);
        if (left == ValueType.FUNCTION) {
            assertThat(((FunctionQueryValue) condition.leftOperand()).get().isNative()).isEqualTo(leftNative);
        }
        if (right == ValueType.FUNCTION) {
            assertThat(((FunctionQueryValue) condition.rightOperand()).get().isNative()).isEqualTo(rightNative);
        }
    }

    @Test
    @DisplayName("should keep argument order and nesting in a left function")
    void shouldKeepArgumentOrder() {
        var function = ((FunctionQueryValue) where("LEFT(name, LENGTH(:prefix)) = :prefix").leftOperand()).get();
        assertThat(function.name()).isEqualTo("LEFT");
        assertThat(function.params()[0]).isEqualTo(QueryPath.of("name"));
        var nested = ((FunctionQueryValue) function.params()[1]).get();
        assertThat(nested.name()).isEqualTo("LENGTH");
        assertThat(nested.isNative()).isFalse();
        assertThat(function.isNative()).isTrue();
    }

    @Test
    @DisplayName("should not let name() masquerade a function as an attribute")
    void shouldFailNameForFunction() {
        var condition = where("UPPER(name) = 'OTAVIO'");
        assertThatThrownBy(condition::name).isInstanceOf(UnsupportedOperationException.class);
        assertThat(where("name = 'a'").name()).isEqualTo("name");
        assertThat(where("name = 'a'").value()).isEqualTo(where("name = 'a'").rightOperand());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "name = 'a' AND age = 1|AND", "name = 'a' OR age = 1|OR", "NOT name = 'a'|NOT",
            "(name = 'a' OR age = 1) AND active = TRUE|AND"
    })
    @DisplayName("should keep logical predicates without a left operand")
    void shouldKeepLogicalPredicates(String predicate, Condition operator) {
        var condition = where(predicate);
        assertThat(condition.condition()).isEqualTo(operator);
        assertThat(condition.rightOperand()).isInstanceOf(ConditionQueryValue.class);
        assertThatThrownBy(condition::leftOperand).isInstanceOf(UnsupportedOperationException.class);
        assertThat(condition.name()).isEqualTo("_" + operator.name());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "age BETWEEN 1 AND 3|BETWEEN|ARRAY", "age IN (1, 2)|IN|ARRAY", "name LIKE 'a%'|LIKE|STRING",
            "name IS NULL|EQUALS|NULL", "age > 1|GREATER_THAN|NUMBER", "age <= 1|LESSER_EQUALS_THAN|NUMBER"
    })
    @DisplayName("should keep unary and compound predicates")
    void shouldKeepOtherPredicates(String predicate, Condition operator, ValueType right) {
        var condition = where(predicate);
        assertThat(condition.condition()).isEqualTo(operator);
        assertThat(condition.leftOperand()).isInstanceOf(QueryPath.class);
        assertThat(condition.rightOperand().type()).isEqualTo(right);
    }

    @Test
    @DisplayName("should keep a left function under NOT and nested parentheses")
    void shouldKeepFunctionInsideLogical() {
        var not = where("NOT (age = 1 OR (ABS(balance) > 100 AND name = 'a'))");
        var or = ((ConditionQueryValue) not.rightOperand()).get().get(0);
        assertThat(or.condition()).isEqualTo(Condition.OR);
        var and = ((ConditionQueryValue) or.rightOperand()).get().get(1);
        assertThat(and.condition()).isEqualTo(Condition.AND);
        assertThat(((ConditionQueryValue) and.rightOperand()).get().get(0).leftOperand())
                .isInstanceOf(FunctionQueryValue.class);
    }
}
