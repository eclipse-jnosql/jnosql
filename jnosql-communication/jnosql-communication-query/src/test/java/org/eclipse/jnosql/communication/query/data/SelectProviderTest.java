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
package org.eclipse.jnosql.communication.query.data;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


class SelectProviderTest {

    @Nested
    @DisplayName("When the select provider is used")
    class WhenTheSelectProvider {
    }


    @Test
    @DisplayName("Should Execute Query Without Entity")
    void shouldExecuteQueryWithoutEntity(){
        SelectProvider provider = SelectProvider.INSTANCE;

        String query = "FROM users";
        var selectQuery = provider.apply(query, null);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(selectQuery.entity()).isEqualTo("users");
            softAssertions.assertThat(selectQuery.fields()).isEmpty();
        });
    }

    @Test
    @DisplayName("Should Execute Query With Entity")
    void shouldExecuteQueryWithEntity(){
        SelectProvider provider = SelectProvider.INSTANCE;

        String query = "FROM users";
        var selectQuery = provider.apply(query, "users");

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(selectQuery.entity()).isEqualTo("users");
            softAssertions.assertThat(selectQuery.fields()).isEmpty();
        });
    }
}