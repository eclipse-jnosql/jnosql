/*
 *
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at
 *   https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Mohan Lal
 *
 */
package org.eclipse.jnosql.communication.util;

import org.eclipse.jnosql.communication.TypeReferenceReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ServiceDiscoveryTest {

    private final ClassLoader originalClassLoader =
            Thread.currentThread().getContextClassLoader();

    @AfterEach
    void restoreContextClassLoader() {
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    @Nested
    @DisplayName("When loading a provider")
    class WhenTheProviderLoading {

        @Test
        @DisplayName("Should return a provider from the context class loader")
        void shouldLoadUsingContextClassLoader() {
            // Given
            var service = TypeReferenceReader.class;
            var referenceClass = TypeReferenceReader.class;
            Thread.currentThread().setContextClassLoader(
                    TypeReferenceReader.class.getClassLoader());

            // When
            Optional<TypeReferenceReader> result =
                    ServiceDiscovery.of(service, referenceClass).first();

            // Then
            assertThat(result)
                    .as("provider discovered through the context class loader")
                    .isPresent();
        }

        @Test
        @DisplayName("Should fall back to the reference class loader when the context class loader has no provider")
        void shouldFallBackToReferenceClassLoaderWhenContextCannotFindProvider() {
            // Given
            var service = TypeReferenceReader.class;
            var referenceClass = TypeReferenceReader.class;
            Thread.currentThread().setContextClassLoader(new ClassLoader(null) {
            });

            // When
            Optional<TypeReferenceReader> result =
                    ServiceDiscovery.of(service, referenceClass).first();

            // Then
            assertThat(result)
                    .as("provider discovered through the reference class loader")
                    .isPresent();
        }

        @Test
        @DisplayName("Should fall back to the reference class loader when the context class loader is null")
        void shouldFallBackToReferenceClassLoaderWhenContextClassLoaderIsNull() {
            // Given
            var service = TypeReferenceReader.class;
            var referenceClass = TypeReferenceReader.class;
            Thread.currentThread().setContextClassLoader(null);

            // When
            Optional<TypeReferenceReader> result =
                    ServiceDiscovery.of(service, referenceClass).first();

            // Then
            assertThat(result)
                    .as("provider discovered through the reference class loader")
                    .isPresent();
        }

        @Test
        @DisplayName("Should return an empty optional when neither class loader provides a provider")
        void shouldReturnEmptyWhenNoProviderExists() {
            // Given
            var emptyClassLoader = new EmptyServicesClassLoader();
            Class<?> referenceClass = emptyClassLoader.loadMarker();
            Thread.currentThread().setContextClassLoader(emptyClassLoader);
            var service = TypeReferenceReader.class;

            // When
            Optional<TypeReferenceReader> result =
                    ServiceDiscovery.of(service, referenceClass).first();

            // Then
            assertThat(result)
                    .as("no provider is visible from either class loader")
                    .isEmpty();
        }

        @Test
        @DisplayName("Should return an empty optional when the context and reference class loaders are the same")
        void shouldReturnEmptyWhenContextAndReferenceClassLoadersAreTheSame() {
            // Given
            var emptyClassLoader = new EmptyServicesClassLoader();
            Class<?> referenceClass = emptyClassLoader.loadMarker();
            Thread.currentThread().setContextClassLoader(emptyClassLoader);
            var service = TypeReferenceReader.class;

            // When
            Optional<TypeReferenceReader> result =
                    ServiceDiscovery.of(service, referenceClass).first();

            // Then
            assertThat(result)
                    .as("no provider is visible when both loaders are empty")
                    .isEmpty();
        }
    }

    /**
     * A class loader that defines its own copy of {@link Marker} directly
     * (rather than delegating to its parent), and reports no resources for
     * any {@code META-INF/services} provider-configuration file. Used to
     * deterministically force the "no provider found" outcome of
     * {@link ServiceDiscovery#first()} independent of ambient providers.
     */
    private static final class EmptyServicesClassLoader extends ClassLoader {

        EmptyServicesClassLoader() {
            super(null);
        }

        Class<?> loadMarker() {
            try {
                String path = Marker.class.getName().replace('.', '/') + ".class";
                byte[] bytes;
                try (var in = EmptyServicesClassLoader.class.getClassLoader()
                        .getResourceAsStream(path)) {
                    bytes = Objects.requireNonNull(
                                    in, "Marker class bytes not found")
                            .readAllBytes();
                }
                return defineClass(
                        Marker.class.getName(), bytes, 0, bytes.length);
            } catch (java.io.IOException e) {
                throw new IllegalStateException(
                        "Unable to define Marker for test", e);
            }
        }

        @Override
        protected java.util.Enumeration<java.net.URL> findResources(
                String name) {
            return java.util.Collections.emptyEnumeration();
        }

        @Override
        protected java.net.URL findResource(String name) {
            return null;
        }
    }

    /**
     * A no-op class redefined by {@link EmptyServicesClassLoader} so that
     * its {@code getClassLoader()} reports that isolated loader.
     */
    private static final class Marker {
    }

    @Nested
    @DisplayName("When validating the discovery")
    class WhenTheValidation {

        @Test
        @DisplayName("Should reject a null service type")
        void shouldRejectNullService() {
            // Given
            Class<TypeReferenceReader> service = null;
            var referenceClass = TypeReferenceReader.class;

            // When / Then
            assertThatNullPointerException()
                    .isThrownBy(() ->
                            ServiceDiscovery.of(service, referenceClass))
                    .withMessage("service is required");
        }

        @Test
        @DisplayName("Should reject a null reference class")
        void shouldRejectNullReferenceClass() {
            // Given
            var service = TypeReferenceReader.class;
            Class<?> referenceClass = null;

            // When / Then
            assertThatNullPointerException()
                    .isThrownBy(() ->
                            ServiceDiscovery.of(service, referenceClass))
                    .withMessage("referenceClass is required");
        }
    }

    @Nested
    @DisplayName("When loading all providers")
    class WhenTheAllProviderLoading {

        @Test
        @DisplayName("Should return every provider from the context class loader")
        void shouldLoadAllUsingContextClassLoader() {
            // Given
            var service = TypeReferenceReader.class;
            var referenceClass = TypeReferenceReader.class;
            Thread.currentThread().setContextClassLoader(
                    TypeReferenceReader.class.getClassLoader());

            // When
            var result = ServiceDiscovery.of(service, referenceClass).all();

            // Then
            assertThat(result)
                    .as("providers discovered through the context class loader")
                    .isNotEmpty();
        }

        @Test
        @DisplayName("Should return an empty list when neither class loader provides a provider")
        void shouldReturnEmptyListWhenNoProviderExists() {
            // Given
            var emptyClassLoader = new EmptyServicesClassLoader();
            Class<?> referenceClass = emptyClassLoader.loadMarker();
            Thread.currentThread().setContextClassLoader(emptyClassLoader);
            var service = TypeReferenceReader.class;

            // When
            var result = ServiceDiscovery.of(service, referenceClass).all();

            // Then
            assertThat(result)
                    .as("no providers are visible from either class loader")
                    .isEmpty();
        }

        @Test
        @DisplayName("Should fall back to the reference class loader when the context class loader has no providers")
        void shouldFallBackToReferenceClassLoaderForAll() {
            // Given
            var service = TypeReferenceReader.class;
            var referenceClass = TypeReferenceReader.class;
            Thread.currentThread().setContextClassLoader(new ClassLoader(null) {
            });

            // When
            var result = ServiceDiscovery.of(service, referenceClass).all();

            // Then
            assertThat(result)
                    .as("providers discovered through the reference class loader")
                    .isNotEmpty();
        }
    }
}
