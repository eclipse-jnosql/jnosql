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
package org.eclipse.jnosql.mapping.vector;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.interceptor.Interceptor;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ApplicationScoped
@Alternative
@Priority(Interceptor.Priority.APPLICATION)
public class MockProducer {

    @Produces
    @ApplicationScoped
    @Default
    @Database(DatabaseType.VECTOR)
    public VectorManager defaultManager() {
        return manager("default");
    }

    @Produces
    @ApplicationScoped
    @Database(value = DatabaseType.VECTOR, provider = "named")
    public VectorManager namedManager() {
        return manager("named");
    }

    @Produces
    @Database(value = DatabaseType.TIME_SERIES, provider = "other")
    public DatabaseManager otherManager() {
        return manager("other");
    }

    private VectorManager manager(String provider) {
        VectorManager manager = mock(VectorManager.class);
        when(manager.name()).thenReturn(provider);
        when(manager.defaultIdFieldName()).thenReturn(Optional.empty());
        when(manager.singleResult(any(SelectQuery.class))).thenReturn(Optional.empty());
        when(manager.insert(any(CommunicationEntity.class))).thenAnswer(invocation -> {
            CommunicationEntity entity = invocation.getArgument(0);
            entity.add("content", provider);
            return entity;
        });
        return manager;
    }
}
