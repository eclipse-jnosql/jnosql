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
package org.eclipse.jnosql.mapping.semistructured.repository;

import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class SemistructuredRepositoryOperationProviderTest {
    @Inject
    private RepositoryOperationProvider provider;

    @DisplayName("Should inject provider")
    @Test
    void shouldInjectProvider() {
        assertThat(provider)
                .isNotNull()
                .isInstanceOf(SemistructuredRepositoryOperationProvider.class);
    }

    @DisplayName("Should provide insert operation")
    @Test
    void shouldProvideInsertOperation() {
        assertThat(provider.insertOperation()).isNotNull();
    }

    @DisplayName("Should provide update operation")
    @Test
    void shouldProvideUpdateOperation() {
        assertThat(provider.updateOperation()).isNotNull();
    }

    @DisplayName("Should provide delete operation")
    @Test
    void shouldProvideDeleteOperation() {
        assertThat(provider.deleteOperation()).isNotNull();
    }

    @DisplayName("Should provide save operation")
    @Test
    void shouldProvideSaveOperation() {
        assertThat(provider.saveOperation()).isNotNull();
    }

    @DisplayName("Should provide find by operation")
    @Test
    void shouldProvideFindByOperation() {
        assertThat(provider.findByOperation()).isNotNull();
    }

    @DisplayName("Should provide find all operation")
    @Test
    void shouldProvideFindAllOperation() {
        assertThat(provider.findAllOperation()).isNotNull();
    }

    @DisplayName("Should provide count by operation")
    @Test
    void shouldProvideCountByOperation() {
        assertThat(provider.countByOperation()).isNotNull();
    }

    @DisplayName("Should provide count all operation")
    @Test
    void shouldProvideCountAllOperation() {
        assertThat(provider.countAllOperation()).isNotNull();
    }

    @DisplayName("Should provide exists by operation")
    @Test
    void shouldProvideExistsByOperation() {
        assertThat(provider.existsByOperation()).isNotNull();
    }

    @DisplayName("Should provide delete by operation")
    @Test
    void shouldProvideDeleteByOperation() {
        assertThat(provider.deleteByOperation()).isNotNull();
    }

    @DisplayName("Should provide parameter based operation")
    @Test
    void shouldProvideParameterBasedOperation() {
        assertThat(provider.parameterBasedOperation()).isNotNull();
    }

    @DisplayName("Should provide cursor pagination operation")
    @Test
    void shouldProvideCursorPaginationOperation() {
        assertThat(provider.cursorPaginationOperation()).isNotNull();
    }

    @DisplayName("Should provide query operation")
    @Test
    void shouldProvideQueryOperation() {
        assertThat(provider.queryOperation()).isNotNull();
    }

    @DisplayName("Should provider operation")
    @Test
    void shouldProviderOperation(){
        assertThat(provider.providerOperation()).isNotNull();
    }

    @Nested
    @DisplayName("When the semistructured repository operation provider is tested")
    class WhenTheSemistructuredRepositoryOperationProviderIsTested {
    }
}