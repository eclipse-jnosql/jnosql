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
package org.eclipse.jnosql.mapping.timeseries.query;

import jakarta.data.repository.DataRepository;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.timeseries.TimeSeriesTemplate;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.RepositoryBean;

/**
 * Registers a Jakarta Data repository to execute against a time-series database.
 * The provider selects which configured database backs the repository.
 *
 * @param <T> the repository type
 */
public class RepositoryTimeSeriesBean<T extends DataRepository<T, ?>> extends RepositoryBean<T> {

    /**
     * Registers the repository type for the selected time-series provider.
     *
     * @param type the repository type
     * @param provider the provider name, or an empty value for the default database
     */
    public RepositoryTimeSeriesBean(Class<?> type, String provider) {
        super(type, provider, DatabaseType.TIME_SERIES);
    }

    @Override
    protected Class<? extends SemiStructuredTemplate>  getTemplateClass() {
        return TimeSeriesTemplate.class;
    }
}