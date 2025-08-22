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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UpdateQueryParamsTest {

    @Test
    void shouldCreateUpdateQueryParams() {
        UpdateQuery updateQuery = mock(UpdateQuery.class);
        Params params = mock(Params.class);

        UpdateQueryParams updateQueryParams = new UpdateQueryParams(updateQuery, params);

        assertThat(updateQueryParams.updateQuery()).isSameAs(updateQuery);
        assertThat(updateQueryParams.params()).isSameAs(params);
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        UpdateQuery updateQuery = mock(UpdateQuery.class);
        Params params = mock(Params.class);

        UpdateQueryParams first = new UpdateQueryParams(updateQuery, params);
        UpdateQueryParams second = new UpdateQueryParams(updateQuery, params);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSameHashCodeAs(second);
    }

    @Test
    void shouldHaveToStringRepresentation() {
        UpdateQuery updateQuery = mock(UpdateQuery.class);
        Params params = mock(Params.class);

        UpdateQueryParams updateQueryParams = new UpdateQueryParams(updateQuery, params);

        assertThat(updateQueryParams.toString())
                .contains("updateQuery=")
                .contains("params=");
    }
}