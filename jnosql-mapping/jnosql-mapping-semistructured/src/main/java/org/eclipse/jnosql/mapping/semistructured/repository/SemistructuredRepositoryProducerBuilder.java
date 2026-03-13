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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.semistructured.repository;

import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.RepositoryOperationProvider;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;

import java.util.Objects;

/**
 * Step builder for creating {@link SemistructuredRepositoryProducer} instances outside a
 * CDI container.
 *
 * <p>
 * Usage example:
 *
 * <pre>{@code
 * SemistructuredRepositoryProducer producer = SemistructuredRepositoryProducerBuilder.builder()
 *         .withEntities(entitiesMetadata)
 *         .withRepositories(repositoriesMetadata)
 *         .withInfrastructure(infrastructureOperatorProvider)
 *         .withOperations(repositoryOperationProvider)
 *         .build();
 * }</pre>
 *
 * <p>
 * All steps are immutable records. Each {@code withXxx()} call returns a new
 * record with the updated value, leaving the original step unchanged.
 * </p>
 *
 * Required dependencies (in order):
 * <ol>
 *   <li>{@link EntitiesMetadata} — entity reflection metadata</li>
 *   <li>{@link RepositoriesMetadata} — repository interface metadata</li>
 *   <li>{@link InfrastructureOperatorProvider} — proxy infrastructure operators</li>
 *   <li>{@link RepositoryOperationProvider} — query and operation execution</li>
 * </ol>
 *
 * <p>
 * The {@code build()} method is available only from {@link OperationsStep}, once all
 * four required dependencies have been provided.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> Instances created by this builder are not managed by
 * any CDI container. The caller is responsible for lifecycle management.
 * If a repository method uses a custom CDI-backed operator, invocation may
 * fail at runtime if no CDI context is active.
 * </p>
 */
