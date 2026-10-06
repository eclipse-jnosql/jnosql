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
package org.eclipse.jnosql.mapping.vector.spi;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessProducer;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.DatabaseMetadata;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.Databases;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.eclipse.jnosql.mapping.vector.query.CustomRepositoryVectorBean;
import org.eclipse.jnosql.mapping.vector.query.RepositoryVectorBean;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Discovers vector manager producers and registers their named templates,
 * Jakarta Data repositories, and custom repositories for CDI injection.
 */
public class VectorExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(VectorExtension.class.getName());

    private final Set<DatabaseMetadata> databases = new HashSet<>();

    <T, X extends DatabaseManager> void observes(@Observes ProcessProducer<T, X> producer) {
        Databases.addDatabase(producer, DatabaseType.VECTOR, databases);
    }

    void onAfterBeanDiscovery(@Observes AfterBeanDiscovery discovery) {
        ClassScanner scanner = ClassScanner.load();
        Set<Class<?>> repositories = scanner.repositoriesStandard();
        Set<Class<?>> customRepositories = scanner.customRepositories();

        LOGGER.fine(() -> String.format("Processing vector extension: %d databases, %d repositories, %d custom repositories",
                databases.size(), repositories.size(), customRepositories.size()));

        databases.stream().filter(database -> !database.getProvider().isBlank())
                .forEach(database -> discovery.addBean(new TemplateBean(database.getProvider())));

        Set<DatabaseMetadata> repositoryDatabases = new HashSet<>(databases);
        repositoryDatabases.add(DatabaseMetadata.DEFAULT_VECTOR);
        repositoryDatabases.forEach(database -> {
            repositories.forEach(type -> discovery.addBean(new RepositoryVectorBean<>(type, database.getProvider())));
            customRepositories.forEach(type ->
                    discovery.addBean(new CustomRepositoryVectorBean<>(type, database.getProvider())));
        });
    }
}
