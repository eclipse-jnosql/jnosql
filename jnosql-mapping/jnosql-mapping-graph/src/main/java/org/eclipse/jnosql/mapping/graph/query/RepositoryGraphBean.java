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
package org.eclipse.jnosql.mapping.graph.query;

import jakarta.data.repository.DataRepository;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.graph.GraphTemplate;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.RepositoryBean;


/**
 * A CDI bean for dynamically creating repository implementations for graph databases.
 * <p>
 * This class extends {@link AbstractBean} and provides integration with JNoSQL's
 * custom repository handling mechanism. It facilitates the creation of repositories
 * with support for CDI discovery and dependency injection.
 * </p>
 *
 * @param <T> the type of the repository interface
 * @see AbstractBean
 */
public class RepositoryGraphBean<T extends DataRepository<T, ?>> extends RepositoryBean<T> {

    public RepositoryGraphBean(Class<?> type, String provider) {
        super(type, provider, DatabaseType.GRAPH);
    }

    @Override
    protected Class<? extends SemiStructuredTemplate> getTemplateClass() {
        return GraphTemplate.class;
    }
}