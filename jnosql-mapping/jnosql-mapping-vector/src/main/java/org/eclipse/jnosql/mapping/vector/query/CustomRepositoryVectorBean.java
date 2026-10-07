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
package org.eclipse.jnosql.mapping.vector.query;

import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.CustomRepositoryBean;
import org.eclipse.jnosql.mapping.vector.VectorTemplate;

/**
 * Registers a custom repository backed by a vector database.
 *
 * @param <T> the repository type
 */
public class CustomRepositoryVectorBean<T> extends CustomRepositoryBean<T> {

    /**
     * Registers the custom repository for the selected vector provider.
     *
     * @param type the repository type
     * @param provider the provider name, or an empty value for the default database
     */
    public CustomRepositoryVectorBean(Class<?> type, String provider) {
        super(type, provider, DatabaseType.VECTOR);
    }

    @Override
    protected Class<? extends SemiStructuredTemplate> getTemplateClass() {
        return VectorTemplate.class;
    }
}
