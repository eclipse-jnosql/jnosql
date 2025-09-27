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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class KeyValueConfigurationTest {


    @Test
    void shouldErrorWhenConfiguration() {
        Assertions.assertThrows(CommunicationException.class, () -> KeyValueConfiguration.getConfiguration());
    }

    @Test
    void shouldCheckConfiguration2() {
        Assertions.assertThrows(CommunicationException.class,
                () -> KeyValueConfiguration.getConfiguration(MockKeyValueConfiguration.class));
    }
}