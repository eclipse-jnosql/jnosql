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
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.BooleanQueryValue;
import org.eclipse.jnosql.communication.query.NullQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.StringQueryValue;
import org.eclipse.jnosql.communication.query.UpdateItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

class UpdateJakartaDataQuerySpecialTest {
    private UpdateParser updateParser;

    @BeforeEach
    void setUp() {
        updateParser = new UpdateParser();
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET active = true"})
    void shouldValidateTrue(String query){
        var selectQuery = updateParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            List<UpdateItem> items = selectQuery.set();
            soft.assertThat(items).hasSize(1);

            soft.assertThat(items).isNotNull().hasSize(1)
                    .contains(JDQLUpdateItem.of("active", BooleanQueryValue.TRUE));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET active = false"})
    void shouldValidateFalse(String query){
        var selectQuery = updateParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            List<UpdateItem> items = selectQuery.set();
            soft.assertThat(items).hasSize(1);

            soft.assertThat(items).isNotNull().hasSize(1)
                    .contains(JDQLUpdateItem.of("active", BooleanQueryValue.FALSE));
        });
    }

    @ParameterizedTest(name = "Should parser the query {0}")
    @ValueSource(strings = {"UPDATE entity SET active = NULL"})
    void shouldValidateNull(String query){
        var selectQuery = updateParser.apply(query);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(selectQuery.entity()).isEqualTo("entity");
            List<UpdateItem> items = selectQuery.set();
            soft.assertThat(items).hasSize(1);

            soft.assertThat(items).isNotNull().hasSize(1)
                    .contains(JDQLUpdateItem.of("active", NullQueryValue.INSTANCE));
        });
    }


}
