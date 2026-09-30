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
package org.eclipse.jnosql.mapping;


import jakarta.enterprise.inject.spi.ProcessProducer;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Class util for check some data during CDI Extensions for Artemis
 */
public final class Databases {

    private static final Logger LOGGER = Logger.getLogger(Databases.class.getName());

    private Databases() {
    }

    /**
     * This method read the class from ProcessProduce, check if the object is a valid object for the database type
     * add it for a list passed for CDI's lifecycle.
     *
     * @param processProducer the {@link ProcessProducer} of CDI Extension
     * @param type            type data which extension is scanning
     * @param databases       list of objects which will be used by Artemis CDI Extension
     * @see DatabaseType
     */
    public static void addDatabase(final ProcessProducer processProducer, final DatabaseType type, final Set<DatabaseMetadata> databases) {

        Set<Annotation> annotations = processProducer.getAnnotatedMember().getAnnotations();
        Optional<Database> databaseOptional = annotations.stream().filter(a -> a instanceof Database)
                .map(Database.class::cast).findFirst();
        databaseOptional.ifPresent(database -> {
            if (type.equals(database.value())) {
                final DatabaseMetadata metadata = DatabaseMetadata.of(database);
                LOGGER.fine(() -> String.format("Found the type %s to metadata %s", type, metadata));
                databases.add(metadata);
            } else {
                String simpleName = processProducer.getAnnotatedMember().getDeclaringType().getJavaClass().getSimpleName();
                LOGGER.fine(() -> String.format("Ignoring because the %s is producing a wrong manager for %s type", simpleName, type));
            }
        });
    }
}
