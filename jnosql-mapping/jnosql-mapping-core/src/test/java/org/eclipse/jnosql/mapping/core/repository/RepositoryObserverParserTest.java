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

    @Test
    void shouldCreateInstance() {
        EntityMetadata metadata = entities.get(Person.class);
        RepositoryObserverParser parser = RepositoryObserverParser.of(metadata);
        org.assertj.core.api.Assertions.assertThat(parser).isNotNull();
    }

    @Test
    void shouldFireEvent() {
        EntityMetadata metadata = entities.get(Person.class);
        RepositoryObserverParser parser = RepositoryObserverParser.of(metadata);
        org.assertj.core.api.Assertions.assertThat(parser)
                .extracting(RepositoryObserverParser::name)
                .isEqualTo(metadata.name());
    }


    @Test
    void shouldKeepName() {
        EntityMetadata metadata = entities.get(Person.class);
        RepositoryObserverParser parser = RepositoryObserverParser.of(metadata);
        org.assertj.core.api.Assertions.assertThat(parser)
                .extracting(p -> p.field("name"))
                .isEqualTo("name");
    }

    @Test
    void shouldReplaceName(){
        EntityMetadata metadata = entities.get(Worker.class);
        RepositoryObserverParser parser = RepositoryObserverParser.of(metadata);
        org.assertj.core.api.Assertions.assertThat(parser)
                .extracting(p -> p.field("salary"))
                .isEqualTo("money");
    }

    @Test
    void shouldKeepWhenDoesNotFind() {
        EntityMetadata metadata = entities.get(Address.class);
        RepositoryObserverParser parser = RepositoryObserverParser.of(metadata);
        org.assertj.core.api.Assertions.assertThat(parser)
                .extracting(p -> p.field("not-found"))
                .isEqualTo("not-found");
    }

    @Test
    void shouldConcatSmart() {
        EntityMetadata metadata = entities.get(Address.class);
        RepositoryObserverParser parser = RepositoryObserverParser.of(metadata);
        org.assertj.core.api.Assertions.assertThat(parser)
                .extracting(p -> p.field("zipCodePlusFour"))
                .isEqualTo("zipCode.plusFour");
    }

}