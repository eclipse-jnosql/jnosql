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
package org.eclipse.jnosql.mapping.graph.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import org.eclipse.jnosql.communication.CommunicationException;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.graph.GraphDatabaseManager;
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

import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.GRAPH_DATABASE;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.GRAPH_PROVIDER;

@ApplicationScoped
class GraphManagerSupplier implements Supplier<DatabaseManager> {

    private static final Logger LOGGER = Logger.getLogger(GraphManagerSupplier.class.getName());

    private static final String DEFAULT_GRAPH_DATABASE = "graph";

    @Override
    @Produces
    @ApplicationScoped
    public GraphDatabaseManager get() {
        Settings settings = MicroProfileSettings.INSTANCE;

        DatabaseConfiguration configuration = settings.get(GRAPH_PROVIDER, Class.class)
                .filter(DatabaseConfiguration.class::isAssignableFrom)
                .map(c -> (DatabaseConfiguration) Reflections.newInstance(c))
                .orElseGet(DatabaseConfiguration::getConfiguration);

        var managerFactory = configuration.apply(settings);

        Optional<String> database = settings.get(GRAPH_DATABASE, String.class);
        String db = database.orElseGet(() ->{
            LOGGER.log(Level.FINE, "The database name is required, default value `{0}` is used", DEFAULT_GRAPH_DATABASE);
            return DEFAULT_GRAPH_DATABASE;
        });
        var manager = managerFactory.apply(db);

        if(manager instanceof GraphDatabaseManager) {
            LOGGER.log(Level.FINEST, "Starting  a GraphManager instance using Eclipse MicroProfile Config," +
                    " database name: " + db);
            return (GraphDatabaseManager) manager;
        }
        throw new CommunicationException("The database manager is not a GraphDatabaseManager instance, " +
                "check the configuration, the current instance is: " + manager.getClass());
    }

    public void close(@Disposes @Database(DatabaseType.GRAPH) DatabaseManager manager) {
        LOGGER.log(Level.FINEST, "Closing GraphManager resource, database name: " + manager.name());
        manager.close();
    }
}
