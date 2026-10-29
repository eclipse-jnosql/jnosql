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

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.WeakHashMap;

/**
 * A lazily initialized handle to service providers of a given type.
 *
 * <p>The thread context class loader (TCCL) is tried first, followed by the
 * class loader of the reference class. This supports environments where the
 * context class loader and the class loader that loaded the API classes are
 * different, such as a Quarkus/Vert.x event-loop thread whose context class
 * loader does not see the application's {@code META-INF/services} entries,
 * or an application server that shares one JNoSQL between its applications,
 * where the context class loader sees the providers of the application, for
 * example those of its database driver, and the reference class loader sees
 * the built-in providers of JNoSQL.</p>
 *
 * <p>An instance lazily discovers and resolves the providers as a
 * {@code List}. Discovery runs at most once per instance and context class
 * loader; subsequent calls to {@link #first()} and {@link #all()} on a
 * thread with the same context class loader reuse the retained list rather
 * than invoking {@link ServiceLoader} again. The lists are kept per context
 * class loader so that each application in an application server gets its
 * own providers, and they are referenced weakly so that the applications
 * can still be unloaded. This class does not maintain a global cache of
 * providers or class loaders; the lifecycle of a {@code ServiceDiscovery}
 * instance belongs to its caller.</p>
 *
 * <p>This abstraction is not suitable for service lookups that must execute
 * from within a caller's own named JPMS module. Such call sites may need to
 * invoke {@link ServiceLoader} directly so that the caller module's
 * {@code uses} declaration is honored.</p>
 *
 * @param <T> the service type
 */
public final class ServiceDiscovery<T> {

    private final Class<T> service;
    private final Class<?> referenceClass;

    private final Map<ClassLoader, SoftReference<List<T>>> services = new WeakHashMap<>();

    private ServiceDiscovery(
            Class<T> service,
            Class<?> referenceClass) {
        this.service = Objects.requireNonNull(service, "service is required");
        this.referenceClass = Objects.requireNonNull(
                referenceClass, "referenceClass is required");
    }

    /**
     * Creates a new service discovery structure.
     *
     * @param service the service type
     * @param referenceClass a class whose class loader is searched after the
     *        context class loader
     * @param <T> the service type
     * @return a new {@code ServiceDiscovery} instance
     * @throws NullPointerException if {@code service} or
     *         {@code referenceClass} is {@code null}
     */
    public static <T> ServiceDiscovery<T> of(
            Class<T> service,
            Class<?> referenceClass) {
        return new ServiceDiscovery<>(service, referenceClass);
    }

    /**
     * Returns the first available implementation of this discovery's service
     * type.
     *
     * <p>Discovery runs lazily on first access and is reused by subsequent
     * calls.</p>
     *
     * @return the first available service implementation, or an empty
     *         optional when no implementation is available
     */
    public Optional<T> first() {
        List<T> found = services();
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    /**
     * Returns every available implementation: first those visible through
     * the context class loader, then those visible only through the class
     * loader of the reference class. A provider class visible through both
     * is included once.
     *
     * @return every available service implementation; empty when none are
     *         found
     */
    public List<T> all() {
        return services();
    }

    private List<T> services() {
        ClassLoader contextClassLoader =
                Thread.currentThread().getContextClassLoader();

        synchronized (services) {
            SoftReference<List<T>> retained = services.get(contextClassLoader);
            List<T> found = retained == null ? null : retained.get();
            if (found == null) {
                found = discover(contextClassLoader);
                services.put(contextClassLoader, new SoftReference<>(found));
            }
            return found;
        }
    }

    private List<T> discover(ClassLoader contextClassLoader) {
        Map<String, T> discovered = new LinkedHashMap<>();

        if (contextClassLoader != null) {
            discover(contextClassLoader, discovered);
        }
        discover(referenceClass.getClassLoader(), discovered);

        return List.copyOf(discovered.values());
    }

    private void discover(ClassLoader classLoader, Map<String, T> discovered) {
        ServiceLoader.load(service, classLoader)
                .stream()
                .filter(provider -> !discovered.containsKey(provider.type().getName()))
                .forEach(provider -> discovered.put(provider.type().getName(), provider.get()));
    }
}
