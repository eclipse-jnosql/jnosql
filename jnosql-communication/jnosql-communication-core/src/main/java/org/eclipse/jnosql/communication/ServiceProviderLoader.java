/*
 *
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
 *
 */
package org.eclipse.jnosql.communication;

import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * Utility for lazily loading service provider implementations using ServiceLoader.
 */
public final class ServiceProviderLoader {

    private ServiceProviderLoader() {
    }

    /**
     * Loads a single provider of the given type using {@link ServiceLoader}, lazily.
     *
     * @param service  the service class
     * @param onError  the exception supplier to throw if no implementation is found
     * @param <T>      the service type
     * @return the loaded provider
     */
    public static <T> Supplier<T> lazySingleton(Class<T> service, Supplier<RuntimeException> onError) {
        return new Supplier<>() {
            private volatile T instance;

            @Override
            public T get() {
                if (instance == null) {
                    synchronized (this) {
                        if (instance == null) {
                            instance = ServiceLoader.load(service)
                                    .findFirst()
                                    .orElseThrow(onError);
                        }
                    }
                }
                return instance;
            }
        };
    }

    /**
     * Loads all providers of the given type using {@link ServiceLoader}, lazily.
     *
     * @param service the service class
     * @param <T>     the service type
     * @return a supplier of all loaded providers
     */
    public static <T> Supplier<List<T>> lazyList(Class<T> service) {
        return new Supplier<>() {
            private volatile List<T> providers;

            @Override
            public List<T> get() {
                if (providers == null) {
                    synchronized (this) {
                        if (providers == null) {
                            providers = ServiceLoader.load(service)
                                    .stream()
                                    .map(ServiceLoader.Provider::get)
                                    .toList();
                        }
                    }
                }
                return providers;
            }
        };
    }
}