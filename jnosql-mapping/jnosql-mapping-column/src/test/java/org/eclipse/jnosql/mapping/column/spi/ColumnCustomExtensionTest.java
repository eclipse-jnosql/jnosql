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
package org.eclipse.jnosql.mapping.column.spi;

import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.column.ColumnTemplate;
import org.eclipse.jnosql.mapping.column.MockProducer;
import org.eclipse.jnosql.mapping.column.entities.People;
import org.eclipse.jnosql.mapping.column.entities.Person;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, ColumnTemplate.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, ColumnExtension.class})
@DisplayName("Column custom extension")
class ColumnCustomExtensionTest {

    @Inject
    @Database(value = DatabaseType.COLUMN)
    private People people;

    @Inject
    @Database(value = DatabaseType.COLUMN, provider = "columnRepositoryMock")
    private People pepoleMock;

    @Inject
    private People repository;


    @Nested
    @DisplayName("When injecting custom repositories")
    class WhenTheCustomRepositoryInjection {

        @Test
        @DisplayName("Should inject the default custom repository")
        void shouldInjectDefaultCustomRepository() {

            // When
            Person person = people.insert(Person.builder().build());

            // Then
            assertSoftly(softly -> {
                softly.assertThat(people).isNotNull();
                softly.assertThat(person).isNotNull();
                softly.assertThat(person.getName()).isEqualTo("Default");
            });
        }

        @Test
        @DisplayName("Should inject the provider-specific custom repository")
        void shouldInjectProviderCustomRepository() {

            // When
            Person person = pepoleMock.insert(Person.builder().build());

            // Then
            assertSoftly(softly -> {
                softly.assertThat(pepoleMock).isNotNull();
                softly.assertThat(person).isNotNull();
                softly.assertThat(person.getName()).isEqualTo("columnRepositoryMock");
            });
        }

        @Test
        @DisplayName("Should inject the unqualified custom repository")
        void shouldInjectUnqualifiedCustomRepository() {

            // When
            Person person = repository.insert(Person.builder().build());

            // Then
            assertSoftly(softly -> {
                softly.assertThat(repository).isNotNull();
                softly.assertThat(person).isNotNull();
                softly.assertThat(person.getName()).isEqualTo("Default");
            });
        }
    }
}
