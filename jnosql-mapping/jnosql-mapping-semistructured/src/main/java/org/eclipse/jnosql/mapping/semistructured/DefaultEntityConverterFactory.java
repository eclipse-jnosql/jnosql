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
 *   Michele Rastelli
 */
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.IdFieldNameSupplier;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;

@ApplicationScoped
class DefaultEntityConverterFactory implements EntityConverterFactory {

    private final EntitiesMetadata entities;
    private final Converters converters;
    private final ProjectorConverter projectorConverter;
    @Inject
    DefaultEntityConverterFactory(EntitiesMetadata entities, Converters converters, ProjectorConverter projectorConverter) {
        this.entities = entities;
        this.converters = converters;
        this.projectorConverter = projectorConverter;
    }

    DefaultEntityConverterFactory() {
        this(null, null, null);
    }

    @Override
    public EntityConverter create(IdFieldNameSupplier idFieldNameSupplier) {
        return new DefaultEntityConverter(entities, converters, projectorConverter, idFieldNameSupplier);
    }
}
