/*
 *  Copyright (c) 2017 Otávio Santana and others
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
package org.eclipse.jnosql.mapping.graph.cdi;

import jakarta.nosql.mapping.Database;
import jakarta.nosql.mapping.DatabaseType;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.jnosql.mapping.graph.GraphTraversalSourceSupplier;
import org.mockito.Mockito;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ApplicationScoped
public class GraphProducer {

    private static final Logger LOGGER = Logger.getLogger(GraphProducer.class.getName());

    private Graph graph;

    private String directory;

    @PostConstruct
    public void init() {
        this.directory = new File("").getAbsolutePath() + "/target/jnosql-graph/" + currentTimeMillis() + "/";
        LOGGER.info("Starting Graph database at directory: " + directory);
        this.graph = Neo4jGraph.open(directory);
        LOGGER.info("Graph database created");
    }

    @Produces
    @ApplicationScoped
    public Graph getGraph() {
        return graph;
    }

    @Produces
    @ApplicationScoped
    public GraphTraversalSourceSupplier getGraphTraversalSource() {
        return () -> graph.traversal();
    }

    @Produces
    @ApplicationScoped
    @Database(value = DatabaseType.GRAPH, provider = "graphRepositoryMock")
    public Graph getGraphMock() {

        Graph graphMock = mock(Graph.class);

        Vertex vertex = mock(Vertex.class);

        when(vertex.label()).thenReturn("Person");
        when(vertex.id()).thenReturn(10L);
        when(graphMock.vertices(10L)).thenReturn(Collections.<Vertex>emptyList().iterator());
        when(vertex.keys()).thenReturn(singleton("name"));
        when(vertex.value("name")).thenReturn("nameMock");
        when(graphMock.addVertex(Mockito.anyString())).thenReturn(vertex);

        return graphMock;
    }

    public void dispose(@Disposes Graph graph) throws Exception {
        LOGGER.info("Graph database closing");
        graph.close();
        final Path path = Paths.get(directory);
        if (Files.exists(path)) {
            LOGGER.info("Removing directory graph database: " + directory);
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            LOGGER.info("Graph directory exists?: " + Files.exists(path));
        }
        LOGGER.info("Graph Database closed");
    }
}
