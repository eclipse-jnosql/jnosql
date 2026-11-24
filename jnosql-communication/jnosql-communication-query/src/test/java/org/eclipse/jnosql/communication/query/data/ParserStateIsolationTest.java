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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Parser state isolation between sequential executions")
class ParserStateIsolationTest {

    private static final String COMPLEX = "age = 10 AND salary = 1000 OR name = 'John'";

    @Test
    @DisplayName("DELETE without WHERE must not inherit the previous condition")
    void shouldNotInheritConditionOnDelete() {
        var parser = new DeleteParser();
        parser.apply("DELETE FROM Person WHERE age = 10");
        assertThat(parser.apply("DELETE FROM Person").where()).isEmpty();
    }

    @Test
    @DisplayName("SELECT without WHERE must not inherit the previous condition")
    void shouldNotInheritConditionOnSelect() {
        var parser = new SelectParser();
        parser.apply("FROM Person WHERE age = 10", "Person");
        assertThat(parser.apply("FROM Person", "Person").where()).isEmpty();
    }

    @Test
    @DisplayName("UPDATE without WHERE must not inherit the previous condition")
    void shouldNotInheritConditionOnUpdate() {
        var parser = new UpdateParser();
        parser.apply("UPDATE Person SET active = true WHERE age = 10");
        assertThat(parser.apply("UPDATE Person SET active = false").where()).isEmpty();
    }

    @Test
    @DisplayName("A different WHERE must replace the previous one")
    void shouldReplaceCondition() {
        var parser = new DeleteParser();
        parser.apply("DELETE FROM Person WHERE age = 10");
        var second = parser.apply("DELETE FROM Person WHERE name = 'Ada'");
        assertThat(BooleanOperatorPrecedenceTest.render(second.where().orElseThrow()))
                .isEqualTo("name EQUALS Ada");
    }

    @Test
    @DisplayName("A WHERE after a query without WHERE must only contain the new condition")
    void shouldApplyConditionAfterNoWhere() {
        var parser = new DeleteParser();
        parser.apply("DELETE FROM Person");
        var second = parser.apply("DELETE FROM Person WHERE age = 10");
        assertThat(BooleanOperatorPrecedenceTest.render(second.where().orElseThrow()))
                .isEqualTo("age EQUALS 10");
    }

    @Test
    @DisplayName("A simple condition must not inherit grouping from a complex one")
    void shouldNotInheritGrouping() {
        var parser = new SelectParser();
        parser.apply("FROM Person WHERE " + COMPLEX, "Person");
        var second = parser.apply("FROM Person WHERE age = 10", "Person");
        assertThat(BooleanOperatorPrecedenceTest.render(second.where().orElseThrow()))
                .isEqualTo("age EQUALS 10");
    }

    @Test
    @DisplayName("A valid query after a failed one must have no residual state")
    void shouldRecoverAfterFailure() {
        var parser = new DeleteParser();
        assertThatThrownBy(() -> parser.apply("DELETE FROM Person WHERE age = 10 AND AND"))
                .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> parser.apply("DELETE FROM Person WHERE (age = 10"))
                .isInstanceOf(RuntimeException.class);
        assertThat(parser.apply("DELETE FROM Person").where()).isEmpty();
    }

    @Test
    @DisplayName("SELECT projections, ORDER BY and COUNT must not leak")
    void shouldNotLeakSelectState() {
        var parser = new SelectParser();
        var first = parser.apply("SELECT name FROM Person WHERE age = 10 ORDER BY name DESC", "Person");
        assertThat(first.fields()).containsExactly("name");
        assertThat(first.orderBy()).hasSize(1);

        var second = parser.apply("FROM Person", "Person");
        assertThat(second.fields()).isEmpty();
        assertThat(second.orderBy()).isEmpty();
        assertThat(second.isCount()).isFalse();
        assertThat(second.where()).isEmpty();
        assertThat(first.fields()).containsExactly("name");
        assertThat(first.orderBy()).hasSize(1);

        assertThat(parser.apply("SELECT COUNT(this) FROM Person", "Person").isCount()).isTrue();
        assertThat(parser.apply("FROM Person", "Person").isCount()).isFalse();
    }

    @Test
    @DisplayName("UPDATE assignments and entity must not leak")
    void shouldNotLeakUpdateState() {
        var parser = new UpdateParser();
        var first = parser.apply("UPDATE Person SET name = 'Ada', age = 10");
        assertThat(first.set()).hasSize(2);
        var second = parser.apply("UPDATE Animal SET name = 'Rex'");
        assertThat(second.entity()).isEqualTo("Animal");
        assertThat(second.set()).hasSize(1);
        assertThat(first.set()).hasSize(2);
    }

    @Test
    @DisplayName("DELETE entity must not leak")
    void shouldNotLeakDeleteEntity() {
        var parser = new DeleteParser();
        parser.apply("DELETE FROM Person");
        assertThat(parser.apply("DELETE FROM Animal").entity()).isEqualTo("Animal");
    }

    @Test
    @DisplayName("Separate parser instances must be independent")
    void shouldBeIndependentAcrossInstances() {
        var first = new DeleteParser();
        var second = new DeleteParser();
        first.apply("DELETE FROM Person WHERE " + COMPLEX);
        assertThat(second.apply("DELETE FROM Person").where()).isEmpty();
    }

    @Test
    @DisplayName("NOT NOT a must be equivalent to a")
    void shouldCollapseDoubleNegation() {
        var query = new DeleteParser().apply("DELETE FROM Person WHERE NOT NOT age = 10");
        assertThat(BooleanOperatorPrecedenceTest.render(query.where().orElseThrow())).isEqualTo("age EQUALS 10");
    }

    @Test
    @DisplayName("NOT a NOT IN (...) must be equivalent to a IN (...)")
    void shouldCollapseNotNotIn() {
        var query = new DeleteParser().apply("DELETE FROM Person WHERE NOT age NOT IN (10, 20)");
        assertThat(BooleanOperatorPrecedenceTest.render(query.where().orElseThrow()))
                .isEqualTo("age IN [10, 20]");
    }

    @Test
    @DisplayName("Parenthesized conditions must not leak into following queries")
    void shouldNotLeakParenthesizedGrouping() {
        var parser = new SelectParser();
        var grouped = parser.apply("FROM Person WHERE (age = 10 OR age = 20) AND active = TRUE", "Person");
        assertThat(BooleanOperatorPrecedenceTest.render(grouped.where().orElseThrow()))
                .isEqualTo("AND(OR(age EQUALS 10, age EQUALS 20), active EQUALS true)");

        var plain = parser.apply("FROM Person WHERE age = 10 OR age = 20 AND active = TRUE", "Person");
        assertThat(BooleanOperatorPrecedenceTest.render(plain.where().orElseThrow()))
                .isEqualTo("OR(age EQUALS 10, AND(age EQUALS 20, active EQUALS true))");

        assertThat(parser.apply("FROM Person", "Person").where()).isEmpty();
    }

    @Test
    @DisplayName("A valid query after a malformed parenthesized one must have no residual state")
    void shouldRecoverAfterMalformedParenthesis() {
        var parser = new DeleteParser();
        assertThatThrownBy(() -> parser.apply("DELETE FROM Person WHERE (age = 10 OR"))
                .isInstanceOf(RuntimeException.class);
        var query = parser.apply("DELETE FROM Person WHERE (age = 10 OR age = 20) AND active = FALSE");
        assertThat(BooleanOperatorPrecedenceTest.render(query.where().orElseThrow()))
                .isEqualTo("AND(OR(age EQUALS 10, age EQUALS 20), active EQUALS false)");
    }
}
