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
package org.eclipse.jnosql.mapping.graph.spi;


import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessProducer;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.DatabaseMetadata;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.Databases;
import org.eclipse.jnosql.mapping.graph.query.CustomRepositoryGraphBean;
import org.eclipse.jnosql.mapping.graph.query.RepositoryGraphBean;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;


/**
 * This CDI extension, {@code GraphExtension}, observes the CDI container lifecycle events to perform tasks related to
 * graph-based databases and repository beans.
 * <p>
 * Upon initialization, it maintains a set of {@link DatabaseMetadata} instances representing the graph databases.
 * </p>
 */
public class GraphExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(GraphExtension.class.getName());

    private final Set<DatabaseMetadata> databases = new HashSet<>();


    <T, X extends DatabaseManager> void observes(@Observes final ProcessProducer<T, X> pp) {
        Databases.addDatabase(pp, DatabaseType.GRAPH, databases);
    }


    void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery) {

        ClassScanner scanner = ClassScanner.load();

        Set<Class<?>> crudTypes = scanner.repositoriesStandard();

        Set<Class<?>> customRepositories = scanner.customRepositories();

        LOGGER.info(String.format("Processing Graph extension: %d databases crud %d found, custom repositories: %d",
                databases.size(), crudTypes.size(), customRepositories.size()));
        LOGGER.info("Processing repositories as a Graph implementation: " + crudTypes);

        databases.forEach(type -> {
            if (!type.getProvider().isBlank()) {
                final TemplateBean bean = new TemplateBean(type.getProvider());
                afterBeanDiscovery.addBean(bean);
            }
        });

        crudTypes.forEach(type -> {
            if (!databases.contains(DatabaseMetadata.DEFAULT_GRAPH)) {
                afterBeanDiscovery.addBean(new RepositoryGraphBean<>(type, ""));
            }
            databases.forEach(database ->
                afterBeanDiscovery.addBean(new RepositoryGraphBean<>(type, database.getProvider())));
        });

        customRepositories.forEach(type -> {
            if (!databases.contains(DatabaseMetadata.DEFAULT_GRAPH)) {
                afterBeanDiscovery.addBean(new CustomRepositoryGraphBean<>(type, ""));
            }
            databases.forEach(database ->
                    afterBeanDiscovery.addBean(new CustomRepositoryGraphBean<>(type, database.getProvider())));
        });

    }

}
