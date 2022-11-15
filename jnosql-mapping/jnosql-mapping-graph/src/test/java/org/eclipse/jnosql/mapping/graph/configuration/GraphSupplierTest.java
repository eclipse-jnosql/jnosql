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

import jakarta.nosql.mapping.MappingException;
import jakarta.nosql.tck.test.CDIExtension;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.mapping.config.MappingConfigurations.GRAPH_PROVIDER;

@CDIExtension
class GraphSupplierTest {

    @Inject
    private GraphSupplier supplier;

    @BeforeEach
    public void beforeEach(){
        System.clearProperty(GRAPH_PROVIDER.get());
    }

    @Test
    public void shouldGetGraph() {
        System.setProperty(GRAPH_PROVIDER.get(), GraphConfigurationMock.class.getName());
        Graph graph = supplier.get();
        Assertions.assertNotNull(graph);
        assertThat(graph).isInstanceOf(GraphConfigurationMock.GraphMock.class);
    }


    @Test
    public void shouldUseDefaultConfigurationWhenProviderIsWrong() {
        System.setProperty(GRAPH_PROVIDER.get(), Integer.class.getName());
        Graph graph = supplier.get();
        Assertions.assertNotNull(graph);
        assertThat(graph).isInstanceOf(GraphConfigurationMock2.GraphMock.class);
    }

    @Test
    public void shouldUseDefaultConfiguration() {
        Graph graph = supplier.get();
        Assertions.assertNotNull(graph);
        assertThat(graph).isInstanceOf(GraphConfigurationMock2.GraphMock.class);
    }

    @Test
    public void shouldReturnErrorWhenThereIsNotDatabase() {
        Assertions.assertThrows(MappingException.class, () -> supplier.get());
    }
}