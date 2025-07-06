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
package org.eclipse.jnosql.mapping.keyvalue.configuration;

import jakarta.data.exceptions.MappingException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.CDI;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.communication.keyvalue.BucketManagerFactory;
import org.eclipse.jnosql.communication.keyvalue.KeyValueConfiguration;
import org.eclipse.jnosql.mapping.core.config.MicroProfileSettings;
import org.eclipse.jnosql.mapping.reflection.Reflections;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.KEY_VALUE_DATABASE;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.KEY_VALUE_PROVIDER;

@ApplicationScoped
class BucketManagerSupplier implements Supplier<BucketManager> {

    private static final Logger LOGGER = Logger.getLogger(BucketManagerSupplier.class.getName());

    @Override
    @Produces
    @ApplicationScoped
    public BucketManager get() {

        Settings settings = MicroProfileSettings.INSTANCE;

        KeyValueConfiguration configuration = settings.get(KEY_VALUE_PROVIDER, Class.class)
                .filter(KeyValueConfiguration.class::isAssignableFrom)
                .map(c -> {
                    final Reflections reflections = CDI.current().select(Reflections.class).get();
                    return (KeyValueConfiguration) reflections.newInstance(c);
                }).orElseGet(KeyValueConfiguration::getConfiguration);

        BucketManagerFactory managerFactory = configuration.apply(settings);

        Optional<String> database = settings.get(KEY_VALUE_DATABASE, String.class);
        String db = database.orElseThrow(() -> new MappingException("Please, inform the database filling up the property "
                + KEY_VALUE_DATABASE.get()));
        BucketManager manager = managerFactory.apply(db);

        LOGGER.log(Level.FINEST, "Starting  a BucketManager instance using Eclipse MicroProfile Config," +
                " database name: " + db);

        return manager;
    }

    public void close(@Disposes BucketManager manager) {
        LOGGER.log(Level.FINEST, "Closing BucketManager resource, database name: " + manager.name());
        manager.close();
    }
}
