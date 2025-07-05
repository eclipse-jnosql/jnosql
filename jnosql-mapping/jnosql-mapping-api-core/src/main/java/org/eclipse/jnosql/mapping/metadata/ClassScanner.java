/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.metadata;


import jakarta.data.repository.DataRepository;
import org.eclipse.jnosql.communication.ServiceProviderLoader;

import java.util.ServiceLoader;
import java.util.Set;

/**
 * This interface defines a scanner for classes that are annotated with both the {@link jakarta.nosql.Entity}
 * and {@link jakarta.nosql.Embeddable} annotations, as well as repositories: interfaces that
 * extend {@link jakarta.data.repository.DataRepository} and are annotated with {@link jakarta.data.repository.Repository}.
 * The scanner facilitates the discovery of entities and repositories in the Eclipse JNoSQL context.
 */
public interface ClassScanner {
    ClassScanner INSTANCE = ServiceProviderLoader.loadSingleton(ClassScanner.class,
            new MetadataException("No implementation of ClassScanner found via ServiceLoader"));
    /**
     * Returns a set of classes that are annotated with the {@link jakarta.nosql.Entity} annotation.
     *
     * @return A set of classes with the {@link jakarta.nosql.Entity} annotation.
     */
    Set<Class<?>> entities();

    /**
     * Returns a set of repository interfaces that extend {@link jakarta.data.repository.DataRepository}
     * and are annotated with {@link jakarta.data.repository.Repository}.
     *
     * @return A set of repository interfaces.
     */
    Set<Class<?>> repositories();

    /**
     * Returns a set of classes that are annotated with the {@link jakarta.nosql.Embeddable} annotation.
     *
     * @return A set of classes with the {@link jakarta.nosql.Embeddable} annotation.
     */
    Set<Class<?>> embeddables();

    /**
     * Returns a set of repository interfaces that are assignable from the given filter type.
     *
     * @param filter The repository filter.
     * @param <T>    The repository type.
     * @return A set of repository interfaces that match the filter criteria.
     */
    <T extends DataRepository<?, ?>> Set<Class<?>> repositories(Class<T> filter);

    /**
     * Returns a set of repository interfaces that directly extend both
     * {@link jakarta.data.repository.BasicRepository}, {@link org.eclipse.jnosql.mapping.NoSQLRepository}
     * and {@link jakarta.data.repository.CrudRepository}.
     *
     * @return A set of standard repository interfaces.
     */
    Set<Class<?>> repositoriesStandard();

    /**
     * Returns a set of custom repository interfaces that are not standard repositories.
     *
     * @return A set of custom repository interfaces.
     */
    Set<Class<?>> customRepositories();

    /**
     * Returns a set of classes that are annotated with the {@link org.eclipse.jnosql.mapping.Projection} annotation.
     *
     * @return A set of classes with the {@link org.eclipse.jnosql.mapping.Projection} annotation.
     */
    Set<Class<?>> projections();


    /**
     * Loads and returns an instance of the {@link ClassScanner} implementation using the ServiceLoader mechanism.
     *
     * @return An instance of the loaded {@link ClassScanner} implementation.
     * @throws IllegalStateException If no suitable implementation is found.
     */
    static ClassScanner load() {
        return INSTANCE;
    }

}