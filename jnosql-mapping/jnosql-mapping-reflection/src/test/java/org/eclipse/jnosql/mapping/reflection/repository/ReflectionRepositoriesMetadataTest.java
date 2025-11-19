/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.reflection.repository;

import jakarta.inject.Inject;
import jakarta.nosql.Convert;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.reflection.FieldReader;
import org.eclipse.jnosql.mapping.reflection.entities.AnimalRepository;
import org.eclipse.jnosql.mapping.reflection.entities.Garage;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = FieldReader.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
class ReflectionRepositoriesMetadataTest {

    @Inject
    private RepositoriesMetadata repositoriesMetadata;

    @Inject
    private EntitiesMetadata entitiesMetadata;

    @Test
    void shouldInjectRepository(){
        Assertions.assertThat(repositoriesMetadata).isNotNull();
    }

    @Test
    void shouldReturnNPEErrorWhenTypeIsNull() {
        Assertions.assertThatThrownBy(() -> repositoriesMetadata.get(null))
                .isInstanceOf(NullPointerException.class);
    }


    @Test
    void shouldReturnEmptyWhenTypeIsNull() {
        Assertions.assertThat(repositoriesMetadata.get(Object.class)).isEmpty();
    }

    @Test
    void shouldReturnRepository(){
        Assertions.assertThat(repositoriesMetadata.get(AnimalRepository.class)).isNotEmpty();
    }

    @Test
    void shouldReturnCustomRepository(){
        Assertions.assertThat(repositoriesMetadata.get(Garage.class)).isNotEmpty();
    }

    @Test
    void shouldReceivedProjectorFromLoadRepository() {

    }
}