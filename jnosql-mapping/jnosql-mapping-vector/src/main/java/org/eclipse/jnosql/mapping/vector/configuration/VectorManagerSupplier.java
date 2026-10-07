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
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.semistructured.DatabaseConfiguration;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.config.MicroProfileSettings;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.vector.VectorManager;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.VECTOR_DATABASE;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.VECTOR_PROVIDER;

/**
 * Creates the default vector database manager from MicroProfile Config.
 * The producer declares {@link VectorManager} so CDI proxies retain vector-native operations.
 */
@ApplicationScoped
class VectorManagerSupplier implements Supplier<VectorManager> {

    private static final Logger LOGGER = Logger.getLogger(VectorManagerSupplier.class.getName());

    @Override
    @Produces
    @ApplicationScoped
    @Default
    @Database(DatabaseType.VECTOR)
    public VectorManager get() {
        Settings settings = MicroProfileSettings.INSTANCE;

        String db = settings.get(VECTOR_DATABASE, String.class)
                .filter(name -> !name.isBlank())
                .orElseThrow(() -> new MappingException("Please, configure a non-blank database name using "
                        + VECTOR_DATABASE.get()));
        DatabaseConfiguration configuration = configuration(settings);
        var managerFactory = configuration.apply(settings);
        DatabaseManager manager = managerFactory.apply(db);
        if (!(manager instanceof VectorManager vectorManager)) {
            if (manager != null) {
                manager.close();
            }
            throw new MappingException("The vector database provider must return a VectorManager");
        }

        LOGGER.log(Level.FINEST, "Starting  a VectorManager instance using Eclipse MicroProfile Config," +
                " database name: " + db);
        return vectorManager;
    }

    private DatabaseConfiguration configuration(Settings settings) {
        Class<?> type = settings.get(VECTOR_PROVIDER, Class.class).orElse(null);
        if (type == null) {
            return DatabaseConfiguration.getConfiguration();
        }
        if (!DatabaseConfiguration.class.isAssignableFrom(type)) {
            throw new MappingException("The provider configured by " + VECTOR_PROVIDER.get()
                    + " must implement " + DatabaseConfiguration.class.getName() + ": " + type.getName());
        }
        var configuration = (DatabaseConfiguration) Reflections.newInstance(type);
        if (configuration == null) {
            throw new MappingException("Unable to instantiate the vector database provider configured by "
                    + VECTOR_PROVIDER.get() + ": " + type.getName());
        }
        return configuration;
    }

    /**
     * Releases the default vector manager when its CDI application scope ends.
     *
     * @param manager the manager being removed from the CDI context
     */
    void close(@Disposes @Default @Database(DatabaseType.VECTOR) VectorManager manager) {
        LOGGER.log(Level.FINEST, "Closing the VectorManager instance using Eclipse MicroProfile Config");
        manager.close();
    }
}
