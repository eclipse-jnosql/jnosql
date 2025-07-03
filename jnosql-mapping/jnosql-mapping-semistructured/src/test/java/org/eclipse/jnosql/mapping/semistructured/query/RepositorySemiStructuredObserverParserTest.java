/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class RepositorySemiStructuredObserverParserTest {

    @Inject
    private EntitiesMetadata entitiesMetadata;

    private RepositorySemiStructuredObserverParser parser;

    @BeforeEach
    void setUp() {
        EntityMetadata entityMetadata = entitiesMetadata.get(Person.class);
        this.parser = (RepositorySemiStructuredObserverParser) RepositorySemiStructuredObserverParser.of(entityMetadata);
    }

    @Test
    void shouldCreateFromRepository() {
        EntityMetadata entityMetadata = Mockito.mock(EntityMetadata.class);
        var parser = RepositorySemiStructuredObserverParser.of(entityMetadata);
        assertNotNull(parser);
    }

    @Test
    void shouldFireEntity() {
        String entity = "entity";
        assertEquals("Person", parser.fireEntity(entity));
    }

    @Test
    void shouldFireSelectField() {
       String field = "id";
       assertEquals("_id", parser.fireSelectField("entity", field));
    }

    @Test
    void shouldFireSortProperty() {
        String field = "id";
        assertEquals("_id", parser.fireSortProperty("entity", field));
    }


    @Test
    void shouldFireConditionField() {
        String field = "id";
        assertEquals("_id", parser.fireConditionField("entity", field));
    }
}