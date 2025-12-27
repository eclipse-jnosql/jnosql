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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.keyvalue.BucketManagerFactory;
import org.eclipse.jnosql.communication.keyvalue.KeyValueConfiguration;
import org.eclipse.jnosql.mapping.core.config.MicroProfileSettings;
import org.eclipse.jnosql.mapping.reflection.Reflections;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.KEY_VALUE_PROVIDER;

@ApplicationScoped
class BucketManagerFactorySupplier implements Supplier<BucketManagerFactory> {

    private static final Logger LOGGER = Logger.getLogger(BucketManagerFactorySupplier.class.getName());

    @Override
    @Produces
    @ApplicationScoped
    public BucketManagerFactory get() {

        Settings settings = MicroProfileSettings.INSTANCE;

        KeyValueConfiguration configuration = settings.get(KEY_VALUE_PROVIDER, Class.class)
                .filter(KeyValueConfiguration.class::isAssignableFrom)
                .map(c -> (KeyValueConfiguration) Reflections.newInstance(c))
                .orElseGet(KeyValueConfiguration::getConfiguration);

        return configuration.apply(settings);
    }

    public void close(@Disposes BucketManagerFactory manager) {
        LOGGER.log(Level.FINEST, "Closing BucketManagerFactory");
        manager.close();
    }
}
