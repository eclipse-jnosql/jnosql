/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UpdateJakartaDataQueryProviderTest {

    private UpdateProvider updateProvider;

    @BeforeEach
    void setUp() {
        updateProvider = new UpdateProvider();
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE FROM entity SET name = 'Ada'"})
    void shouldReturnParserQuery(String query) {
        var updateQuery = updateProvider.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(updateQuery.where()).isEmpty();
        });
    }

}