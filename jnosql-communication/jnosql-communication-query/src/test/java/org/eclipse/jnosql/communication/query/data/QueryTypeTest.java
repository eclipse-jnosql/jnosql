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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class QueryTypeTest {


    @Test
    void shouldParseSelectQuery() {
        String query = "SELECT * FROM table";
        QueryType result = QueryType.parse(query);
        assertThat(result).isEqualTo(QueryType.SELECT);
    }

    @Test
    void shouldParseDeleteQuery() {
        String query = "DELETE FROM table WHERE id = 1";
        QueryType result = QueryType.parse(query);
        assertThat(result).isEqualTo(QueryType.DELETE);
    }

    @Test
    void shouldParseUpdateQuery() {
        String query = "UPDATE table SET name = 'newName' WHERE id = 1";
        QueryType result = QueryType.parse(query);
        assertThat(result).isEqualTo(QueryType.UPDATE);
    }

    @Test
    void shouldDefaultToSelectForUnknownQuery() {
        String query = "INSERT INTO table (id, name) VALUES (1, 'name')";
        QueryType result = QueryType.parse(query);
        assertThat(result).isEqualTo(QueryType.SELECT);
    }

    @Test
    void shouldDefaultToSelectForShortQuery() {
        String query = "DELE";
        QueryType result = QueryType.parse(query);
        assertThat(result).isEqualTo(QueryType.SELECT);
    }

    @Test
    void shouldDefaultToSelectForEmptyQuery() {
        String query = "";
        QueryType result = QueryType.parse(query);
        assertThat(result).isEqualTo(QueryType.SELECT);
    }

    @Test
    void shouldThrowNullPointerExceptionForNullQuery() {
        String query = null;
        assertThatThrownBy(() -> QueryType.parse(query))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldReturnIsNotSelect() {
        Assertions.assertThat(QueryType.SELECT.isNotSelect()).isFalse();
        Assertions.assertThat(QueryType.DELETE.isNotSelect()).isTrue();
        Assertions.assertThat(QueryType.UPDATE.isNotSelect()).isTrue();
    }

    @Test
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
    @ValueSource(classes = {Void.class, void.class})
    void shouldIsVoid(Class<?> type) {
        Assertions.assertThat(QueryType.SELECT.isVoid(type)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(classes = {String.class, Integer.class})
    void shouldNotIsVoid(Class<?> type) {
        Assertions.assertThat(QueryType.SELECT.isVoid(type)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(classes = {Integer.class, int.class})
    void shouldIsInt(Class<?> type) {
        Assertions.assertThat(QueryType.SELECT.isInt(type)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(classes = {String.class, List.class})
    void shouldNotIsInt(Class<?> type) {
        Assertions.assertThat(QueryType.SELECT.isInt(type)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(classes = {Long.class, long.class})
    void shouldIsLong(Class<?> type) {
        Assertions.assertThat(QueryType.SELECT.isLong(type)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(classes = {String.class, List.class})
    void shouldNotIsLong(Class<?> type) {
        Assertions.assertThat(QueryType.SELECT.isLong(type)).isFalse();
    }

    @Test
    void shouldCheckValueReturn() {
        QueryType type = QueryType.UPDATE;
        Assertions.assertThatThrownBy(() -> type.checkValidReturn(String.class, "UPDATE table SET name = 'newName' WHERE id = 1"))
                .isInstanceOf(UnsupportedOperationException.class);

    }
}