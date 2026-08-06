/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured.query;

import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

/**
 * Base CDI bean representation for custom semi-structured repositories.
 *
 * @param <T> the repository type
 */
public abstract class CustomRepositoryBean<T> extends BaseRepositoryBean<T> {

    /**
     * Creates a custom semi-structured repository bean.
     *
     * @param type the repository type
     * @param provider the provider name
     * @param databaseType the database type
     */
    public CustomRepositoryBean(Class<?> type, String provider, DatabaseType databaseType) {
        super(type, provider, databaseType);
    }

    @Override
    protected Class<? extends SemiStructuredTemplate> getTemplateClass() {
        return SemiStructuredTemplate.class;
    }

}
