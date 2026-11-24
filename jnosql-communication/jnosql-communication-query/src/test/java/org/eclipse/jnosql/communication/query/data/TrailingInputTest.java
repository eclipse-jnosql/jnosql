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

import org.eclipse.jnosql.communication.QueryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Strict end-of-query validation")
class TrailingInputTest {

    private static void parse(String query) {
        if (query.startsWith("DELETE")) {
            new DeleteParser().apply(query);
        } else if (query.startsWith("UPDATE")) {
            new UpdateParser().apply(query);
        } else {
            new SelectParser().apply(query, "Person");
        }
    }

    @ParameterizedTest
    @DisplayName("Should reject unsupported trailing syntax")
    @ValueSource(strings = {
            "FROM Person WHERE age = 10 foo",
            "FROM Person LIMIT 10",
            "FROM Person SKIP 10",
            "FROM Person WHERE age = 10 LIMIT 10",
            "FROM Person WHERE age = 10 SKIP 5",
            "DELETE FROM Person WHERE age = 10 foo",
            "DELETE FROM Person WHERE age = 10 LIMIT 10",
            "DELETE FROM Person WHERE age = 10 SKIP 10",
            "UPDATE Person SET active = FALSE WHERE age = 10 foo",
            "UPDATE Person SET active = FALSE WHERE age = 10 LIMIT 10",
            "UPDATE Person SET active = FALSE WHERE age = 10 SKIP 10"
    })
    void shouldRejectTrailingInput(String query) {
        assertThatThrownBy(() -> parse(query)).isInstanceOf(QueryException.class);
    }

    @ParameterizedTest
    @DisplayName("Should accept valid queries with surrounding and trailing whitespace")
    @ValueSource(strings = {
            "FROM Person WHERE age = 10 ",
            "FROM Person WHERE age = 10  \n\t ",
            "  FROM Person WHERE (age = 10 OR age = 20) AND active = TRUE \n",
            "FROM Person ",
            "DELETE FROM Person WHERE age = 10 \n",
            "DELETE FROM Person  ",
            "UPDATE Person SET active = FALSE WHERE age = 10 \t\n",
            "UPDATE Person SET active = FALSE "
    })
    void shouldAcceptTrailingWhitespace(String query) {
        assertThatCode(() -> parse(query)).doesNotThrowAnyException();
    }
}
