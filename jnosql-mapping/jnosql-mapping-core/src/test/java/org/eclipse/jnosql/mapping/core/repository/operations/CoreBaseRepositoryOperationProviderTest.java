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

    @Test
    @DisplayName("Should create instance using default constructor")
    void shouldHaveDefaultConstructor() {
        var repositoryOperationProvider = new CoreBaseRepositoryOperationProvider();
        Assertions.assertThat(repositoryOperationProvider).isNotNull();
    }

    @Test
    @DisplayName("Should have implementation injected by CDI")
    void shouldInjectProvider() {
        Assertions.assertThat(provider).isNotNull();
    }

    @Test
    @DisplayName("InsertOperation must NOT throw UnsupportedOperationException")
    void shouldReturnInsertOperation() {
        Assertions.assertThatCode(() -> provider.insertOperation())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("UpdateOperation must NOT throw UnsupportedOperationException")
    void shouldReturnUpdateOperation() {
        Assertions.assertThatCode(() -> provider.updateOperation())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("DeleteOperation must NOT throw UnsupportedOperationException")
    void shouldReturnDeleteOperation() {
        Assertions.assertThatCode(() -> provider.deleteOperation())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("SaveOperation must NOT throw UnsupportedOperationException")
    void shouldReturnSaveOperation() {
        Assertions.assertThatCode(() -> provider.saveOperation())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ProviderOperation must NOT throw UnsupportedOperationException")
    void shouldReturnProviderOperation() {
        Assertions.assertThatCode(() -> provider.providerOperation())
                .doesNotThrowAnyException();
    }


    @Test
    void shouldFailOnFindByOperation() {
        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.findByOperation());
    }

    @Test
    void shouldFailOnFindAllOperation() {
        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.findAllOperation());
    }

    @Test
    void shouldFailOnCountByOperation() {
        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.countByOperation());
    }

    @Test
    void shouldFailOnCountAllOperation() {
        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.countAllOperation());
    }

    @Test
    void shouldFailOnExistsByOperation() {
        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.existsByOperation());
    }

    @Test
    void shouldFailOnDeleteByOperation() {
        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.deleteByOperation());
    }

    @Test
    void shouldFailOnParameterBasedOperation() {
        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.parameterBasedOperation());
    }

    @Test
    void shouldFailOnCursorPaginationOperation() {
        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.cursorPaginationOperation());
    }

    @Test
    void shouldFailOnQueryOperation() {
        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> provider.queryOperation());
    }

}