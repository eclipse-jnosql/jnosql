/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.vector.configuration;

import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.semistructured.DatabaseConfiguration;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DatabaseManagerFactory;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VectorConfigurationMock implements DatabaseConfiguration {

    @Override
    public DatabaseManagerFactory apply(Settings settings) {
        DatabaseManagerFactory factory = mock(DatabaseManagerFactory.class);
        when(factory.apply(anyString())).thenAnswer(invocation -> {
            DatabaseManager manager = mock(DatabaseManager.class);
            when(manager.name()).thenReturn(getClass().getSimpleName() + ":" + invocation.getArgument(0));
            return manager;
        });
        return factory;
    }
}
