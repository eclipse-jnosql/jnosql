/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 */
package org.eclipse.jnosql.mapping.document;


import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import jakarta.interceptor.Interceptor;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ApplicationScoped
@Alternative
@Priority(Interceptor.Priority.APPLICATION)
public class MockProducer implements Supplier<DatabaseManager> {

    @Produces
    @Override
    @Database(DatabaseType.DOCUMENT)
    public DatabaseManager get() {
        CommunicationEntity entity = CommunicationEntity.of("Person");
        entity.add(Element.of("name", "Default"));
        entity.add(Element.of("age", 10));
        DatabaseManager manager = mock(DatabaseManager.class);
        when(manager.insert(Mockito.any(CommunicationEntity.class))).thenReturn(entity);
        return manager;
    }

    @Produces
    @Database(value = DatabaseType.DOCUMENT, provider = "documentRepositoryMock")
    public DatabaseManager getDocumentManagerMock() {
        CommunicationEntity entity = CommunicationEntity.of("Person");
        entity.add(Element.of("name", "documentRepositoryMock"));
        entity.add(Element.of("age", 10));
        DatabaseManager manager = mock(DatabaseManager.class);
        when(manager.insert(Mockito.any(CommunicationEntity.class))).thenReturn(entity);
        when(manager.singleResult(Mockito.any(SelectQuery.class))).thenReturn(Optional.empty());
        return manager;

    }
}
