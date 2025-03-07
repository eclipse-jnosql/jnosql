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
package org.eclipse.jnosql.mapping.column.query;

import jakarta.data.repository.DataRepository;
import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.column.ColumnTemplate;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.RepositoryBean;

/**
 * This class serves as a JNoSQL discovery bean for CDI extension, responsible for registering Repository instances.
 * It extends {@link AbstractBean} and is parameterized with type {@code T} representing the repository type.
 * <p>
 * Upon instantiation, it initializes with the provided repository type, provider name, and qualifiers.
 * The provider name specifies the database provider for the repository.
 * </p>
 *
 * @param <T> the type of the repository
 * @see AbstractBean
 */
public class RepositoryColumnBean<T extends DataRepository<T, ?>> extends RepositoryBean<T> {

    public RepositoryColumnBean(Class<?> type, String provider) {
        super(type, provider, DatabaseType.COLUMN);
    }

    @Override
    protected Class<? extends SemiStructuredTemplate> getTemplateClass() {
        return ColumnTemplate.class;
    }

    @Override
    protected DatabaseQualifier getDatabaseQualifier() {
        return DatabaseQualifier.ofColumn();
    }

    @Override
    protected DatabaseQualifier getDatabaseQualifier(String provider) {
        return DatabaseQualifier.ofColumn(provider);
    }
}