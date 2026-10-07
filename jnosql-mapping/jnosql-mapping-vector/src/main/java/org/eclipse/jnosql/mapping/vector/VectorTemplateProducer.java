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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.EntityConverterFactory;
import org.eclipse.jnosql.mapping.semistructured.EventPersistManager;

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Creates templates for application-managed vector managers or named CDI providers.
 * The caller retains responsibility for closing an application-managed manager.
 */
@ApplicationScoped
public class VectorTemplateProducer implements Function<VectorManager, VectorTemplate> {

    @Inject
    private EntityConverterFactory converter;

    @Inject
    private EventPersistManager eventManager;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    /**
     * Creates a template backed by the supplied manager using the shared mapping services.
     * Vector search operations are delegated to the supplied manager.
     *
     * @param manager the vector manager that executes persistence operations
     * @return a vector template backed by the manager
     * @throws NullPointerException when manager is null
     */
    @Override
    public VectorTemplate apply(VectorManager manager) {
        requireNonNull(manager, "manager is required");
        return new DefaultVectorTemplate(converter, manager, eventManager, entities, converters);
    }
}
