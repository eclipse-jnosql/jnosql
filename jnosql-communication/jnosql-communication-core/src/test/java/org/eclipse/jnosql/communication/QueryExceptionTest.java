/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


public class QueryExceptionTest {

    @Test
    void shouldCreateWithMessage() {
        String message = "query failed";

        QueryException ex = new QueryException(message);

        assertThat(ex)
                .isInstanceOf(CommunicationException.class)
                .hasMessage(message)
                .hasNoCause();
    }

    @Test
    void shouldCreateWithMessageAndCause() {
        String message = "bad query";
        IllegalArgumentException cause = new IllegalArgumentException("boom");

        QueryException ex = new QueryException(message, cause);

        assertThat(ex)
                .isInstanceOf(CommunicationException.class)
                .hasMessage(message)
                .hasCause(cause);

        assertThat(ex.getCause())
                .isSameAs(cause)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("boom");
    }
}
