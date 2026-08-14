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

import jakarta.data.repository.By;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

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

    @DisplayName("Should create from repository")
    @Test
    void shouldCreateFromRepository() {
        EntityMetadata entityMetadata = Mockito.mock(EntityMetadata.class);
        var parser = RepositorySemiStructuredObserverParser.of(entityMetadata);
        assertThat(parser).isNotNull();
    }

    @DisplayName("Should fire entity")
    @Test
    void shouldFireEntity() {
        String entity = "entity";
        assertThat(parser.fireEntity(entity)).isEqualTo("Person");
    }

    @DisplayName("Should fire select field")
    @Test
    void shouldFireSelectField() {
       String field = "id";
       assertThat(parser.fireSelectField("entity", field)).isEqualTo("_id");
    }

    @DisplayName("Should fire sort property")
    @Test
    void shouldFireSortProperty() {
        String field = "id";
        assertThat(parser.fireSortProperty("entity", field)).isEqualTo("_id");
    }


    @DisplayName("Should fire condition field")
    @Test
    void shouldFireConditionField() {
        String field = "id";
        assertThat(parser.fireConditionField("entity", field)).isEqualTo("_id");
    }


    @Nested
    @DisplayName("When Id function is used")
    class WhenIdFunctionUsed {

        @DisplayName("Should fire select field")
        @Test
        void shouldFireSelectField() {
            String field = By.ID;
            assertThat(parser.fireSelectField("entity", field)).isEqualTo("_id");
        }

        @DisplayName("Should fire sort property")
        @Test
        void shouldFireSortProperty() {
            String field = By.ID;
            assertThat(parser.fireSortProperty("entity", field)).isEqualTo("_id");
        }


        @DisplayName("Should fire condition field")
        @Test
        void shouldFireConditionField() {
            String field = By.ID;
            assertThat(parser.fireConditionField("entity", field)).isEqualTo("_id");
        }
    }


}