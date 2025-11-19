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
package org.eclipse.jnosql.mapping.reflection.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.reflection.ProjectionFound;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;


@ApplicationScoped
class ReflectionRepositoriesMetadata implements RepositoriesMetadata {

    private static final Logger LOGGER = Logger.getLogger(ReflectionRepositoriesMetadata.class.getName());

    private final Map<Class<?>, RepositoryMetadata> repositories = new HashMap<>();


    private final Event<ProjectionFound> projectionFoundEvent;

    @Inject
    ReflectionRepositoriesMetadata(Event<ProjectionFound> projectionFoundEvent) {
        this.projectionFoundEvent = projectionFoundEvent;
    }

    ReflectionRepositoriesMetadata() {
        this(null);
    }

    @Inject
    void init() {
        ClassScanner scanner = ClassScanner.load();
        Set<Class<?>> customRepositories = scanner.customRepositories();
        Set<Class<?>> loadRepositories = scanner.repositories();
        Set<Class<?>> repositoriesType = new HashSet<>(customRepositories.size() + loadRepositories.size());
        repositoriesType.addAll(customRepositories);
        repositoriesType.addAll(loadRepositories);

        LOGGER.fine("Found repositories: " + repositoriesType);
        var supplier = ReflectionRepositorySupplier.INSTANCE;
        for (Class<?> type : repositoriesType) {
            RepositoryMetadata metadata = supplier.apply(type, projectionFoundEvent);
            repositories.put(type, metadata);
        }

    }

    @Override
    public Optional<RepositoryMetadata> get(Class<?> type) {
        Objects.requireNonNull(type, "type is required");
        return Optional.ofNullable(repositories.get(type));
    }
}
