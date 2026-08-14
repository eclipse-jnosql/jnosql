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
package org.eclipse.jnosql.mapping.core.repository.operations;

import static org.assertj.core.api.Assertions.assertThatCode;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;
import jakarta.nosql.Convert;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.core.VetedConverter;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.ReflectionClassConverter;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = EntitiesMetadata.class)
@AddPackages(value = VetedConverter.class)
@AddPackages(value = InfrastructureOperatorProvider.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
@AddPackages(value = ReflectionClassConverter.class)
class CoreBaseRepositoryOperationProviderTest {

    @Inject
    private CoreBaseRepositoryOperationProvider provider;

    @DisplayName("Should have default constructor")
    @Test
    void shouldHaveDefaultConstructor() {
        var repositoryOperationProvider = new CoreBaseRepositoryOperationProvider();
        assertThat(repositoryOperationProvider).isNotNull();
    }

    @DisplayName("Should inject provider")
    @Test
    void shouldInjectProvider() {
        assertThat(provider).isNotNull();
    }

    @DisplayName("Should return insert operation")
    @Test
    void shouldReturnInsertOperation() {
        assertThatCode(() -> provider.insertOperation())
                .doesNotThrowAnyException();
    }

    @DisplayName("Should return update operation")
    @Test
    void shouldReturnUpdateOperation() {
        assertThatCode(() -> provider.updateOperation())
                .doesNotThrowAnyException();
    }

    @DisplayName("Should return delete operation")
    @Test
    void shouldReturnDeleteOperation() {
        assertThatCode(() -> provider.deleteOperation())
                .doesNotThrowAnyException();
    }

    @DisplayName("Should return save operation")
    @Test
    void shouldReturnSaveOperation() {
        assertThatCode(() -> provider.saveOperation())
                .doesNotThrowAnyException();
    }

    @DisplayName("Should return provider operation")
    @Test
    void shouldReturnProviderOperation() {
        assertThatCode(() -> provider.providerOperation())
                .doesNotThrowAnyException();
    }


    @DisplayName("Should fail on find by operation")
    @Test
    void shouldFailOnFindByOperation() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.findByOperation());
    }

    @DisplayName("Should fail on find all operation")
    @Test
    void shouldFailOnFindAllOperation() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.findAllOperation());
    }

    @DisplayName("Should fail on count by operation")
    @Test
    void shouldFailOnCountByOperation() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.countByOperation());
    }

    @DisplayName("Should fail on count all operation")
    @Test
    void shouldFailOnCountAllOperation() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.countAllOperation());
    }

    @DisplayName("Should fail on exists by operation")
    @Test
    void shouldFailOnExistsByOperation() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.existsByOperation());
    }

    @DisplayName("Should fail on delete by operation")
    @Test
    void shouldFailOnDeleteByOperation() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.deleteByOperation());
    }

    @DisplayName("Should fail on parameter based operation")
    @Test
    void shouldFailOnParameterBasedOperation() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.parameterBasedOperation());
    }

    @DisplayName("Should fail on cursor pagination operation")
    @Test
    void shouldFailOnCursorPaginationOperation() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.cursorPaginationOperation());
    }

    @DisplayName("Should fail on query operation")
    @Test
    void shouldFailOnQueryOperation() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.queryOperation());
    }

}