public sealed interface SemistructuredRepositoryProducerBuilder
        permits SemistructuredRepositoryProducerBuilder.EntitiesStep,
        SemistructuredRepositoryProducerBuilder.RepositoriesStep,
        SemistructuredRepositoryProducerBuilder.InfraStep,
        SemistructuredRepositoryProducerBuilder.OperationsStep {

    /**
     * Returns a new builder starting from {@link EntitiesStep}.
     * Call {@link EntitiesStep#withEntities(EntitiesMetadata)} to provide the
     * required {@link EntitiesMetadata}.
     *
     * @return the first step of the builder
     */
    static EntitiesStep builder() {
        return new EntitiesStep(null);
    }

    /**
     * First step of the builder. Holds the {@link EntitiesMetadata}.
     *
     * @param entities the entities metadata; may be {@code null} only in the
     *                 initial step returned by {@link #builder()}
     */
    record EntitiesStep(EntitiesMetadata entities) implements SemistructuredRepositoryProducerBuilder {

        /**
         * Returns a new {@code EntitiesStep} with the given entities metadata.
         *
         * @param entities the new entities metadata
         * @return a new {@code EntitiesStep} with the updated metadata
         * @throws NullPointerException if {@code entities} is {@code null}
         */
        public EntitiesStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new EntitiesStep(entities);
        }

        /**
         * Advances to the next step by providing the {@link RepositoriesMetadata}.
         *
         * @param repositories the repositories metadata to use
         * @return a {@link RepositoriesStep} holding both dependencies
         * @throws NullPointerException if {@code repositories} is {@code null}
         */
        public RepositoriesStep withRepositories(RepositoriesMetadata repositories) {
            Objects.requireNonNull(repositories, "repositories is required");
            return new RepositoriesStep(entities, repositories);
        }
    }

    /**
     * Second step of the builder. Holds {@link EntitiesMetadata} and {@link RepositoriesMetadata}.
     *
     * @param entities     the entities metadata; must not be {@code null}
     * @param repositories the repositories metadata; must not be {@code null}
     */
    record RepositoriesStep(EntitiesMetadata entities, RepositoriesMetadata repositories)
            implements SemistructuredRepositoryProducerBuilder {

        /**
         * Returns a new {@code EntitiesStep} with the given entities metadata,
         * discarding the repositories metadata.
         *
         * @param entities the new entities metadata
         * @return a new {@code EntitiesStep} with the updated metadata
         * @throws NullPointerException if {@code entities} is {@code null}
         */
        public EntitiesStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new EntitiesStep(entities);
        }

        /**
         * Returns a new {@code RepositoriesStep} with the given repositories metadata,
         * preserving the current entities metadata.
         *
         * @param repositories the new repositories metadata
         * @return a new {@code RepositoriesStep} with the updated metadata
         * @throws NullPointerException if {@code repositories} is {@code null}
         */
        public RepositoriesStep withRepositories(RepositoriesMetadata repositories) {
            Objects.requireNonNull(repositories, "repositories is required");
            return new RepositoriesStep(entities, repositories);
        }

        /**
         * Advances to the next step by providing the {@link InfrastructureOperatorProvider}.
         *
         * @param infrastructure the infrastructure operator provider to use
         * @return an {@link InfraStep} holding all three dependencies
         * @throws NullPointerException if {@code infrastructure} is {@code null}
         */
        public InfraStep withInfrastructure(InfrastructureOperatorProvider infrastructure) {
            Objects.requireNonNull(infrastructure, "infrastructure is required");
            return new InfraStep(entities, repositories, infrastructure);
        }
    }

    /**
     * Third step of the builder. Holds {@link EntitiesMetadata}, {@link RepositoriesMetadata},
     * and {@link InfrastructureOperatorProvider}.
     *
     * @param entities       the entities metadata; must not be {@code null}
     * @param repositories   the repositories metadata; must not be {@code null}
     * @param infrastructure the infrastructure operator provider; must not be {@code null}
     */
    record InfraStep(EntitiesMetadata entities, RepositoriesMetadata repositories,
                     InfrastructureOperatorProvider infrastructure)
            implements SemistructuredRepositoryProducerBuilder {

        /**
         * Returns a new {@code EntitiesStep} with the given entities metadata,
         * discarding the repositories and infrastructure dependencies.
         *
         * @param entities the new entities metadata
         * @return a new {@code EntitiesStep} with the updated metadata
         * @throws NullPointerException if {@code entities} is {@code null}
         */
        public EntitiesStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new EntitiesStep(entities);
        }

        /**
         * Returns a new {@code RepositoriesStep} with the given repositories metadata,
         * discarding the infrastructure dependency.
         *
         * @param repositories the new repositories metadata
         * @return a new {@code RepositoriesStep} preserving entities
         * @throws NullPointerException if {@code repositories} is {@code null}
         */
        public RepositoriesStep withRepositories(RepositoriesMetadata repositories) {
            Objects.requireNonNull(repositories, "repositories is required");
            return new RepositoriesStep(entities, repositories);
        }

        /**
         * Returns a new {@code InfraStep} with the given infrastructure operator provider,
         * preserving the current entities and repositories metadata.
         *
         * @param infrastructure the new infrastructure operator provider
         * @return a new {@code InfraStep} with the updated infrastructure
         * @throws NullPointerException if {@code infrastructure} is {@code null}
         */
        public InfraStep withInfrastructure(InfrastructureOperatorProvider infrastructure) {
            Objects.requireNonNull(infrastructure, "infrastructure is required");
            return new InfraStep(entities, repositories, infrastructure);
        }

        /**
         * Advances to the final step by providing the {@link RepositoryOperationProvider}.
         *
         * @param operations the repository operation provider to use
         * @return an {@link OperationsStep} holding all four dependencies, ready to build
         * @throws NullPointerException if {@code operations} is {@code null}
         */
        public OperationsStep withOperations(RepositoryOperationProvider operations) {
            Objects.requireNonNull(operations, "operations is required");
            return new OperationsStep(entities, repositories, infrastructure, operations);
        }
    }

    /**
     * Final step of the builder. Holds all four required dependencies.
     * Exposes re-configure methods for each dependency and the {@link #build()} terminal method.
     *
     * @param entities       the entities metadata; must not be {@code null}
     * @param repositories   the repositories metadata; must not be {@code null}
     * @param infrastructure the infrastructure operator provider; must not be {@code null}
     * @param operations     the repository operation provider; must not be {@code null}
     */
    record OperationsStep(EntitiesMetadata entities, RepositoriesMetadata repositories,
                          InfrastructureOperatorProvider infrastructure,
                          RepositoryOperationProvider operations)
            implements SemistructuredRepositoryProducerBuilder {

        /**
         * Returns a new {@code EntitiesStep} with the given entities metadata,
         * discarding all subsequent dependencies.
         *
         * @param entities the new entities metadata
         * @return a new {@code EntitiesStep} with the updated metadata
         * @throws NullPointerException if {@code entities} is {@code null}
         */
        public EntitiesStep withEntities(EntitiesMetadata entities) {
            Objects.requireNonNull(entities, "entities is required");
            return new EntitiesStep(entities);
        }

        /**
         * Returns a new {@code RepositoriesStep} with the given repositories metadata,
         * discarding the infrastructure and operations dependencies.
         *
         * @param repositories the new repositories metadata
         * @return a new {@code RepositoriesStep} preserving entities
         * @throws NullPointerException if {@code repositories} is {@code null}
         */
        public RepositoriesStep withRepositories(RepositoriesMetadata repositories) {
            Objects.requireNonNull(repositories, "repositories is required");
            return new RepositoriesStep(entities, repositories);
        }

        /**
         * Returns a new {@code InfraStep} with the given infrastructure operator provider,
         * discarding the operations dependency.
         *
         * @param infrastructure the new infrastructure operator provider
         * @return a new {@code InfraStep} preserving entities and repositories
         * @throws NullPointerException if {@code infrastructure} is {@code null}
         */
        public InfraStep withInfrastructure(InfrastructureOperatorProvider infrastructure) {
            Objects.requireNonNull(infrastructure, "infrastructure is required");
            return new InfraStep(entities, repositories, infrastructure);
        }

        /**
         * Returns a new {@code OperationsStep} with the given repository operation provider,
         * preserving all other dependencies.
         *
         * @param operations the new repository operation provider
         * @return a new {@code OperationsStep} with the updated operations
         * @throws NullPointerException if {@code operations} is {@code null}
         */
        public OperationsStep withOperations(RepositoryOperationProvider operations) {
            Objects.requireNonNull(operations, "operations is required");
            return new OperationsStep(entities, repositories, infrastructure, operations);
        }

        /**
         * Builds a {@link SemistructuredRepositoryProducer} using the accumulated dependencies.
         *
         * <p>All four dependencies must be non-null. This method validates each one
         * with {@link Objects#requireNonNull}.</p>
         *
         * @return a new, unmanaged {@link SemistructuredRepositoryProducer} instance
         * @throws NullPointerException if any dependency is {@code null}
         */
        public SemistructuredRepositoryProducer build() {
            Objects.requireNonNull(entities, "entities is required");
            Objects.requireNonNull(repositories, "repositories is required");
            Objects.requireNonNull(infrastructure, "infrastructure is required");
            Objects.requireNonNull(operations, "operations is required");
            return new SemistructuredRepositoryProducer(entities, repositories, infrastructure, operations);
        }
    }
}
