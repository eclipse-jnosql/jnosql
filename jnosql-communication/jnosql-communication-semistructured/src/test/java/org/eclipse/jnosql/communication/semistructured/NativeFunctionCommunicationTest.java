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
import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.function.EvaluableFunction;
import org.eclipse.jnosql.communication.function.FunctionArgument;
import org.eclipse.jnosql.communication.function.NativeFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Native function communication representation")
class NativeFunctionCommunicationTest {

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

    private static Value counting(AtomicInteger counter, Object result) {
        return new Value() {
            @Override
            public Object get() {
                counter.incrementAndGet();
                return result;
            }

            @Override
            public <T> T get(Class<T> type) {
                return type.cast(get());
            }

            @Override
            public <T> T get(TypeSupplier<T> supplier) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isInstanceOf(Class<?> type) {
                return type.isInstance(result);
            }

            @Override
            public boolean isNull() {
                return result == null;
            }
        };
    }

    @Test
    @DisplayName("should represent a native function on the left against a value")
    void shouldRepresentLeftFunction() {
        select("FROM Person WHERE UPPER(name) = 'OTAVIO'").result();
        var condition = lastSelectCondition();
        assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
        assertThat(condition.isNative()).isTrue();
        assertThat(function(condition.leftOperand()).name()).isEqualTo("UPPER");
        assertThat(function(condition.leftOperand()).arguments())
                .containsExactly(new FunctionArgument.AttributePath("name"));
        assertThat(condition.rightOperand()).isInstanceOf(CriteriaOperand.Constant.class);
        assertThat(constant(condition.rightOperand())).isEqualTo("OTAVIO");
    }

    @Test
    @DisplayName("should represent a native function against an evaluable function transparently")
    void shouldRepresentEvaluableOnTheRight() {
        select("FROM Person WHERE UPPER(name) = UPPER(:name)").bind("name", "otavio").result();
        var condition = lastSelectCondition();
        assertThat(function(condition.leftOperand()).name()).isEqualTo("UPPER");
        assertThat(condition.rightOperand()).isInstanceOf(CriteriaOperand.Constant.class);
        assertThat(constant(condition.rightOperand())).isEqualTo("OTAVIO");
    }

    @Test
    @DisplayName("should represent an attribute against a native function on the right")
    void shouldRepresentRightFunction() {
        select("FROM Person WHERE name = UPPER(otherName)").result();
        var condition = lastSelectCondition();
        assertThat(condition.leftOperand()).isEqualTo(new CriteriaOperand.Attribute("name"));
        assertThat(function(condition.rightOperand()).name()).isEqualTo("UPPER");
        assertThat(function(condition.rightOperand()).arguments())
                .containsExactly(new FunctionArgument.AttributePath("otherName"));
    }

    @Test
    @DisplayName("should represent native functions on both operands")
    void shouldRepresentBothFunctions() {
        select("FROM Person WHERE UPPER(firstName) = UPPER(lastName)").result();
        var condition = lastSelectCondition();
        assertThat(function(condition.leftOperand()).arguments())
                .containsExactly(new FunctionArgument.AttributePath("firstName"));
        assertThat(function(condition.rightOperand()).arguments())
                .containsExactly(new FunctionArgument.AttributePath("lastName"));
    }

    @Test
    @DisplayName("should evaluate independent arguments and keep the stored one for the database")
    void shouldPartiallyEvaluate() {
        select("FROM Person WHERE LEFT(name, LENGTH(:prefix)) = :prefix").bind("prefix", "OTA").result();
        var condition = lastSelectCondition();
        var left = function(condition.leftOperand());
        assertThat(left.name()).isEqualTo("LEFT");
        assertThat(left.arguments()).hasSize(2);
        assertThat(left.arguments().get(0)).isEqualTo(new FunctionArgument.AttributePath("name"));
        assertThat(valueArgument(left.arguments().get(1))).isEqualTo(3);
        assertThat(constant(condition.rightOperand())).isEqualTo("OTA");
    }

