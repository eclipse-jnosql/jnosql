/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 */
package org.eclipse.jnosql.mapping.core.config;

import org.eclipse.jnosql.communication.Settings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappingSettingsProviderTest {

    private static final Settings FIRST_SETTINGS = Settings.of(Map.of("provider", "first"));

    private static final Settings SECOND_SETTINGS = Settings.of(Map.of("provider", "second"));

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldReturnMicroProfileSettingsWhenProviderIsNotAvailable() throws Exception {
        try (URLClassLoader classLoader = serviceClassLoader("fallback", null)) {
            assertSame(MicroProfileSettings.INSTANCE, resolve(classLoader));
        }
    }

    @Test
    void shouldResolveProviderBeforeLoadingMicroProfileSettings() throws Exception {
        Path root = serviceRoot("lazy", LazyProvider.class.getName());
        URL mainClasses = MappingSettingsProvider.class.getProtectionDomain().getCodeSource().getLocation();
        URL testClasses = MappingSettingsProviderTest.class.getProtectionDomain().getCodeSource().getLocation();

        try (URLClassLoader classLoader = new MicroProfileBlockingClassLoader(
                new URL[]{root.toUri().toURL(), mainClasses, testClasses}, getClass().getClassLoader())) {
            Class<?> providerType = Class.forName(MappingSettingsProvider.class.getName(), true, classLoader);
            Method resolve = providerType.getMethod("resolve");

            assertSame(Settings.settings(), resolve.invoke(null));
        }
    }

    @Test
    void shouldIgnoreHostileContextClassLoaderProvider() throws Exception {
        try (URLClassLoader hostile = serviceClassLoader("hostile", FirstProvider.class.getName())) {
            ClassLoader previous = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(hostile);

                assertSame(MicroProfileSettings.INSTANCE, MappingSettingsProvider.resolve());
            } finally {
                Thread.currentThread().setContextClassLoader(previous);
            }
        }
    }

    @Test
    void shouldFollowSequentialExplicitClassLoaderChanges() throws Exception {
        try (URLClassLoader first = serviceClassLoader("first", FirstProvider.class.getName());
             URLClassLoader second = serviceClassLoader("second", SecondProvider.class.getName())) {
            assertSame(FIRST_SETTINGS, resolve(first));
            assertSame(SECOND_SETTINGS, resolve(second));
            assertSame(FIRST_SETTINGS, resolve(first));
        }
    }

    @Test
    void shouldSelectFirstProvider() throws Exception {
        String providers = FirstProvider.class.getName() + '\n' + SecondProvider.class.getName();
        try (URLClassLoader classLoader = serviceClassLoader("ordered", providers)) {
            assertSame(FIRST_SETTINGS, resolve(classLoader));
        }
    }

    @Test
    void shouldKeepConcurrentExplicitClassLoadersIsolated() throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try (URLClassLoader first = serviceClassLoader("concurrent-first", FirstProvider.class.getName());
             URLClassLoader second = serviceClassLoader("concurrent-second", SecondProvider.class.getName())) {
            Future<Settings> firstResult = executor.submit(() -> resolveRepeatedly(first, FIRST_SETTINGS, ready, start));
            Future<Settings> secondResult = executor.submit(() ->
                    resolveRepeatedly(second, SECOND_SETTINGS, ready, start));

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            assertSame(FIRST_SETTINGS, firstResult.get(10, TimeUnit.SECONDS));
            assertSame(SECOND_SETTINGS, secondResult.get(10, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void shouldRejectNullProviderSettings() throws Exception {
        try (URLClassLoader classLoader = serviceClassLoader("null", NullProvider.class.getName())) {
            NullPointerException exception = assertThrows(NullPointerException.class, () -> resolve(classLoader));

            assertEquals("MappingSettingsProvider.getSettings() must not return null", exception.getMessage());
        }
    }

    @Test
    void shouldPropagateProviderFailure() throws Exception {
        try (URLClassLoader classLoader = serviceClassLoader("failure", FailingProvider.class.getName())) {
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> resolve(classLoader));

            assertEquals("provider failure", exception.getMessage());
        }
    }

    @Test
    void shouldPropagateProviderConstructionFailure() throws Exception {
        try (URLClassLoader classLoader = serviceClassLoader("construction-failure",
                ConstructionFailingProvider.class.getName())) {
            assertThrows(ServiceConfigurationError.class, () -> resolve(classLoader));
        }
    }

    @Test
    void shouldPropagateMalformedServiceEntry() throws Exception {
        try (URLClassLoader classLoader = serviceClassLoader("malformed", "not.a.MappingSettingsProvider")) {
            assertThrows(ServiceConfigurationError.class, () -> resolve(classLoader));
        }
    }

    private Settings resolve(ClassLoader classLoader) {
        return MappingSettingsResolver.resolve(classLoader);
    }

    private Settings resolveRepeatedly(ClassLoader classLoader, Settings expected,
                                       CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        assertTrue(start.await(10, TimeUnit.SECONDS));
        Settings resolved = null;
        for (int index = 0; index < 100; index++) {
            resolved = MappingSettingsResolver.resolve(classLoader);
            assertSame(expected, resolved);
        }
        return resolved;
    }

    private URLClassLoader serviceClassLoader(String name, String providerName) throws Exception {
        Path root = serviceRoot(name, providerName);
        return new URLClassLoader(new URL[]{root.toUri().toURL()}, getClass().getClassLoader());
    }

    private Path serviceRoot(String name, String providerName) throws Exception {
        Path root = temporaryDirectory.resolve(name);
        if (providerName != null) {
            Path services = root.resolve("META-INF/services");
            Files.createDirectories(services);
            Files.writeString(services.resolve(MappingSettingsProvider.class.getName()), providerName + '\n',
                    StandardCharsets.UTF_8);
        }
        return root;
    }

    public static final class FirstProvider implements MappingSettingsProvider {

        @Override
        public Settings getSettings() {
            return FIRST_SETTINGS;
        }
    }

    public static final class SecondProvider implements MappingSettingsProvider {

        @Override
        public Settings getSettings() {
            return SECOND_SETTINGS;
        }
    }

    public static final class NullProvider implements MappingSettingsProvider {

        @Override
        public Settings getSettings() {
            return null;
        }
    }

    public static final class FailingProvider implements MappingSettingsProvider {

        @Override
        public Settings getSettings() {
            throw new IllegalStateException("provider failure");
        }
    }

    public static final class ConstructionFailingProvider implements MappingSettingsProvider {

        public ConstructionFailingProvider() {
            throw new IllegalStateException("provider construction failure");
        }

        @Override
        public Settings getSettings() {
            return Settings.settings();
        }
    }

    public static final class LazyProvider implements MappingSettingsProvider {

        @Override
        public Settings getSettings() {
            return Settings.settings();
        }
    }

    private static final class MicroProfileBlockingClassLoader extends URLClassLoader {

        private MicroProfileBlockingClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                Class<?> type = findLoadedClass(name);
                if (type == null && MicroProfileSettings.class.getName().equals(name)) {
                    throw new ClassNotFoundException(name);
                }
                if (type == null && isIsolated(name)) {
                    type = findClass(name);
                }
                if (type == null) {
                    type = super.loadClass(name, false);
                }
                if (resolve) {
                    resolveClass(type);
                }
                return type;
            }
        }

        private boolean isIsolated(String name) {
            return MappingSettingsProvider.class.getName().equals(name)
                    || MappingSettingsResolver.class.getName().equals(name)
                    || LazyProvider.class.getName().equals(name);
        }
    }
}
