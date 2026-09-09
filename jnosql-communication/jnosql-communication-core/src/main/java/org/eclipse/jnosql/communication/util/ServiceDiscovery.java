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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * A lazily initialized handle to service providers of a given type.
 *
 * <p>The thread context class loader (TCCL) is tried first. When it cannot
 * discover a provider, the class loader of the reference class is used as a
 * fallback. This supports environments where the context class loader and
 * the class loader that loaded the API classes are different, such as a
 * Quarkus/Vert.x event-loop thread whose context class loader does not see
 * the application's {@code META-INF/services} entries.</p>
 *
 * <p>An instance lazily discovers and resolves the providers as a
 * {@code List}. Discovery runs at most once per instance; subsequent calls
 * to {@link #first()} and {@link #all()} reuse the retained list rather
 * than invoking {@link ServiceLoader} again. This class does not maintain a
 * global cache of providers or class loaders; the lifecycle of a
 * {@code ServiceDiscovery} instance belongs to its caller.</p>
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

    private List<T> services;

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
     * @param referenceClass a class whose class loader is used as a fallback
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
     * Returns every available implementation visible through this
     * discovery's selected class loader (see {@link #first()} for how the
     * loader is selected). Providers visible only through the other,
     * non-selected class loader are not included — the two are never
     * merged.
     *
     * @return every available service implementation found through the
     *         selected class loader; empty when none are found
     */
    public List<T> all() {
        return services();
    }

    private synchronized List<T> services() {
        if (services == null) {
            services = discover();
        }
        return services;
    }

    private List<T> discover() {
        ClassLoader contextClassLoader =
                Thread.currentThread().getContextClassLoader();

        if (contextClassLoader != null) {
            List<T> discovered = ServiceLoader.load(service, contextClassLoader)
                    .stream()
                    .map(ServiceLoader.Provider::get)
                    .toList();

            if (!discovered.isEmpty()) {
                return discovered;
            }
        }

        ClassLoader referenceClassLoader = referenceClass.getClassLoader();

        return ServiceLoader.load(service, referenceClassLoader)
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList();
    }
}
