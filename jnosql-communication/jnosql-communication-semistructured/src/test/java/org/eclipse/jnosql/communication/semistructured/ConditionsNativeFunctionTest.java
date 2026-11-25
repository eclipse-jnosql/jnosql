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
package org.eclipse.jnosql.communication.semistructured;

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.function.FunctionArgument;
import org.eclipse.jnosql.communication.function.NativeFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("Conditions with native functions")
class ConditionsNativeFunctionTest {

    private static final CommunicationObserverParser MAPPING = new CommunicationObserverParser() {
        @Override
        public String fireConditionField(String entity, String field) {
            return switch (field) {
                case "name" -> "person_name";
                case "address.city" -> "address.city_name";
                default -> field;
            };
        }
    };

    private DatabaseManager manager;

    @BeforeEach
    void setUp() {
        manager = Mockito.mock(DatabaseManager.class);
    }

    private CommunicationPreparedStatement select(String query) {
        return select(query, CommunicationObserverParser.EMPTY);
    }

    private CommunicationPreparedStatement select(String query, CommunicationObserverParser observer) {
        return new SelectQueryParser().prepare(query, null, manager, observer);
    }

    private CriteriaCondition lastSelectCondition() {
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(manager, Mockito.atLeastOnce()).select(captor.capture());
        return captor.getAllValues().getLast().condition().orElseThrow();
    }

    private static NativeFunction function(CriteriaOperand operand) {
        return ((CriteriaOperand.Function) operand).function();
    }

    private static Object constant(CriteriaOperand operand) {
        return ((CriteriaOperand.Constant) operand).value().get();
    }

    private static Object valueArgument(FunctionArgument argument) {
        return ((FunctionArgument.ValueArgument) argument).value().get();
    }

    @SuppressWarnings("unchecked")
    private static List<CriteriaCondition> children(CriteriaCondition condition) {
        return (List<CriteriaCondition>) condition.element().value().get();
    }

    @Nested
    @DisplayName("When converting a native function predicate")
    class WhenTheConversion {

        @Test
        @DisplayName("Should keep the native function on the left against a value")
        void shouldKeepLeftFunction() {

            // Given
            var statement = select("FROM Person WHERE UPPER(name) = 'OTAVIO'");

            // When
            statement.result();

            // Then
            var condition = lastSelectCondition();
            assertSoftly(softly -> {
                softly.assertThat(condition.condition()).as("condition").isEqualTo(Condition.EQUALS);
                softly.assertThat(condition.isNative()).as("native").isTrue();
                softly.assertThat(function(condition.leftOperand()).name()).as("function").isEqualTo("UPPER");
                softly.assertThat(function(condition.leftOperand()).arguments()).as("arguments")
                        .containsExactly(new FunctionArgument.AttributePath("name"));
                softly.assertThat(constant(condition.rightOperand())).as("value").isEqualTo("OTAVIO");
            });
        }

        @Test
        @DisplayName("Should expose an evaluable function on the right as an ordinary value")
        void shouldExposeEvaluableAsValue() {

            // Given
            var statement = select("FROM Person WHERE UPPER(name) = UPPER(:name)").bind("name", "otavio");

            // When
            statement.result();

            // Then
            var condition = lastSelectCondition();
            assertSoftly(softly -> {
                softly.assertThat(function(condition.leftOperand()).name()).as("function").isEqualTo("UPPER");
                softly.assertThat(condition.rightOperand()).as("operand")
                        .isInstanceOf(CriteriaOperand.Constant.class);
                softly.assertThat(constant(condition.rightOperand())).as("value").isEqualTo("OTAVIO");
            });
        }

        @Test
        @DisplayName("Should keep the native function on the right against an attribute")
        void shouldKeepRightFunction() {

            // Given
            var statement = select("FROM Person WHERE name = UPPER(otherName)");

            // When
            statement.result();

            // Then
            var condition = lastSelectCondition();
            assertSoftly(softly -> {
                softly.assertThat(condition.leftOperand()).as("left")
                        .isEqualTo(new CriteriaOperand.Attribute("name"));
                softly.assertThat(function(condition.rightOperand()).arguments()).as("arguments")
                        .containsExactly(new FunctionArgument.AttributePath("otherName"));
            });
        }

