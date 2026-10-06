/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.vector.configuration;

import jakarta.data.exceptions.MappingException;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.VECTOR_DATABASE;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.VECTOR_PROVIDER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ResourceLock(Resources.SYSTEM_PROPERTIES)
@DisplayName("Vector manager supplier")
class VectorManagerSupplierTest {

    private final VectorManagerSupplier supplier = new VectorManagerSupplier();
    private String originalDatabase;
    private String originalProvider;

    @BeforeEach
    void saveProperties() {
        originalDatabase = System.getProperty(VECTOR_DATABASE.get());
        originalProvider = System.getProperty(VECTOR_PROVIDER.get());
        System.clearProperty(VECTOR_DATABASE.get());
        System.clearProperty(VECTOR_PROVIDER.get());
    }

    @AfterEach
    void restoreProperties() {
        restore(VECTOR_DATABASE.get(), originalDatabase);
        restore(VECTOR_PROVIDER.get(), originalProvider);
    }

    private void restore(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    @Nested
    @DisplayName("When resolving a configured vector manager")
    class WhenTheResolution {

        @Test
        @DisplayName("Should discover a provider when none is explicitly configured")
        void shouldDiscoverProvider() {
            System.setProperty(VECTOR_DATABASE.get(), "articles");

            DatabaseManager manager = supplier.get();

            assertThat(manager.name()).as("discovered provider and database").isEqualTo("VectorConfigurationMock:articles");
        }

        @Test
        @DisplayName("Should select an explicitly configured provider instead of the discovered provider")
        void shouldSelectProvider() {
            System.setProperty(VECTOR_DATABASE.get(), "articles");
            System.setProperty(VECTOR_PROVIDER.get(), ExplicitConfiguration.class.getName());

            DatabaseManager manager = supplier.get();

            assertThat(manager.name()).as("explicit provider and database").isEqualTo("ExplicitConfiguration:articles");
        }

        @Test
        @DisplayName("Should reject a missing database before resolving the provider")
        void shouldRejectMissingDatabase() {
            System.setProperty(VECTOR_PROVIDER.get(), "missing.Provider");

            assertThatThrownBy(supplier::get).isInstanceOf(MappingException.class)
                    .hasMessageContaining(VECTOR_DATABASE.get());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "\t"})
        @DisplayName("Should reject blank database names")
        void shouldRejectBlankDatabase(String database) {
            System.setProperty(VECTOR_DATABASE.get(), database);

            assertThatThrownBy(supplier::get).isInstanceOf(MappingException.class)
                    .hasMessageContaining(VECTOR_DATABASE.get());
        }

        @Test
        @DisplayName("Should reject an incompatible provider rather than silently falling back")
        void shouldRejectInvalidProviderType() {
            System.setProperty(VECTOR_DATABASE.get(), "articles");
            System.setProperty(VECTOR_PROVIDER.get(), String.class.getName());

            assertThatThrownBy(supplier::get).isInstanceOf(MappingException.class)
                    .hasMessageContaining(VECTOR_PROVIDER.get()).hasMessageContaining("must implement");
        }

        @Test
        @DisplayName("Should reject a provider that cannot be constructed")
        void shouldRejectUnconstructableProvider() {
            System.setProperty(VECTOR_DATABASE.get(), "articles");
            System.setProperty(VECTOR_PROVIDER.get(), UnconstructableConfiguration.class.getName());

            assertThatThrownBy(supplier::get).isInstanceOf(MappingException.class)
                    .hasMessageContaining("Unable to instantiate");
        }
    }

    @Nested
    @DisplayName("When disposing the default vector manager")
    class WhenTheDisposal {

        @Test
        @DisplayName("Should close the manager")
        void shouldCloseManager() {
            DatabaseManager manager = mock(DatabaseManager.class);

            supplier.close(manager);

            verify(manager).close();
        }
    }

    public static class ExplicitConfiguration extends VectorConfigurationMock {
    }

    public static class UnconstructableConfiguration extends VectorConfigurationMock {

        public UnconstructableConfiguration() {
            throw new IllegalStateException("Cannot construct provider");
        }
    }
}
