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
package org.eclipse.jnosql.mapping.semistructured;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.IdFieldNameSupplier;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;

import java.util.Optional;

/**
 * The default implementation to {@link EntityConverter}
 */
@ApplicationScoped
public class DefaultEntityConverter extends EntityConverter {
    private final EntitiesMetadata entities;
    private final Converters converters;
    private final ProjectorConverter projectorConverter;
    private final IdFieldNameSupplier idFieldNameSupplier;

    @Inject
    DefaultEntityConverter(EntitiesMetadata entities, Converters converters, ProjectorConverter projectorConverter) {
        this(entities, converters, projectorConverter, Optional::empty);
    }

    DefaultEntityConverter(EntitiesMetadata entities, Converters converters, ProjectorConverter projectorConverter, IdFieldNameSupplier idFieldNameSupplier) {
        this.entities = entities;
        this.converters = converters;
        this.projectorConverter = projectorConverter;
        this.idFieldNameSupplier = idFieldNameSupplier;
    }

    protected DefaultEntityConverter() {
        this(null, null, null, null);
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
    protected ProjectorConverter projectorConverter() {
        return projectorConverter;
    }

    @Override
    protected IdFieldNameSupplier idFieldNameSupplier() {
        return idFieldNameSupplier;
    }
}
