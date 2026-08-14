/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;


import org.eclipse.jnosql.communication.Params;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThat;

class QueryParamsTest {


    @Nested
    @DisplayName("When the query params is used")
    class WhenTheQueryParamsIsUsed {

        @DisplayName("Should Create Query Params")
        @Test
        void shouldCreateQueryParams() {
            SelectQuery query = mock(SelectQuery.class);
            Params params = mock(Params.class);

            QueryParams queryParams = new QueryParams(query, params);

            assertThat(queryParams.query()).isSameAs(query);
            assertThat(queryParams.params()).isSameAs(params);
        }

        @DisplayName("Should Implement Equals And Hash Code")
        @Test
        void shouldImplementEqualsAndHashCode() {
            SelectQuery query = mock(SelectQuery.class);
            Params params = mock(Params.class);

            QueryParams first = new QueryParams(query, params);
            QueryParams second = new QueryParams(query, params);

            assertThat(first).isEqualTo(second);
            assertThat(first).hasSameHashCodeAs(second);
        }

        @DisplayName("Should Have To String Representation")
        @Test
        void shouldHaveToStringRepresentation() {
            SelectQuery query = mock(SelectQuery.class);
            Params params = mock(Params.class);

            QueryParams queryParams = new QueryParams(query, params);

            assertThat(queryParams.toString())
                    .contains("query=")
                    .contains("params=");
        }
    }

}
