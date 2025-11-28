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
package org.eclipse.jnosql.mapping.column;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.AbstractSemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactory;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;

import java.util.Objects;
import java.util.function.Function;

/**
 * This class is responsible for producing instances of {@link ColumnTemplate}.
 * It implements the {@link Function} interface, which allows it to accept a {@link DatabaseManager}
 * and produce a corresponding {@link ColumnTemplate} instance.
 * <p>
 * The produced {@link ColumnTemplate} instance is configured with the necessary dependencies such as
 * {@link EntityConverter}, {@link EventPersistManager}, {@link EntitiesMetadata}, and {@link Converters}.
 * </p>
 * <p>
 * Usage of this class enables the creation of ready-to-use {@link ColumnTemplate} instances for interacting
 * with column-based data structures in a semistructured database.
 * </p>
 *
 * @see ColumnTemplate
 * @see DatabaseManager
 * @see EntityConverter
 * @see EventPersistManager
 * @see EntitiesMetadata
 * @see Converters
 */
@ApplicationScoped
public class ColumnTemplateProducer implements Function<DatabaseManager, ColumnTemplate> {


    @Inject
    private EntityConverterFactory converter;

    @Inject
    private EventPersistManager eventManager;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    @Override
    public ColumnTemplate apply(DatabaseManager manager) {
        Objects.requireNonNull(manager, "manager is required");
        return new ProducerColumnTemplate(converter, manager,
                eventManager, entities, converters);
    }


    /**
     * Inner class that serves as the actual implementation of {@link ColumnTemplate}.
     * It extends {@link AbstractSemiStructuredTemplate} and implements {@link ColumnTemplate}.
     */
    @Vetoed
    static class ProducerColumnTemplate extends AbstractSemiStructuredTemplate implements ColumnTemplate {

        private final EntityConverter converter;

        private final  DatabaseManager manager;

        private final EventPersistManager eventManager;

        private final EntitiesMetadata entities;

        private final  Converters converters;

        ProducerColumnTemplate(EntityConverterFactory converter,
                               DatabaseManager manager,
                               EventPersistManager eventManager,
                               EntitiesMetadata entities,
                               Converters converters) {
            this.converter = converter.create(manager);
            this.manager = manager;
            this.eventManager = eventManager;
            this.entities = entities;
            this.converters = converters;
        }

        ProducerColumnTemplate() {
            this.converter = null;
            this.manager = null;
            this.eventManager = null;
            this.entities = null;
            this.converters = null;
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
