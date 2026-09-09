/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.timeseries;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.AbstractSemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;

import java.util.Objects;
import java.util.function.Function;

/**
 * Creates a {@link TimeSeriesTemplate} for an application-managed {@link DatabaseManager}.
 * <p>
 * This is useful when the application builds managers programmatically or needs a template for a manager
 * that is not the default CDI-managed time-series database.
 * </p>
 */
@ApplicationScoped
public class TimeSeriesTemplateProducer implements Function<DatabaseManager, TimeSeriesTemplate> {

    @Inject
    private EntityConverter converter;

    @Inject
    private EventPersistManager eventManager;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;


    /**
     * Associates a mapping template with the supplied database manager.
     *
     * @param manager the manager that will execute the template operations
     * @return a time-series template backed by the manager
     * @throws NullPointerException when the manager is {@code null}
     */
    @Override
    public TimeSeriesTemplate apply(DatabaseManager manager) {
        Objects.requireNonNull(manager, "manager is required");
        return new ProducerTimeSeriesTemplate(converter, manager,
                eventManager, entities, converters);
    }

    @Vetoed
    static class ProducerTimeSeriesTemplate  extends AbstractSemiStructuredTemplate implements TimeSeriesTemplate {

        private final EntityConverter converter;

        private final  DatabaseManager manager;

        private final EventPersistManager eventManager;

        private final EntitiesMetadata entities;

        private final  Converters converters;

        ProducerTimeSeriesTemplate(EntityConverter converter,
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

        ProducerTimeSeriesTemplate() {
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