    @Test
    @DisplayName("should keep nested native functions structured with evaluable arguments resolved")
    void shouldKeepNestedNativeStructure() {
        select("FROM Person WHERE UPPER(LEFT(name, ABS(:length))) = :name")
                .bind("length", -4).bind("name", "JAVA").result();
        var condition = lastSelectCondition();
        var upper = function(condition.leftOperand());
        assertThat(upper.name()).isEqualTo("UPPER");
        var left = ((FunctionArgument.NativeArgument) upper.arguments().getFirst()).function();
        assertThat(left.name()).isEqualTo("LEFT");
        assertThat(left.arguments().getFirst()).isEqualTo(new FunctionArgument.AttributePath("name"));
        assertThat(valueArgument(left.arguments().get(1))).isEqualTo(4);
        assertThat(constant(condition.rightOperand())).isEqualTo("JAVA");
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
    @DisplayName("should represent every standard native function")
    void shouldRepresentStandardFunctions(String predicate, String name) {
        select("FROM Person WHERE " + predicate).result();
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
    @DisplayName("should support the predicate operators with a native function")
    void shouldSupportPredicates(String predicate, Condition expected) {
        select("FROM Person WHERE " + predicate).result();
        var condition = lastSelectCondition();
        assertThat(condition.condition()).isEqualTo(expected);
        assertThat(condition.leftOperand()).isInstanceOf(CriteriaOperand.Function.class);
        assertThat(condition.rightOperand()).isInstanceOf(CriteriaOperand.Constant.class);
    }

    @Test
    @DisplayName("should represent NOT EQUALS and IS NULL with a native function keeping the negation")
    void shouldSupportNegationAndNull() {
        select("FROM Person WHERE UPPER(name) <> 'A'").result();
        var notEquals = lastSelectCondition();
        assertThat(notEquals.condition()).isEqualTo(Condition.NOT);
        var negated = notEquals.element().get(CriteriaCondition.class);
        assertThat(negated.condition()).isEqualTo(Condition.EQUALS);
        assertThat(negated.isNative()).isTrue();

        assertThatThrownBy(() -> select("FROM Person WHERE UPPER(name) IS NULL"))
                .isInstanceOf(QueryException.class);
        var upper = NativeFunction.of("UPPER", new FunctionArgument.AttributePath("name"));
        var isNull = CriteriaCondition.of(new CriteriaOperand.Function(upper), Condition.EQUALS,
                new CriteriaOperand.Constant(Value.of(null)));
        assertThat(((CriteriaOperand.Constant) isNull.rightOperand()).value().isNull()).isTrue();
    }

    @Test
    @DisplayName("should reject IN with a function operand because the grammar does not define it")
    void shouldRejectInWithFunction() {
        assertThatThrownBy(() -> select("FROM Person WHERE UPPER(name) IN ('JAVA', 'JAKARTA')"))
                .isInstanceOf(QueryException.class);
        var upper = NativeFunction.of("UPPER", new FunctionArgument.AttributePath("name"));
        var in = CriteriaCondition.of(new CriteriaOperand.Function(upper), Condition.IN,
                new CriteriaOperand.Constant(Value.of(List.of("JAVA", "JAKARTA"))));
        assertThat(in.condition()).isEqualTo(Condition.IN);
        assertThat(constant(in.rightOperand())).isEqualTo(List.of("JAVA", "JAKARTA"));
    }

    @Test
    @DisplayName("should keep Boolean grouping and negation around native predicates")
    void shouldKeepGrouping() {
        select("FROM Person WHERE UPPER(name) = 'OTAVIO' AND (ABS(balance) > 100 OR active = TRUE)").result();
        var and = lastSelectCondition();
        assertThat(and.condition()).isEqualTo(Condition.AND);
        var branches = children(and);
        assertThat(branches.get(0).isNative()).isTrue();
        assertThat(branches.get(1).condition()).isEqualTo(Condition.OR);
        var or = children(branches.get(1));
        assertThat(or.get(0).isNative()).isTrue();
        assertThat(or.get(1).isNative()).isFalse();
        assertThat(or.get(1).element().name()).isEqualTo("active");

        select("FROM Person WHERE NOT UPPER(name) = 'A' OR LENGTH(name) = 3 AND active = TRUE").result();
        var root = lastSelectCondition();
        assertThat(root.condition()).isEqualTo(Condition.OR);
        assertThat(children(root).get(0).condition()).isEqualTo(Condition.NOT);
        assertThat(children(root).get(1).condition()).isEqualTo(Condition.AND);
    }

    @Test
    @DisplayName("should map only attribute references to stored field names")
    void shouldMapOnlyAttributes() {
        select("FROM Person WHERE UPPER(name) = UPPER('name')", MAPPING).result();
        var condition = lastSelectCondition();
        assertThat(function(condition.leftOperand()).arguments())
                .containsExactly(new FunctionArgument.AttributePath("person_name"));
        assertThat(constant(condition.rightOperand())).isEqualTo("NAME");

        select("FROM Person WHERE LEFT(name, LENGTH(:name)) = :name", MAPPING).bind("name", "name").result();
        var parameter = lastSelectCondition();
        var left = function(parameter.leftOperand());
        assertThat(left.arguments().getFirst()).isEqualTo(new FunctionArgument.AttributePath("person_name"));
        assertThat(valueArgument(left.arguments().get(1))).isEqualTo(4);
        assertThat(constant(parameter.rightOperand())).isEqualTo("name");

        select("FROM Person WHERE name = UPPER(address.city)", MAPPING).result();
        var right = lastSelectCondition();
        assertThat(right.leftOperand()).isEqualTo(new CriteriaOperand.Attribute("person_name"));
        assertThat(function(right.rightOperand()).arguments())
                .containsExactly(new FunctionArgument.AttributePath("address.city_name"));

        select("FROM Person WHERE UPPER(LEFT(name, 2)) = 'OT'", MAPPING).result();
        var nested = lastSelectCondition();
        var inner = ((FunctionArgument.NativeArgument) function(nested.leftOperand()).arguments().getFirst())
                .function();
        assertThat(inner.arguments().getFirst()).isEqualTo(new FunctionArgument.AttributePath("person_name"));
        assertThat(inner.arguments().get(1)).isEqualTo(new FunctionArgument.Literal(2));
    }

    @Test
    @DisplayName("should use current bindings in each execution and isolate delayed consumption")
    void shouldIsolateExecutions() {
        var statement = select("FROM Person WHERE LEFT(name, LENGTH(:prefix)) = UPPER(:prefix)");

        statement.bind("prefix", "ota").result();
        var first = lastSelectCondition();
        statement.bind("prefix", "jakarta").result();
        var second = lastSelectCondition();
        statement.bind("prefix", "java").result();
        var third = lastSelectCondition();

        assertThat(first).isNotSameAs(second);
        assertThat(valueArgument(function(first.leftOperand()).arguments().get(1))).isEqualTo(3);
        assertThat(constant(first.rightOperand())).isEqualTo("OTA");
        assertThat(valueArgument(function(second.leftOperand()).arguments().get(1))).isEqualTo(7);
        assertThat(constant(second.rightOperand())).isEqualTo("JAKARTA");
        assertThat(valueArgument(function(third.leftOperand()).arguments().get(1))).isEqualTo(4);
        assertThat(constant(third.rightOperand())).isEqualTo("JAVA");
    }

    @Test
    @DisplayName("should capture a bare parameter argument per execution")
    void shouldCaptureParameterArguments() {
        var statement = select("FROM Person WHERE LEFT(name, :size) = 'ot'");
        statement.bind("size", 2).result();
        var first = lastSelectCondition();
        statement.bind("size", 5).result();
        assertThat(valueArgument(function(first.leftOperand()).arguments().get(1))).isEqualTo(2);
        assertThat(valueArgument(function(lastSelectCondition().leftOperand()).arguments().get(1))).isEqualTo(5);
    }

    @Test
    @DisplayName("should not contaminate later executions after a failed one")
    void shouldRecoverAfterFailure() {
        var statement = select("FROM Person WHERE LEFT(name, LENGTH(:prefix)) = :prefix");
        assertThatThrownBy(statement::result).isInstanceOf(QueryException.class);
        statement.bind("prefix", "ota").result();
        assertThat(valueArgument(function(lastSelectCondition().leftOperand()).arguments().get(1))).isEqualTo(3);
    }

    @Test
    @DisplayName("should not evaluate arguments when building or inspecting, and memoize once evaluated")
    void shouldBeLazyAndMemoized() {
        var counter = new AtomicInteger();
        var length = EvaluableFunction.of("LENGTH", new FunctionArgument.ValueArgument(counting(counter, "ota")));
        var left = NativeFunction.of("LEFT", new FunctionArgument.AttributePath("name"),
                new FunctionArgument.ValueArgument(length));

        var condition = CriteriaCondition.of(new CriteriaOperand.Function(left), Condition.EQUALS,
                new CriteriaOperand.Constant(Value.of("ota")));
        assertThat(condition.isNative()).isTrue();
        assertThat(function(condition.leftOperand()).name()).isEqualTo("LEFT");
        assertThat(function(condition.leftOperand()).arguments()).hasSize(2);
        assertThat(counter).hasValue(0);

        var argument = (FunctionArgument.ValueArgument) function(condition.leftOperand()).arguments().get(1);
        assertThat(argument.value().get()).isEqualTo(3);
        assertThat(argument.value().get()).isEqualTo(3);
        assertThat(counter).hasValue(1);
    }

    @Test
    @DisplayName("should memoize null results")
    void shouldMemoizeNull() {
        var counter = new AtomicInteger();
        var upper = EvaluableFunction.of("UPPER", new FunctionArgument.ValueArgument(counting(counter, null)));
        assertThat(upper.get()).isNull();
        assertThat(upper.get()).isNull();
        assertThat(counter).hasValue(1);
    }

    @Test
    @DisplayName("should not evaluate the function at preparation or binding")
    void shouldNotEvaluateAtBinding() {
        select("FROM Person WHERE LEFT(name, LENGTH(:prefix)) = :prefix").bind("prefix", "x");
        Mockito.verifyNoInteractions(manager);
    }

    @Test
    @DisplayName("should expose immutable structured arguments")
    void shouldBeImmutable() {
        select("FROM Person WHERE LEFT(name, 2) = 'ot'").result();
        var arguments = function(lastSelectCondition().leftOperand()).arguments();
        assertThatThrownBy(() -> arguments.add(new FunctionArgument.Literal(1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should keep ordinary conditions compatible with the legacy contract")
    void shouldKeepOrdinaryCompatible() {
        var condition = CriteriaCondition.eq(Element.of("name", "OTAVIO"));
        assertThat(condition.isNative()).isFalse();
        assertThat(condition.element().name()).isEqualTo("name");
        assertThat(condition.element().get()).isEqualTo("OTAVIO");
        assertThat(condition.leftOperand()).isEqualTo(new CriteriaOperand.Attribute("name"));
        assertThat(constant(condition.rightOperand())).isEqualTo("OTAVIO");
        assertThat(CriteriaCondition.of(new CriteriaOperand.Attribute("name"), Condition.EQUALS,
                new CriteriaOperand.Constant(Value.of("OTAVIO")))).isEqualTo(condition);
        assertThatThrownBy(() -> CriteriaCondition.and(condition, condition).leftOperand())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should fail explicitly instead of exposing a native function through the legacy element")
    void shouldFailLegacyAccess() {
        select("FROM Person WHERE UPPER(name) = 'OTAVIO'").result();
        var condition = lastSelectCondition();
        assertThatThrownBy(condition::element).isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("native function");
        select("FROM Person WHERE name = UPPER(other)").result();
        assertThatThrownBy(lastSelectCondition()::element).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should deliver native predicates intact to DELETE, UPDATE WHERE and COUNT")
    void shouldDeliverToDestructiveOperations() {
        new DeleteQueryParser().prepare("DELETE FROM Person WHERE UPPER(name) = 'OTAVIO'", manager,
                CommunicationObserverParser.EMPTY).result();
        var delete = ArgumentCaptor.forClass(DeleteQuery.class);
        Mockito.verify(manager).delete(delete.capture());
        assertThat(delete.getValue().condition().orElseThrow().isNative()).isTrue();

        new UpdateQueryParser().prepare("UPDATE Person SET active = TRUE WHERE UPPER(name) = 'OTAVIO'", manager,
                CommunicationObserverParser.EMPTY).result();
        var update = ArgumentCaptor.forClass(UpdateQuery.class);
        Mockito.verify(manager).update(update.capture());
        assertThat(update.getValue().where().orElseThrow().isNative()).isTrue();

        select("SELECT COUNT(this) FROM Person WHERE UPPER(name) = 'OTAVIO'").count();
        var count = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(manager).count(count.capture());
        assertThat(count.getValue().condition().orElseThrow().isNative()).isTrue();
    }

    @Test
    @DisplayName("should keep native assignments unsupported")
    void shouldRejectNativeAssignment() {
        assertThatThrownBy(() -> new UpdateQueryParser().prepare("UPDATE Person SET name = UPPER(name) WHERE id = :id",
                manager, CommunicationObserverParser.EMPTY))
                .isInstanceOf(QueryException.class).hasMessageContaining("Native function");
        Mockito.verifyNoInteractions(manager);
    }

    @Test
    @DisplayName("should build native predicates programmatically with one factory")
    void shouldBuildProgrammatically() {
        var upper = NativeFunction.of("UPPER", new FunctionArgument.AttributePath("name"));
        var abs = NativeFunction.of("ABS", new FunctionArgument.AttributePath("balance"));
        var evaluable = EvaluableFunction.of("UPPER", FunctionArgument.literal("otavio"));

        var one = CriteriaCondition.of(new CriteriaOperand.Function(upper), Condition.EQUALS,
                new CriteriaOperand.Constant(Value.of("OTAVIO")));
        var two = CriteriaCondition.of(new CriteriaOperand.Attribute("name"), Condition.EQUALS,
                new CriteriaOperand.Function(upper));
        var three = CriteriaCondition.of(new CriteriaOperand.Function(upper), Condition.EQUALS,
                new CriteriaOperand.Constant(evaluable));
        var four = CriteriaCondition.of(new CriteriaOperand.Function(abs), Condition.GREATER_THAN,
                new CriteriaOperand.Constant(Value.of(100)));

        assertThat(constant(three.rightOperand())).isEqualTo("OTAVIO");
        assertThat(function(two.rightOperand())).isEqualTo(upper);
        assertThat(four.condition()).isEqualTo(Condition.GREATER_THAN);

        var composed = one.and(four).or(two);
        assertThat(composed.condition()).isEqualTo(Condition.OR);
        assertThat(CriteriaCondition.readOnly(one)).isEqualTo(one);
        assertThat(CriteriaCondition.readOnly(one).isNative()).isTrue();
        assertThat(CriteriaCondition.readOnly(one).hashCode()).isNotEqualTo(0);
        assertThat(one).isNotEqualTo(two).isEqualTo(CriteriaCondition.of(new CriteriaOperand.Function(upper),
                Condition.EQUALS, new CriteriaOperand.Constant(Value.of("OTAVIO"))));
        assertThat(one.toString()).contains("UPPER");
    }

    @Test
    @DisplayName("should reject invalid operand combinations explicitly")
    void shouldRejectInvalidCombinations() {
        var upper = new CriteriaOperand.Function(NativeFunction.of("UPPER", new FunctionArgument.AttributePath("n")));
        var attribute = new CriteriaOperand.Attribute("a");
        var value = new CriteriaOperand.Constant(Value.of("x"));
        assertThatThrownBy(() -> CriteriaCondition.of(value, Condition.EQUALS, upper))
                .isInstanceOf(QueryException.class);
        assertThatThrownBy(() -> CriteriaCondition.of(attribute, Condition.EQUALS, attribute))
                .isInstanceOf(QueryException.class);
        assertThatThrownBy(() -> CriteriaCondition.of(upper, Condition.LIKE, upper))
                .isInstanceOf(QueryException.class);
        assertThatThrownBy(() -> CriteriaCondition.of(upper, Condition.IN, value))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CriteriaCondition.of(upper, Condition.BETWEEN,
                new CriteriaOperand.Constant(Value.of(List.of(1)))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CriteriaCondition.of(upper, Condition.AND, value))
                .isInstanceOf(QueryException.class);
        assertThatThrownBy(() -> CriteriaCondition.of(attribute, Condition.OR, value))
                .isInstanceOf(QueryException.class);
        assertThatThrownBy(() -> CriteriaCondition.of(null, Condition.EQUALS, value))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Condition.class, names = {"CONTAINS", "STARTS_WITH", "ENDS_WITH", "IGNORE_CASE", "NOT"})
    @DisplayName("should reject conditions that cannot safely hold a native function")
    void shouldRejectUnsupportedConditions(Condition condition) {
        var upper = new CriteriaOperand.Function(NativeFunction.of("UPPER", new FunctionArgument.AttributePath("n")));
        assertThatThrownBy(() -> CriteriaCondition.of(upper, condition, new CriteriaOperand.Constant(Value.of("x"))))
                .isInstanceOf(QueryException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "FROM Person WHERE UPPER(name) = UPPER(:missing)",
            "FROM Person WHERE LEFT(name, LENGTH(:missing)) = 'x'"
    })
    @DisplayName("should fail explicitly when evaluating an unbound parameter")
    void shouldFailUnbound(String query) {
        assertThatThrownBy(() -> {
            select(query).result();
            var condition = lastSelectCondition();
            constant(condition.rightOperand());
            condition.leftOperand();
            valueArgument(function(condition.leftOperand()).arguments().get(1));
        }).isInstanceOfAny(QueryException.class, IndexOutOfBoundsException.class);
    }
}
