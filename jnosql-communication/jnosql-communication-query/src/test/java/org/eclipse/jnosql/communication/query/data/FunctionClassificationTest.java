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
import org.eclipse.jnosql.communication.query.Function;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryPath;
import org.eclipse.jnosql.communication.query.UpdateQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Function classification and representation")
class FunctionClassificationTest {

    private final SelectParser parser = new SelectParser();

    private Function rightFunction(String expression) {
        QueryCondition condition = parser.apply("FROM Person WHERE name = " + expression, "Person")
                .where().orElseThrow().condition();
        return ((FunctionQueryValue) condition.value()).get();
    }

    @ParameterizedTest(name = "{0} native={1}")
    @CsvSource(delimiter = '|', value = {
            "UPPER('otavio')|false",
            "UPPER(:name)|false",
            "UPPER(?1)|false",
            "ABS(-10)|false",
            "UPPER(LEFT(:name, 19))|false",
            "UPPER(otherName)|true",
            "LEFT(otherName, 3)|true",
            "LEFT(otherName, :length)|true",
            "UPPER(LOWER(otherName))|true",
            "LEFT(otherName, LENGTH(:prefix))|true",
            "LEFT(:name, LENGTH(otherName))|true",
            "upper(LeFt(:name, 19))|false"
    })
    @DisplayName("should classify recursively without evaluating")
    void shouldClassify(String expression, boolean nativeFunction) {
        assertThat(rightFunction(expression).isNative()).isEqualTo(nativeFunction);
    }

    @ParameterizedTest
    @CsvSource({"UPPER", "upper", "Upper", "UpPeR"})
    @DisplayName("should normalize function names regardless of case")
    void shouldBeCaseInsensitive(String name) {
        assertThat(rightFunction(name + "('otavio')").name()).isEqualTo("UPPER");
    }

    @Test
    @DisplayName("should keep distinct representations for literal, parameter and attribute")
    void shouldDistinguishArguments() {
        assertThat(rightFunction("UPPER('name')").params()[0])
                .isInstanceOf(org.eclipse.jnosql.communication.query.StringQueryValue.class);
        assertThat(rightFunction("UPPER(:name)").params()[0]).isInstanceOf(org.eclipse.jnosql.communication.query.ParamQueryValue.class);
        assertThat(rightFunction("UPPER(?1)").params()[0]).isInstanceOf(org.eclipse.jnosql.communication.query.ParamQueryValue.class);
        assertThat(rightFunction("UPPER(name)").params()[0]).isInstanceOf(QueryPath.class);
    }

    @Test
    @DisplayName("should accept function names as parameter names")
    void shouldAcceptKeywordParameterNames() {
        var function = rightFunction("LEFT(:name, :length)");
        assertThat(((org.eclipse.jnosql.communication.query.ParamQueryValue) function.params()[1]).get())
                .isEqualTo("length");
    }

    @Test
    @DisplayName("should keep a left function structured")
    void shouldKeepLeftFunctionStructured() {
        var condition = parser.apply("FROM Person WHERE UPPER(name) = UPPER(LEFT(:name, 19))", "Person")
                .where().orElseThrow().condition();
        assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
        var left = ((FunctionQueryValue) condition.leftOperand()).get();
        assertThat(left.name()).isEqualTo("UPPER");
        assertThat(left.isNative()).isTrue();
        assertThat(left.params()[0]).isEqualTo(QueryPath.of("name"));
        assertThat(((FunctionQueryValue) condition.value()).get().isNative()).isFalse();
    }

    @Test
    @DisplayName("should not mark ordinary attributes as function operands")
    void shouldNotMarkAttributes() {
        var condition = parser.apply("FROM Person WHERE name = 'a'", "Person").where().orElseThrow().condition();
        assertThat(condition.leftOperand()).isEqualTo(QueryPath.of("name"));
    }

    @Test
    @DisplayName("should keep left functions under boolean operators and negation")
    void shouldKeepLeftFunctionInBooleans() {
        var condition = parser.apply("FROM Person WHERE NOT (age = 1 OR ABS(balance) > 100)", "Person")
                .where().orElseThrow().condition();
        var or = ((org.eclipse.jnosql.communication.query.ConditionQueryValue) condition.value()).get().getFirst();
        var operands = ((org.eclipse.jnosql.communication.query.ConditionQueryValue) or.value()).get();
        assertThat(operands.get(1).leftOperand()).isInstanceOf(FunctionQueryValue.class);
        assertThat(operands.get(1).condition()).isEqualTo(Condition.GREATER_THAN);
    }

    @Test
    @DisplayName("should parse functions in UPDATE SET")
    void shouldParseUpdateSet() {
        UpdateQuery query = new UpdateParser().apply("UPDATE Person SET name = upper(LEFT(:name, 19)) WHERE id = :id");
        var function = ((FunctionQueryValue) query.set().getFirst().value()).get();
        assertThat(function.name()).isEqualTo("UPPER");
        assertThat(function.isNative()).isFalse();
    }
}
