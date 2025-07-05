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

/**
 * Utility for lazily loading service provider implementations using ServiceLoader.
 */
public final class ServiceProviderLoader {

    private ServiceProviderLoader() {
    }

    /**
     * Loads and returns a single provider of the given type using {@link ServiceLoader}, eagerly.
     *
     * @param service the service class
     * @param onError the exception to throw if no implementation is found
     * @param <T>     the service type
     * @return the eagerly loaded service provider
     */
    public static <T> T loadSingleton(Class<T> service, RuntimeException onError) {
        return ServiceLoader.load(service)
                .findFirst()
                .orElseThrow(() -> onError);
    }

    /**
     * Loads and returns all providers of the given type using {@link ServiceLoader}, eagerly.
     *
     * @param service the service class
     * @param <T>     the service type
     * @return an eagerly loaded unmodifiable list of service providers
     */
    public static <T> List<T> loadAll(Class<T> service) {
        return ServiceLoader.load(service)
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList();
    }
}