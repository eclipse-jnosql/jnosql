/*
 *  Copyright (c) 2022,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.timeseries.spi;


import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessProducer;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.DatabaseMetadata;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.Databases;
import org.eclipse.jnosql.mapping.timeseries.query.CustomRepositoryTimeSeriesBean;
import org.eclipse.jnosql.mapping.timeseries.query.RepositoryTimeSeriesBean;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;


/**
 * Enables CDI integration for time-series mapping.
 * <p>
 * The extension discovers {@link DatabaseType#TIME_SERIES} managers and makes the corresponding
 * {@code TimeSeriesTemplate}, Jakarta Data repositories, and custom repositories available for injection.
 * Named managers are exposed through the provider declared in {@code @Database}.
 * </p>
 */
public class TimeSeriesExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(TimeSeriesExtension.class.getName());

    private final Set<DatabaseMetadata> databases = new HashSet<>();


    <T, X extends DatabaseManager> void observes(@Observes final ProcessProducer<T, X> pp) {
        Databases.addDatabase(pp, DatabaseType.TIME_SERIES, databases);
    }


    void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery) {

        ClassScanner scanner = ClassScanner.load();

        Set<Class<?>> crudTypes = scanner.repositoriesStandard();

        Set<Class<?>> customRepositories = scanner.customRepositories();

        LOGGER.info(() -> String.format("Processing TimeSeries extension: %d databases crud %d found, custom repositories: %d",
                databases.size(), crudTypes.size(), customRepositories.size()));
        LOGGER.info(() -> "Processing repositories as a TimeSeries implementation: " + crudTypes);

        databases.forEach(type -> {
            if (!type.getProvider().isBlank()) {
                final TemplateBean bean = new TemplateBean(type.getProvider());
                afterBeanDiscovery.addBean(bean);
            }
        });

        crudTypes.forEach(type -> {
            if (!databases.contains(DatabaseMetadata.DEFAULT_TIME_SERIES)) {
                afterBeanDiscovery.addBean(new RepositoryTimeSeriesBean<>(type, ""));
            }
            databases.forEach(database ->
                afterBeanDiscovery.addBean(new RepositoryTimeSeriesBean<>(type, database.getProvider())));
        });

        customRepositories.forEach(type -> {
            if (!databases.contains(DatabaseMetadata.DEFAULT_TIME_SERIES)) {
                afterBeanDiscovery.addBean(new CustomRepositoryTimeSeriesBean<>(type, ""));
            }
            databases.forEach(database ->
                    afterBeanDiscovery.addBean(new CustomRepositoryTimeSeriesBean<>(type, database.getProvider())));
        });

    }

}
