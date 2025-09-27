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
package org.eclipse.jnosql.mapping.keyvalue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;

/**
 * The default implementation of {@link KeyValueEntityConverter}
 */
@ApplicationScoped
class DefaultKeyValueEntityConverter extends KeyValueEntityConverter  {

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    @Override
    protected EntitiesMetadata getEntities() {
        return entities;
    }

    @Override
    protected Converters getConverters() {
        return converters;
    }

}
