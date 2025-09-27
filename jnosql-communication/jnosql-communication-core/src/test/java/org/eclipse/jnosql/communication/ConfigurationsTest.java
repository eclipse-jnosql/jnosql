/*
 *
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 *
 */
package org.eclipse.jnosql.communication;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

class ConfigurationsTest {

    private static final Map<Configurations, String> EXPECTED_VALUES = Map.of(
            Configurations.USER, "jakarta.nosql.user",
            Configurations.PASSWORD, "jakarta.nosql.password",
            Configurations.HOST, "jakarta.nosql.host",
            Configurations.ENCRYPTION, "jakarta.nosql.settings.encryption",
            Configurations.CURSOR_PAGINATION_MULTIPLE_SORTING, "org.eclipse.jnosql.pagination.cursor"
    );

    @ParameterizedTest
    @EnumSource(Configurations.class)
    void shouldReturnExpectedConfigurationValue(Configurations config) {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(config.get())
                    .as("Check get() for " + config.name())
                    .isEqualTo(EXPECTED_VALUES.get(config));

            soft.assertThat(config)
                    .as("Ensure mapping is defined for " + config.name())
                    .isIn(EXPECTED_VALUES.keySet());
        });
    }
}