        @Test
        @DisplayName("Should keep native functions on both operands")
        void shouldKeepBothFunctions() {

            // Given
            var statement = select("FROM Person WHERE UPPER(firstName) = UPPER(lastName)");

            // When
            statement.result();

            // Then
            var condition = lastSelectCondition();
            assertSoftly(softly -> {
                softly.assertThat(function(condition.leftOperand()).arguments()).as("left")
                        .containsExactly(new FunctionArgument.AttributePath("firstName"));
                softly.assertThat(function(condition.rightOperand()).arguments()).as("right")
                        .containsExactly(new FunctionArgument.AttributePath("lastName"));
            });
        }

        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', value = {
                "UPPER(name) = 'A'|UPPER",
                "LOWER(name) = 'a'|LOWER",
                "LEFT(name, 2) = 'a'|LEFT",
                "RIGHT(name, 2) = 'a'|RIGHT",
                "LENGTH(name) = 3|LENGTH",
                "ABS(balance) = 3|ABS"
        })
        @DisplayName("Should keep every standard native function")
        void shouldKeepStandardFunctions(String predicate, String name) {

            // Given
            var statement = select("FROM Person WHERE " + predicate);

            // When
            statement.result();

            // Then
            assertThat(function(lastSelectCondition().leftOperand()).name()).isEqualTo(name);
        }

        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', value = {
                "UPPER(name) = 'A'|EQUALS",
                "ABS(balance) > 100|GREATER_THAN",
                "ABS(balance) >= 100|GREATER_EQUALS_THAN",
                "ABS(balance) < 100|LESSER_THAN",
                "ABS(balance) <= 100|LESSER_EQUALS_THAN",
                "LOWER(name) LIKE 'ota%'|LIKE",
                "ABS(balance) BETWEEN 100 AND 500|BETWEEN"
        })
        @DisplayName("Should keep the operator of the predicate")
        void shouldKeepOperator(String predicate, Condition expected) {

            // Given
            var statement = select("FROM Person WHERE " + predicate);

            // When
            statement.result();

            // Then
            var condition = lastSelectCondition();
            assertSoftly(softly -> {
                softly.assertThat(condition.condition()).as("condition").isEqualTo(expected);
                softly.assertThat(condition.leftOperand()).as("left")
                        .isInstanceOf(CriteriaOperand.Function.class);
                softly.assertThat(condition.rightOperand()).as("right")
                        .isInstanceOf(CriteriaOperand.Constant.class);
            });
        }

        @Test
        @DisplayName("Should keep the negation of a native predicate")
        void shouldKeepNegation() {

            // Given
            var statement = select("FROM Person WHERE UPPER(name) <> 'A'");

            // When
            statement.result();

            // Then
            var condition = lastSelectCondition();
            var negated = condition.element().get(CriteriaCondition.class);
            assertSoftly(softly -> {
                softly.assertThat(condition.condition()).as("outer").isEqualTo(Condition.NOT);
                softly.assertThat(negated.condition()).as("inner").isEqualTo(Condition.EQUALS);
                softly.assertThat(negated.isNative()).as("native").isTrue();
            });
        }

        @Test
        @DisplayName("Should reject a native function with an invalid structure when preparing")
        void shouldRejectInvalidStructure() {
            assertThatThrownBy(() -> select("FROM Person WHERE LEFT(name) = 'a'"))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("LEFT");
        }

        @Test
        @DisplayName("Should reject a nested evaluable function whose result type does not fit the argument")
        void shouldRejectIncompatibleNestedResult() {
            assertThatThrownBy(() -> select("FROM Person WHERE LEFT(name, UPPER(:prefix)) = 'a'"))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("UPPER");
        }

        @Test
        @DisplayName("Should reject an unsupported argument kind in a native function")
        void shouldRejectUnsupportedArgument() {
            assertThatThrownBy(() -> select("FROM Person WHERE LEFT(name, LOCAL DATE) = 'a'").result())
                    .isInstanceOf(QueryException.class);
        }

        @Test
        @DisplayName("Should reject an evaluable function against an attribute on the left")
        void shouldRejectEvaluableOnTheLeft() {
            assertThatThrownBy(() -> select("FROM Person WHERE UPPER('a') = name").result())
                    .isInstanceOf(QueryException.class).hasMessageContaining("left operand");
        }

        @ParameterizedTest(name = "{0}")
        @CsvSource(delimiter = '|', value = {
                "UPPER(name) IN ('JAVA', 'JAKARTA')",
                "UPPER(name) IS NULL"
        })
        @DisplayName("Should reject predicates the grammar does not define for functions")
        void shouldRejectUnsupportedGrammar(String predicate) {
            assertThatThrownBy(() -> select("FROM Person WHERE " + predicate))
                    .isInstanceOf(QueryException.class);
        }
    }

    @Nested
    @DisplayName("When combining native predicates with Boolean operators")
    class WhenTheBooleanComposition {

        @Test
        @DisplayName("Should keep the grouping of parentheses")
        void shouldKeepParentheses() {

            // Given
            var statement = select(
                    "FROM Person WHERE UPPER(name) = 'OTAVIO' AND (ABS(balance) > 100 OR active = TRUE)");

            // When
            statement.result();

            // Then
            var and = lastSelectCondition();
            var branches = children(and);
            var or = children(branches.get(1));
            assertSoftly(softly -> {
                softly.assertThat(and.condition()).as("root").isEqualTo(Condition.AND);
                softly.assertThat(branches.get(0).isNative()).as("first branch").isTrue();
                softly.assertThat(branches.get(1).condition()).as("second branch").isEqualTo(Condition.OR);
                softly.assertThat(or.get(0).isNative()).as("native in OR").isTrue();
                softly.assertThat(or.get(1).element().name()).as("ordinary in OR").isEqualTo("active");
            });
        }

        @Test
        @DisplayName("Should keep the precedence of NOT, AND and OR")
        void shouldKeepPrecedence() {

            // Given
            var statement = select("FROM Person WHERE NOT UPPER(name) = 'A' OR LENGTH(name) = 3 AND active = TRUE");

            // When
            statement.result();

            // Then
            var root = lastSelectCondition();
            assertSoftly(softly -> {
                softly.assertThat(root.condition()).as("root").isEqualTo(Condition.OR);
                softly.assertThat(children(root).get(0).condition()).as("first").isEqualTo(Condition.NOT);
                softly.assertThat(children(root).get(1).condition()).as("second").isEqualTo(Condition.AND);
            });
        }
    }

    @Nested
    @DisplayName("When evaluating the arguments of a native function")
    class WhenThePartialEvaluation {

        @Test
        @DisplayName("Should evaluate the independent argument and keep the stored attribute")
        void shouldEvaluateIndependentArgument() {

            // Given
            var statement = select("FROM Person WHERE LEFT(name, LENGTH(:prefix)) = :prefix")
                    .bind("prefix", "OTA");

            // When
            statement.result();

            // Then
            var condition = lastSelectCondition();
            var left = function(condition.leftOperand());
            assertSoftly(softly -> {
                softly.assertThat(left.name()).as("function").isEqualTo("LEFT");
                softly.assertThat(left.arguments().get(0)).as("attribute")
                        .isEqualTo(new FunctionArgument.AttributePath("name"));
                softly.assertThat(valueArgument(left.arguments().get(1))).as("length").isEqualTo(3);
                softly.assertThat(constant(condition.rightOperand())).as("value").isEqualTo("OTA");
            });
        }

        @Test
        @DisplayName("Should keep nested native functions structured")
        void shouldKeepNestedStructure() {

            // Given
            var statement = select("FROM Person WHERE UPPER(LEFT(name, ABS(:length))) = :name")
                    .bind("length", -4).bind("name", "JAVA");

            // When
            statement.result();

            // Then
            var condition = lastSelectCondition();
            var upper = function(condition.leftOperand());
            var left = ((FunctionArgument.NativeArgument) upper.arguments().getFirst()).function();
            assertSoftly(softly -> {
                softly.assertThat(upper.name()).as("outer").isEqualTo("UPPER");
                softly.assertThat(left.name()).as("inner").isEqualTo("LEFT");
                softly.assertThat(left.arguments().getFirst()).as("attribute")
                        .isEqualTo(new FunctionArgument.AttributePath("name"));
                softly.assertThat(valueArgument(left.arguments().get(1))).as("length").isEqualTo(4);
                softly.assertThat(constant(condition.rightOperand())).as("value").isEqualTo("JAVA");
            });
        }

        @Test
        @DisplayName("Should not evaluate anything when preparing or binding")
        void shouldNotEvaluateAtBinding() {

            // Given
            var statement = select("FROM Person WHERE LEFT(name, LENGTH(:prefix)) = :prefix");

            // When
            statement.bind("prefix", "x");

            // Then
            Mockito.verifyNoInteractions(manager);
        }

        @Test
        @DisplayName("Should fail when a parameter used by the function is not bound")
        void shouldFailWhenParameterIsNotBound() {

            // Given
            var statement = select("FROM Person WHERE LEFT(name, LENGTH(:prefix)) = :prefix");

            // When / Then
            assertThatThrownBy(statement::result).isInstanceOf(QueryException.class);
        }
    }

    @Nested
    @DisplayName("When mapping the entity attributes of a native function")
    class WhenTheFieldMapping {

        @Test
        @DisplayName("Should map only attribute references and keep literals")
        void shouldMapOnlyAttributes() {

            // Given
            var statement = select("FROM Person WHERE UPPER(name) = UPPER('name')", MAPPING);

            // When
            statement.result();

            // Then
            var condition = lastSelectCondition();
            assertSoftly(softly -> {
                softly.assertThat(function(condition.leftOperand()).arguments()).as("attribute")
                        .containsExactly(new FunctionArgument.AttributePath("person_name"));
                softly.assertThat(constant(condition.rightOperand())).as("literal").isEqualTo("NAME");
            });
        }

        @Test
        @DisplayName("Should keep a parameter whose value matches an attribute name")
        void shouldKeepParameterValue() {

            // Given
            var statement = select("FROM Person WHERE LEFT(name, LENGTH(:name)) = :name", MAPPING)
                    .bind("name", "name");

            // When
            statement.result();

            // Then
            var condition = lastSelectCondition();
            var left = function(condition.leftOperand());
            assertSoftly(softly -> {
                softly.assertThat(left.arguments().getFirst()).as("attribute")
                        .isEqualTo(new FunctionArgument.AttributePath("person_name"));
                softly.assertThat(valueArgument(left.arguments().get(1))).as("length").isEqualTo(4);
                softly.assertThat(constant(condition.rightOperand())).as("parameter").isEqualTo("name");
            });
        }

        @Test
        @DisplayName("Should map a nested attribute path on the right operand")
        void shouldMapNestedPath() {

            // Given
            var statement = select("FROM Person WHERE name = UPPER(address.city)", MAPPING);

            // When
            statement.result();

            // Then
            var condition = lastSelectCondition();
            assertSoftly(softly -> {
                softly.assertThat(condition.leftOperand()).as("left")
                        .isEqualTo(new CriteriaOperand.Attribute("person_name"));
                softly.assertThat(function(condition.rightOperand()).arguments()).as("right")
                        .containsExactly(new FunctionArgument.AttributePath("address.city_name"));
            });
        }

        @Test
        @DisplayName("Should map the attribute of a nested native function")
        void shouldMapNestedFunction() {

            // Given
            var statement = select("FROM Person WHERE UPPER(LEFT(name, 2)) = 'OT'", MAPPING);

            // When
            statement.result();

            // Then
            var inner = ((FunctionArgument.NativeArgument) function(lastSelectCondition().leftOperand())
                    .arguments().getFirst()).function();
            assertSoftly(softly -> {
                softly.assertThat(inner.arguments().getFirst()).as("attribute")
                        .isEqualTo(new FunctionArgument.AttributePath("person_name"));
                softly.assertThat(inner.arguments().get(1)).as("literal")
                        .isEqualTo(new FunctionArgument.Literal(2));
            });
        }
    }

    @Nested
    @DisplayName("When executing a prepared statement with a native function")
    class WhenTheExecution {

        @Test
        @DisplayName("Should use the current bindings in each execution")
        void shouldUseCurrentBindings() {

            // Given
            var statement = select("FROM Person WHERE LEFT(name, LENGTH(:prefix)) = UPPER(:prefix)");

            // When
            statement.bind("prefix", "ota").result();
            var first = lastSelectCondition();
            statement.bind("prefix", "jakarta").result();
            var second = lastSelectCondition();
            statement.bind("prefix", "java").result();
            var third = lastSelectCondition();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(valueArgument(function(first.leftOperand()).arguments().get(1)))
                        .as("first length").isEqualTo(3);
                softly.assertThat(constant(first.rightOperand())).as("first value").isEqualTo("OTA");
                softly.assertThat(valueArgument(function(second.leftOperand()).arguments().get(1)))
                        .as("second length").isEqualTo(7);
                softly.assertThat(constant(second.rightOperand())).as("second value").isEqualTo("JAKARTA");
                softly.assertThat(valueArgument(function(third.leftOperand()).arguments().get(1)))
                        .as("third length").isEqualTo(4);
                softly.assertThat(constant(third.rightOperand())).as("third value").isEqualTo("JAVA");
            });
        }

        @Test
        @DisplayName("Should capture a parameter bound before the execution as a plain value")
        void shouldCaptureBoundParameter() {

            // Given
            var statement = select("FROM Person WHERE LEFT(name, :size) = 'ot'").bind("size", 2);

            // When
            statement.result();

            // Then
            assertThat(valueArgument(function(lastSelectCondition().leftOperand()).arguments().get(1)))
                    .isEqualTo(2);
        }

        @Test
        @DisplayName("Should not observe a later binding in a query already delivered")
        void shouldIsolateDelayedConsumption() {

            // Given
            var statement = select("FROM Person WHERE LEFT(name, :size) = 'ot'");
            statement.bind("size", 2).result();
            var first = lastSelectCondition();

            // When
            statement.bind("size", 5).result();

            // Then
            assertSoftly(softly -> {
                softly.assertThat(valueArgument(function(first.leftOperand()).arguments().get(1)))
                        .as("first").isEqualTo(2);
                softly.assertThat(valueArgument(function(lastSelectCondition().leftOperand()).arguments().get(1)))
                        .as("second").isEqualTo(5);
            });
        }

        @Test
        @DisplayName("Should capture a bound parameter used as the constant operand")
        void shouldIsolateConstantOperand() {

            // Given
            var statement = select("FROM Person WHERE LEFT(name, LENGTH(:prefix)) = :prefix");
            statement.bind("prefix", "ota").result();
            var first = lastSelectCondition();

            // When
            statement.bind("prefix", "jakarta").result();

            // Then
            assertThat(constant(first.rightOperand())).isEqualTo("ota");
        }

        @Test
        @DisplayName("Should recover in a later execution after a failed one")
        void shouldRecoverAfterFailure() {

            // Given
            var statement = select("FROM Person WHERE LEFT(name, LENGTH(:prefix)) = :prefix");
            assertThatThrownBy(statement::result).isInstanceOf(QueryException.class);

            // When
            statement.bind("prefix", "ota").result();

            // Then
            assertThat(valueArgument(function(lastSelectCondition().leftOperand()).arguments().get(1)))
                    .isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("When running destructive and counting operations with a native function")
    class WhenTheOperationIsExecuted {

        @Test
        @DisplayName("Should deliver the complete native predicate to DELETE")
        void shouldDeliverToDelete() {

            // Given
            var statement = new DeleteQueryParser().prepare("DELETE FROM Person WHERE UPPER(name) = 'OTAVIO'",
                    manager, CommunicationObserverParser.EMPTY);

            // When
            statement.result();

            // Then
            var captor = ArgumentCaptor.forClass(DeleteQuery.class);
            Mockito.verify(manager).delete(captor.capture());
            assertThat(captor.getValue().condition().orElseThrow().isNative()).isTrue();
        }

        @Test
        @DisplayName("Should deliver the complete native predicate to UPDATE WHERE")
        void shouldDeliverToUpdate() {

            // Given
            var statement = new UpdateQueryParser().prepare(
                    "UPDATE Person SET active = TRUE WHERE UPPER(name) = 'OTAVIO'",
                    manager, CommunicationObserverParser.EMPTY);

            // When
            statement.result();

            // Then
            var captor = ArgumentCaptor.forClass(UpdateQuery.class);
            Mockito.verify(manager).update(captor.capture());
            assertThat(captor.getValue().where().orElseThrow().isNative()).isTrue();
        }

        @Test
        @DisplayName("Should deliver the complete native predicate to COUNT")
        void shouldDeliverToCount() {

            // Given
            var statement = select("SELECT COUNT(this) FROM Person WHERE UPPER(name) = 'OTAVIO'");

            // When
            statement.count();

            // Then
            var captor = ArgumentCaptor.forClass(SelectQuery.class);
            Mockito.verify(manager).count(captor.capture());
            assertThat(captor.getValue().condition().orElseThrow().isNative()).isTrue();
        }

        @Test
        @DisplayName("Should reject a native assignment without running anything")
        void shouldRejectNativeAssignment() {
            assertThatThrownBy(() -> new UpdateQueryParser().prepare(
                    "UPDATE Person SET name = UPPER(name) WHERE id = :id",
                    manager, CommunicationObserverParser.EMPTY))
                    .isInstanceOf(QueryException.class)
                    .hasMessageContaining("Native function");
            Mockito.verifyNoInteractions(manager);
        }
    }
}
