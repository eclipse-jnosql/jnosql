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
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
@Default
@ApplicationScoped
class DefaultSemiStructuredTemplate extends AbstractSemiStructuredTemplate {

    private EntityConverter converter;

    private Instance<DatabaseManager> manager;

    private EventPersistManager eventManager;

    private EntitiesMetadata entities;

    private Converters converters;

    @Inject
    DefaultSemiStructuredTemplate(EntityConverter converter, Instance<DatabaseManager> manager,
                                  EventPersistManager eventManager,
                                  EntitiesMetadata entities, Converters converters) {
        this.converter = converter;
        this.manager = manager;
        this.eventManager = eventManager;
        this.entities = entities;
        this.converters = converters;
    }

    DefaultSemiStructuredTemplate(){
    }

    @Override
    protected EntityConverter converter() {
        return converter;
    }

    @Override
    protected DatabaseManager manager() {
        return manager.get();
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

    @Override
    protected void checkFunctionSupport(Function function) {
        // no-op: test template accepts all operations; real database behavior is mocked
    }
}
