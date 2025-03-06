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

import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.graph.GraphDatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.graph.MockProducer;
import org.eclipse.jnosql.mapping.graph.spi.GraphExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.core.spi.EntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.GRAPH_DATABASE;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.GRAPH_PROVIDER;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({EntityMetadataExtension.class, GraphExtension.class})
class GraphManagerSupplierTest {

    @Inject
    private GraphManagerSupplier supplier;

    @BeforeEach
    void beforeEach(){
        System.clearProperty(GRAPH_PROVIDER.get());
        System.clearProperty(GRAPH_DATABASE.get());
    }

    @Test
    void shouldGetManager() {
        System.setProperty(GRAPH_PROVIDER.get(), GraphConfigurationMock.class.getName());
        System.setProperty(GRAPH_DATABASE.get(), "database");
        DatabaseManager manager = supplier.get();
        Assertions.assertNotNull(manager);
        assertThat(manager).isInstanceOf(GraphConfigurationMock.GraphManagerMock.class);
    }


    @Test
    void shouldUseDefaultConfigurationWhenProviderIsWrong() {
        System.setProperty(GRAPH_PROVIDER.get(), Integer.class.getName());
        System.setProperty(GRAPH_DATABASE.get(), "database");
        DatabaseManager manager = supplier.get();
        Assertions.assertNotNull(manager);
        assertThat(manager).isInstanceOf(GraphConfigurationMock2.GraphManagerMock.class);
    }

    @Test
    void shouldUseDefaultConfiguration() {
        System.setProperty(GRAPH_DATABASE.get(), "database");
        DatabaseManager manager = supplier.get();
        Assertions.assertNotNull(manager);
        assertThat(manager).isInstanceOf(GraphConfigurationMock2.GraphManagerMock.class);
    }

    @Test
    void shouldClose(){
        var manager = Mockito.mock(GraphDatabaseManager.class);
        supplier.close(manager);
        Mockito.verify(manager).close();
    }
}
