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
package org.eclipse.jnosql.mapping.core.repository;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.VetedConverter;
import org.eclipse.jnosql.mapping.core.entities.Address;
import org.eclipse.jnosql.mapping.core.entities.Person;
import org.eclipse.jnosql.mapping.core.entities.Worker;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
@AddPackages(value = Converters.class)
@AddPackages(value = VetedConverter.class)
@AddPackages(value = Reflections.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
class RepositoryObserverParserTest {

    @Inject
    private EntitiesMetadata entities;









    @Nested
    @DisplayName("When the repository observer parser operates")
    class WhenTheRepositoryObserverParserOperates {

        @DisplayName("Should create instance")
        @Test
        void shouldCreateInstance() {
            EntityMetadata metadata = entities.get(Person.class);
            RepositoryObserverParser parser = RepositoryObserverParser.of(metadata);
            assertThat(parser).isNotNull();
        }
        @DisplayName("Should fire event")
        @Test
        void shouldFireEvent() {
            EntityMetadata metadata = entities.get(Person.class);
            RepositoryObserverParser parser = RepositoryObserverParser.of(metadata);
            assertThat(parser)
                    .extracting(RepositoryObserverParser::name)
                    .isEqualTo(metadata.name());
        }
        @DisplayName("Should keep name")
        @Test
        void shouldKeepName() {
            EntityMetadata metadata = entities.get(Person.class);
            RepositoryObserverParser parser = RepositoryObserverParser.of(metadata);
            assertThat(parser)
                    .extracting(p -> p.field("name"))
                    .isEqualTo("name");
        }
        @DisplayName("Should replace name")
        @Test
        void shouldReplaceName(){
            EntityMetadata metadata = entities.get(Worker.class);
            RepositoryObserverParser parser = RepositoryObserverParser.of(metadata);
            assertThat(parser)
                    .extracting(p -> p.field("salary"))
                    .isEqualTo("money");
        }
        @DisplayName("Should keep when does not find")
        @Test
        void shouldKeepWhenDoesNotFind() {
            EntityMetadata metadata = entities.get(Address.class);
            RepositoryObserverParser parser = RepositoryObserverParser.of(metadata);
            assertThat(parser)
                    .extracting(p -> p.field("not-found"))
                    .isEqualTo("not-found");
        }
        @DisplayName("Should concat smart")
        @Test
        void shouldConcatSmart() {
            EntityMetadata metadata = entities.get(Address.class);
            RepositoryObserverParser parser = RepositoryObserverParser.of(metadata);
            assertThat(parser)
                    .extracting(p -> p.field("zipCodePlusFour"))
                    .isEqualTo("zipCode.plusFour");
        }
    }
}
