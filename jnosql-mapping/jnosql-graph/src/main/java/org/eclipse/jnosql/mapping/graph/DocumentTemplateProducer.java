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
package org.eclipse.jnosql.mapping.graph;

import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.semistructured.AbstractSemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;

import java.util.Objects;
import java.util.function.Function;

/**
 * An {@code ApplicationScoped} producer class responsible for creating instances of {@link GraphTemplate}.
 * It implements the {@link Function} interface with {@link DatabaseManager} as input and {@link GraphTemplate} as output.
 */
@ApplicationScoped
public class DocumentTemplateProducer implements Function<DatabaseManager, GraphTemplate> {

    @Inject
    private EntityConverter converter;

    @Inject
    private EventPersistManager eventManager;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;


    @Override
    public GraphTemplate apply(DatabaseManager manager) {
        Objects.requireNonNull(manager, "manager is required");
        return new ProducerGraphTemplate(converter, manager,
                eventManager, entities, converters);
    }

    @Vetoed
    static class ProducerGraphTemplate extends AbstractSemiStructuredTemplate implements GraphTemplate {

        private final EntityConverter converter;

        private final  DatabaseManager manager;

        private final EventPersistManager eventManager;

        private final EntitiesMetadata entities;

        private final  Converters converters;

        ProducerGraphTemplate(EntityConverter converter,
                              DatabaseManager manager,
                              EventPersistManager eventManager,
                              EntitiesMetadata entities,
                              Converters converters) {
            this.converter = converter;
            this.manager = manager;
            this.eventManager = eventManager;
            this.entities = entities;
            this.converters = converters;
        }

        ProducerGraphTemplate() {
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
}
