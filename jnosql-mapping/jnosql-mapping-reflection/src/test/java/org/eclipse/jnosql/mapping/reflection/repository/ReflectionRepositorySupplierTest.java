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

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryParam;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryType;
import org.eclipse.jnosql.mapping.reflection.entities.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionRepositorySupplierTest {

    private ReflectionRepositorySupplier supplier = new ReflectionRepositorySupplier();

    @Test
    @DisplayName("Should return an error when its not an interface")
    void shouldReturnErrorWhenItsNotAnInterface() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> supplier.apply(ReflectionRepositorySupplierTest.class));
        assertEquals("The type " + ReflectionRepositorySupplierTest.class.getName() + " is not an interface",
                exception.getMessage());
    }

    @Test
    @DisplayName("Should return an error when its null")
    void shouldReturnErrorWhenItsNull() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> supplier.apply(null));
        assertEquals("type is required", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(classes = {BasicEmptyPersonRepository.class, EmptyPersonRepository.class})
    @DisplayName("Should return RepositoryMetadata instance")
    void shouldReturnRepositoryMetadataInstance(Class<?> type) {
        RepositoryMetadata metadata = supplier.apply(type);
        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(metadata).isNotNull();
            soft.assertThat(metadata).isInstanceOf(ReflectionRepositoryMetadata.class);
            soft.assertThat(metadata.entity()).get().isEqualTo(Person.class);
            soft.assertThat(metadata.type()).isEqualTo(type);
            soft.assertThat(metadata.methods()).isNotNull().isEmpty();
        });
    }

    @Test
    @DisplayName("Should return custom repository with no methods")
    void shouldReturnCustomRepositoryNoMethods() {
        RepositoryMetadata metadata = supplier.apply(PersonCustomEmptyRepository.class);
        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(metadata).isNotNull();
            soft.assertThat(metadata).isInstanceOf(ReflectionRepositoryMetadata.class);
            soft.assertThat(metadata.entity()).isEmpty();
            soft.assertThat(metadata.type()).isEqualTo(PersonCustomEmptyRepository.class);
            soft.assertThat(metadata.methods()).isNotNull().isEmpty();
        });
    }

    @Test
    @DisplayName("Should return method query findByName")
    void shouldMethodQueryFindByName(){
        RepositoryMetadata metadata = supplier.apply(PersonRepository.class);
        Optional<RepositoryMethod> findByName = metadata.find("findByName");
        SoftAssertions.assertSoftly(soft ->{
           soft.assertThat(findByName).isPresent();
           var method = findByName.orElseThrow();
           soft.assertThat(method.name()).isEqualTo("findByName");
           soft.assertThat(method.query()).isEmpty();
           soft.assertThat(method.returnType().orElseThrow()).isEqualTo(List.class);
            soft.assertThat(method.elementType().orElseThrow()).isEqualTo(Person.class);
            soft.assertThat(method.sorts()).isEmpty();
            soft.assertThat(method.first()).isEmpty();
            soft.assertThat(method.type()).isEqualTo(RepositoryType.FIND_BY);
        });
    }

    @DisplayName("Should verify params on findByName")
    @Test
    void shouldVerifyParamsOnFindByName() {

        RepositoryMetadata metadata = supplier.apply(PersonRepository.class);
        Optional<RepositoryMethod> findByName = metadata.find("findByName");
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(findByName).isPresent();
            var method = findByName.orElseThrow();
            List<RepositoryParam> params = method.params();
            soft.assertThat(params).isNotEmpty().hasSize(1);
            RepositoryParam repositoryParam = params.get(0);
            soft.assertThat(repositoryParam.name()).isEqualTo("name");
            soft.assertThat(repositoryParam.is()).isEmpty();
            soft.assertThat(repositoryParam.by()).isEmpty();

        });
    }


}