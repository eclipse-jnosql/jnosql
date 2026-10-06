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
package org.eclipse.jnosql.mapping.vector.configuration;

import jakarta.data.exceptions.MappingException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.semistructured.DatabaseConfiguration;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.config.MicroProfileSettings;
import org.eclipse.jnosql.mapping.reflection.Reflections;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.TIME_SERIES_DATABASE;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.VECTOR_DATABASE;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.VECTOR_PROVIDER;

@ApplicationScoped
class VectorManagerSupplier implements Supplier<DatabaseManager> {

    private static final Logger LOGGER = Logger.getLogger(VectorManagerSupplier.class.getName());

    @Override
    @Produces
    @ApplicationScoped
    @Database(DatabaseType.VECTOR)
    public DatabaseManager get() {
        Settings settings = MicroProfileSettings.INSTANCE;

        DatabaseConfiguration configuration = settings.get(VECTOR_PROVIDER, Class.class)
                .filter(DatabaseConfiguration.class::isAssignableFrom)
                .map(c -> (DatabaseConfiguration) Reflections.newInstance(c)).orElseGet(DatabaseConfiguration::getConfiguration);

        var managerFactory = configuration.apply(settings);

        Optional<String> database = settings.get(VECTOR_DATABASE, String.class);
        String db = database.orElseThrow(() -> new MappingException("Please, inform the database filling up the property "
                + VECTOR_DATABASE.get()));
        DatabaseManager manager = managerFactory.apply(db);

        LOGGER.log(Level.FINEST, "Starting  a VectorManager instance using Eclipse MicroProfile Config," +
                " database name: " + db);
        return manager;
    }

    /**
     * Releases the default vector manager when its CDI application scope ends.
     *
     * @param manager the manager being removed from the CDI context
     */
    void close(@Disposes @Database(DatabaseType.VECTOR) DatabaseManager manager) {
        LOGGER.log(Level.FINEST, "Closing the VectorManager instance using Eclipse MicroProfile Config");
        manager.close();
    }
}
