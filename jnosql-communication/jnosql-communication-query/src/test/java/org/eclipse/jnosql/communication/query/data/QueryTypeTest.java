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
package org.eclipse.jnosql.communication.query.data;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class QueryTypeTest {

    @Nested
    @DisplayName("When the query type is used")
    class WhenTheQueryType {
    }


    @Test
    @DisplayName("Should Parse Select Query")
    void shouldParseSelectQuery() {
        String query = "SELECT * FROM table";
        QueryType result = QueryType.parse(query);
        assertThat(result).isEqualTo(QueryType.SELECT);
    }

    @Test
    @DisplayName("Should Parse Delete Query")
    void shouldParseDeleteQuery() {
        String query = "DELETE FROM table WHERE id = 1";
        QueryType result = QueryType.parse(query);
        assertThat(result).isEqualTo(QueryType.DELETE);
    }

    @Test
    @DisplayName("Should Parse Update Query")
    void shouldParseUpdateQuery() {
        String query = "UPDATE table SET name = 'newName' WHERE id = 1";
        QueryType result = QueryType.parse(query);
        assertThat(result).isEqualTo(QueryType.UPDATE);
    }

    @Test
    @DisplayName("Should Default To Select For Unknown Query")
    void shouldDefaultToSelectForUnknownQuery() {
        String query = "INSERT INTO table (id, name) VALUES (1, 'name')";
        QueryType result = QueryType.parse(query);
        assertThat(result).isEqualTo(QueryType.SELECT);
    }

    @Test
    @DisplayName("Should Default To Select For Short Query")
    void shouldDefaultToSelectForShortQuery() {
        String query = "DELE";
        QueryType result = QueryType.parse(query);
        assertThat(result).isEqualTo(QueryType.SELECT);
    }

    @Test
    @DisplayName("Should Default To Select For Empty Query")
    void shouldDefaultToSelectForEmptyQuery() {
        String query = "";
        QueryType result = QueryType.parse(query);
        assertThat(result).isEqualTo(QueryType.SELECT);
    }

    @Test
    @DisplayName("Should Throw Null Pointer Exception For Null Query")
    void shouldThrowNullPointerExceptionForNullQuery() {
        String query = null;
        assertThatThrownBy(() -> QueryType.parse(query))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should Return Is Not Select")
    void shouldReturnIsNotSelect() {
        Assertions.assertThat(QueryType.SELECT.isNotSelect()).isFalse();
        Assertions.assertThat(QueryType.DELETE.isNotSelect()).isTrue();
        Assertions.assertThat(QueryType.UPDATE.isNotSelect()).isTrue();
    }

    @Test
    @DisplayName("Should Check Valid Return")
    void shouldCheckValidReturn() {
        QueryType.SELECT.checkValidReturn(String.class, "SELECT * FROM table");
        QueryType.DELETE.checkValidReturn(Void.class, "DELETE FROM table WHERE id = 1");
        QueryType.UPDATE.checkValidReturn(Void.class, "UPDATE table SET name = 'newName' WHERE id = 1");
        assertThatThrownBy(() -> QueryType.DELETE.checkValidReturn(String.class, "DELETE FROM table WHERE id = 1"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> QueryType.UPDATE.checkValidReturn(String.class, "UPDATE table SET name = 'newName' WHERE id = 1"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @DisplayName("Should Is Void")
    @ValueSource(classes = {Void.class, void.class})
    void shouldIsVoid(Class<?> type) {
        Assertions.assertThat(QueryType.SELECT.isVoid(type)).isTrue();
    }

    @ParameterizedTest
    @DisplayName("Should Not Is Void")
    @ValueSource(classes = {String.class, Integer.class})
    void shouldNotIsVoid(Class<?> type) {
        Assertions.assertThat(QueryType.SELECT.isVoid(type)).isFalse();
    }


    @Test
    @DisplayName("Should Check Value Return")
    void shouldCheckValueReturn() {
        QueryType type = QueryType.UPDATE;
        Assertions.assertThatThrownBy(() -> type.checkValidReturn(String.class, "UPDATE table SET name = 'newName' WHERE id = 1"))
                .isInstanceOf(UnsupportedOperationException.class);

    }
}