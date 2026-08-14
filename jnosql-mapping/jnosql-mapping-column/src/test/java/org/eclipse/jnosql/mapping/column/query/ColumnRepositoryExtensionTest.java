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
package org.eclipse.jnosql.mapping.column.query;

import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.column.ColumnTemplate;
import org.eclipse.jnosql.mapping.column.MockProducer;
import org.eclipse.jnosql.mapping.column.entities.Person;
import org.eclipse.jnosql.mapping.column.entities.PersonRepository;
import org.eclipse.jnosql.mapping.column.spi.ColumnExtension;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.query.SemiStructuredRepositoryProxy;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, SemiStructuredRepositoryProxy.class})
@AddPackages({MockProducer.class, ColumnTemplate.class, Reflections.class})
@AddExtensions({ReflectionEntityMetadataExtension.class, ColumnExtension.class})
@DisplayName("Column repository extension")
class ColumnRepositoryExtensionTest {

    @Inject
    @Database(value = DatabaseType.COLUMN)
    private PersonRepository repository;

    @Inject
    @Database(value = DatabaseType.COLUMN, provider = "columnRepositoryMock")
    private PersonRepository repositoryMock;

    @Nested
    @DisplayName("When injecting column repositories")
    class WhenTheRepositoryInjection {

        @Test
        @DisplayName("Should inject the default repository")
        void shouldInjectDefaultRepository() {

            // When
            Person person = repository.save(Person.builder().build());

            // Then
            assertSoftly(softly -> {
                softly.assertThat(repository).isNotNull();
                softly.assertThat(person.getName()).isEqualTo("Default");
            });
        }

        @Test
        @DisplayName("Should inject the provider-specific repository")
        void shouldInjectProviderRepository() {

            // When
            Person person = repositoryMock.save(Person.builder().build());

            // Then
            assertSoftly(softly -> {
                softly.assertThat(repositoryMock).isNotNull();
                softly.assertThat(person.getName()).isEqualTo("columnRepositoryMock");
            });
        }
    }
}
