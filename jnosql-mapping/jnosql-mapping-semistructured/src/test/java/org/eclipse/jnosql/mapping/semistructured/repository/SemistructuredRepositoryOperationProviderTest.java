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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class SemistructuredRepositoryOperationProviderTest {
    @Inject
    private RepositoryOperationProvider provider;

    @Test
    void shouldInjectProvider() {
        assertThat(provider)
                .isNotNull()
                .isInstanceOf(SemistructuredRepositoryOperationProvider.class);
    }

    @Test
    void shouldProvideInsertOperation() {
        assertThat(provider.insertOperation()).isNotNull();
    }

    @Test
    void shouldProvideUpdateOperation() {
        assertThat(provider.updateOperation()).isNotNull();
    }

    @Test
    void shouldProvideDeleteOperation() {
        assertThat(provider.deleteOperation()).isNotNull();
    }

    @Test
    void shouldProvideSaveOperation() {
        assertThat(provider.saveOperation()).isNotNull();
    }

    @Test
    void shouldProvideFindByOperation() {
        assertThat(provider.findByOperation()).isNotNull();
    }

    @Test
    void shouldProvideFindAllOperation() {
        assertThat(provider.findAllOperation()).isNotNull();
    }

    @Test
    void shouldProvideCountByOperation() {
        assertThat(provider.countByOperation()).isNotNull();
    }

    @Test
    void shouldProvideCountAllOperation() {
        assertThat(provider.countAllOperation()).isNotNull();
    }

    @Test
    void shouldProvideExistsByOperation() {
        assertThat(provider.existsByOperation()).isNotNull();
    }

    @Test
    void shouldProvideDeleteByOperation() {
        assertThat(provider.deleteByOperation()).isNotNull();
    }

    @Test
    void shouldProvideParameterBasedOperation() {
        assertThat(provider.parameterBasedOperation()).isNotNull();
    }

    @Test
    void shouldProvideCursorPaginationOperation() {
        assertThat(provider.cursorPaginationOperation()).isNotNull();
    }

    @Test
    void shouldProvideQueryOperation() {
        assertThat(provider.queryOperation()).isNotNull();
    }
}