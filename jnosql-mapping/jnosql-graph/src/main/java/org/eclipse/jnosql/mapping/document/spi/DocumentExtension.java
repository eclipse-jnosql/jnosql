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
package org.eclipse.jnosql.mapping.document.spi;


import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessProducer;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.DatabaseMetadata;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.Databases;
import org.eclipse.jnosql.mapping.document.query.CustomRepositoryDocumentBean;
import org.eclipse.jnosql.mapping.document.query.RepositoryDocumentBean;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;


/**
 * This CDI extension, {@code DocumentExtension}, observes the CDI container lifecycle events to perform tasks related to
 * document-based databases and repository beans.
 * <p>
 * Upon initialization, it maintains a set of {@link DatabaseMetadata} instances representing the document databases.
 * </p>
 */
public class DocumentExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(DocumentExtension.class.getName());

    private final Set<DatabaseMetadata> databases = new HashSet<>();


    <T, X extends DatabaseManager> void observes(@Observes final ProcessProducer<T, X> pp) {
        Databases.addDatabase(pp, DatabaseType.DOCUMENT, databases);
    }


    void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery) {

        ClassScanner scanner = ClassScanner.load();

        Set<Class<?>> crudTypes = scanner.repositoriesStandard();

        Set<Class<?>> customRepositories = scanner.customRepositories();

        LOGGER.info(String.format("Processing Document extension: %d databases crud %d found, custom repositories: %d",
                databases.size(), crudTypes.size(), customRepositories.size()));
        LOGGER.info("Processing repositories as a Document implementation: " + crudTypes);

        databases.forEach(type -> {
            if (!type.getProvider().isBlank()) {
                final TemplateBean bean = new TemplateBean(type.getProvider());
                afterBeanDiscovery.addBean(bean);
            }
        });

        crudTypes.forEach(type -> {
            if (!databases.contains(DatabaseMetadata.DEFAULT_DOCUMENT)) {
                afterBeanDiscovery.addBean(new RepositoryDocumentBean<>(type, ""));
            }
            databases.forEach(database ->
                afterBeanDiscovery.addBean(new RepositoryDocumentBean<>(type, database.getProvider())));
        });

        customRepositories.forEach(type -> {
            if (!databases.contains(DatabaseMetadata.DEFAULT_DOCUMENT)) {
                afterBeanDiscovery.addBean(new CustomRepositoryDocumentBean<>(type, ""));
            }
            databases.forEach(database ->
                    afterBeanDiscovery.addBean(new CustomRepositoryDocumentBean<>(type, database.getProvider())));
        });

    }

}
