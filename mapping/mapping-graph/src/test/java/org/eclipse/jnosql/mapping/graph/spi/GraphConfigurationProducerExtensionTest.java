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
package org.eclipse.jnosql.mapping.graph.spi;

import jakarta.nosql.mapping.Database;
import org.eclipse.jnosql.mapping.graph.BookRepository;
import org.eclipse.jnosql.mapping.graph.GraphTemplate;
import org.eclipse.jnosql.mapping.graph.model.Book;
import org.eclipse.jnosql.mapping.graph.model.Person;
import jakarta.nosql.tck.test.CDIExtension;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static jakarta.nosql.mapping.DatabaseType.GRAPH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@CDIExtension
public class GraphConfigurationProducerExtensionTest {

    @Inject
    @Database(value = GRAPH, provider = "graphRepositoryMock")
    private GraphTemplate managerMock;

    @Inject
    private GraphTemplate manager;

    @Inject
    @Database(value = GRAPH, provider = "graphRepositoryMock")
    private BookRepository repositoryMock;

    @Inject
    @Database(value = GRAPH)
    private BookRepository repository;


    @Test
    public void shouldInstance() {
        assertNotNull(manager);
        assertNotNull(managerMock);
    }

    @Test
    public void shouldSave() {
        Person personMock = managerMock.insert(Person.builder().withId(10L).build());

        assertEquals(Long.valueOf(10L), personMock.getId());
    }

    @Test
    public void shouldInjectRepository() {
        repositoryMock.save(Book.builder().withName("book").build());
        assertNotNull(repository.findById("10"));
    }

}