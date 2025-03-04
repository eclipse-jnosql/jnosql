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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.AbstractSemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;


@Default
@ApplicationScoped
@Database(DatabaseType.DOCUMENT)
class DefaultDocumentTemplate extends AbstractSemiStructuredTemplate implements DocumentTemplate {


    private final EntityConverter converter;

    private final  DatabaseManager manager;

    private final  EventPersistManager eventManager;

    private final  EntitiesMetadata entities;

    private final  Converters converters;



    @Inject
    DefaultDocumentTemplate(EntityConverter converter,
                            @Database(DatabaseType.DOCUMENT) DatabaseManager manager,
                            EventPersistManager eventManager,
                            EntitiesMetadata entities, Converters converters){
        this.converter = converter;
        this.manager = manager;
        this.eventManager = eventManager;
        this.entities = entities;
        this.converters = converters;
    }

    DefaultDocumentTemplate() {
        this(null, null, null, null, null);
    }

    @Override
    protected EntityConverter converter() {
        return converter;
    }

    @Override
    protected DatabaseManager manager() {
        return manager;
    }

    @Override
    protected EventPersistManager eventManager() {
        return eventManager;
    }

    @Override
    protected EntitiesMetadata entities() {
        return entities;
    }

    @Override
    protected Converters converters() {
        return converters;
    }


}
