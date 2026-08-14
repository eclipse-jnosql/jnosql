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
package org.eclipse.jnosql.communication.keyvalue;

import org.eclipse.jnosql.communication.CommunicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@DisplayName("Key-value configuration")
class KeyValueConfigurationTest {


    @Nested
    @DisplayName("When loading a configuration")
    class WhenTheConfigurationLoading {

        @Test
        @DisplayName("Should throw an exception when no configuration exists")
        void shouldThrowExceptionWhenNoConfigurationExists() {

            // When / Then
            assertThatExceptionOfType(CommunicationException.class)
                    .isThrownBy(KeyValueConfiguration::getConfiguration);
        }

        @Test
        @DisplayName("Should throw an exception when the requested configuration does not exist")
        void shouldThrowExceptionWhenRequestedConfigurationDoesNotExist() {

            // When / Then
            assertThatExceptionOfType(CommunicationException.class)
                    .isThrownBy(() -> KeyValueConfiguration.getConfiguration(MockKeyValueConfiguration.class));
        }
    }
}