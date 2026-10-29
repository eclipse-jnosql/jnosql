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
import org.eclipse.jnosql.communication.TypeSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
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

    @Nested
    @DisplayName("When both class loaders provide providers")
    class WhenBothClassLoadersProvide {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("Should return the providers of the context class loader and of the reference class loader")
        void shouldMergeContextAndReferenceClassLoaderProviders() throws IOException {
            // Given
            var service = TypeReferenceReader.class;
            var referenceClass = TypeReferenceReader.class;
            Thread.currentThread().setContextClassLoader(extraServicesClassLoader());

            // When
            var result = ServiceDiscovery.of(service, referenceClass).all();

            // Then
            assertThat(result)
                    .as("the provider of the context class loader and the built-in providers")
                    .extracting(provider -> provider.getClass().getName())
                    .contains(ExtraServicesClassLoader.PROVIDER, "org.eclipse.jnosql.communication.reader.ListTypeReferenceReader")
                    .doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("Should return the providers of the current context class loader")
        void shouldKeepProvidersPerContextClassLoader() throws IOException {
            // Given
            var discovery = ServiceDiscovery.of(TypeReferenceReader.class, TypeReferenceReader.class);
            var extraServicesClassLoader = extraServicesClassLoader();

            // When
            Thread.currentThread().setContextClassLoader(extraServicesClassLoader);
            var withExtra = discovery.all();
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            var withoutExtra = discovery.all();

            // Then
            assertThat(withExtra)
                    .as("providers for the class loader that lists the extra provider")
                    .extracting(provider -> provider.getClass().getName())
                    .contains(ExtraServicesClassLoader.PROVIDER);
            assertThat(withoutExtra)
                    .as("providers for a class loader that does not list the extra provider")
                    .isNotEmpty()
                    .extracting(provider -> provider.getClass().getName())
                    .doesNotContain(ExtraServicesClassLoader.PROVIDER);
        }

        private ClassLoader extraServicesClassLoader() throws IOException {
            Path services = Files.writeString(tempDir.resolve(TypeReferenceReader.class.getName()),
                    ExtraServicesClassLoader.PROVIDER);
            return new ExtraServicesClassLoader(services.toUri().toURL());
        }
    }

    /**
     * A class loader whose only {@code META-INF/services} entry lists
     * {@link ExtraTypeReferenceReader}, like the class loader of an application
     * that brings its own provider while the built-in providers are only
     * visible through the reference class loader. It has no parent, so the
     * built-in providers declared by the module are not visible through it
     * either; it defines the provider itself, because {@link java.util.ServiceLoader}
     * ignores providers in named modules that are listed in
     * {@code META-INF/services}, and leaves all other classes to the test's
     * class loader.
     */
    private static final class ExtraServicesClassLoader extends ClassLoader {

        static final String PROVIDER = ExtraTypeReferenceReader.class.getName();

        private final URL services;

        ExtraServicesClassLoader(URL services) {
            super(null);
            this.services = services;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!PROVIDER.equals(name)) {
                return ServiceDiscoveryTest.class.getClassLoader().loadClass(name);
            }
            synchronized (getClassLoadingLock(name)) {
                Class<?> provider = findLoadedClass(name);
                return provider != null ? provider : defineProvider(name);
            }
        }

        private Class<?> defineProvider(String name) throws ClassNotFoundException {
            String path = name.replace('.', '/') + ".class";
            try (var in = ServiceDiscoveryTest.class.getClassLoader().getResourceAsStream(path)) {
                byte[] bytes = Objects.requireNonNull(in, "provider class bytes not found").readAllBytes();
                return defineClass(name, bytes, 0, bytes.length);
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        }

        @Override
        public Enumeration<URL> getResources(String name) {
            if (name.equals("META-INF/services/" + TypeReferenceReader.class.getName())) {
                return Collections.enumeration(List.of(services));
            }
            return Collections.emptyEnumeration();
        }
    }

    /**
     * A provider that only {@link ExtraServicesClassLoader} lists.
     */
    public static final class ExtraTypeReferenceReader implements TypeReferenceReader {

        @Override
        public boolean test(TypeSupplier<?> typeSupplier) {
            return false;
        }

        @Override
        public <T> T convert(TypeSupplier<T> typeReference, Object value) {
            throw new UnsupportedOperationException("Not used by the test");
        }
    }
}
