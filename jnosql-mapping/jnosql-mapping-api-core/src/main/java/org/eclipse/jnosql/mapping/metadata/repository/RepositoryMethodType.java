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
package org.eclipse.jnosql.mapping.metadata.repository;


import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;

/**
 * Semantic classification of a repository method.
 * Each constant identifies how a Jakarta Data repository method should be
 * interpreted and executed, based on naming conventions, annotations, or
 * inheritance from built-in repository interfaces.
 *
 */
public enum RepositoryMethodType {

    /**
     * Derived query method that starts with the keyword {@code findBy}.
     * Used to perform selection based on entity attributes and typically
     * returns an entity, an {@code Optional}, or a {@code List}.
     */
    FIND_BY,

    /**
     * Derived delete method that starts with the keyword {@code deleteBy}.
     * Executes a deletion query and may return the number of deleted records
     * or no result (void).
     */
    DELETE_BY,

    /**
     * Derived query method that contains the keyword {@code findAll}.
     * Used to retrieve all entities managed by the repository, optionally
     * with sorting or pagination.
     */
    FIND_ALL,

    /**
     * Count projection method that matches the pattern {@code countAll}.
     * Returns a numeric value representing the total number of entities
     * handled by the repository.
     */
    COUNT_ALL,

    /**
     * Count projection method that starts with the keyword {@code countBy}.
     * Returns a numeric value representing the number of entities that match
     * the specified criteria.
     */
    COUNT_BY,

    /**
     * Existence-check method that starts with the keyword {@code existsBy}.
     * Typically returns a boolean value indicating whether any entity matches
     * the given condition.
     */
    EXISTS_BY,

    /**
     * Method defined as a {@code default} method within the repository interface.
     * Executed directly by user code rather than by the Jakarta Data provider.
     */
    DEFAULT_METHOD,

    /**
     * Method annotated with
     * {@link jakarta.data.repository.Query jakarta.data.repository.Query}.
     * Represents a custom query explicitly defined by the developer rather
     * than derived from naming conventions.
     */
    QUERY,

    /**
     * Method annotated with
     * {@link jakarta.data.repository.Save jakarta.data.repository.Save}.
     * Represents a save or upsert operation that either inserts or updates
     * an entity depending on its state.
     */
    SAVE,

    /**
     * Method annotated with
     * {@link jakarta.data.repository.Find jakarta.data.repository.Find}.
     * The query is resolved dynamically from the parameters rather than a
     * fixed name pattern or explicit query string.
     */
    PARAMETER_BASED,

    /**
     * Method annotated with
     * {@link jakarta.data.repository.Insert jakarta.data.repository.Insert}.
     * Represents an explicit insert operation that adds a new entity.
     */
    INSERT,

    /**
     * Method annotated with
     * {@link jakarta.data.repository.Delete jakarta.data.repository.Delete}.
     * Represents an explicit delete operation that removes one or more
     * entities matching the specified conditions.
     */
    DELETE,

    /**
     * Method annotated with
     * {@link jakarta.data.repository.Update jakarta.data.repository.Update}.
     * Represents an explicit update operation that modifies existing entities.
     */
    UPDATE,
    /**
     * Method that returns a paginated result set.
     */
    CURSOR_PAGINATION,
    /**
     * At the stage it is undefined, thus, required validation
     */
    UNKNOWN,
    /**
     * Methods from either {@link CrudRepository}, {@link  BasicRepository} and {@link  org.eclipse.jnosql.mapping.NoSQLRepository}
     */
    DEFAULT,
    /**
     * Methods from {@link Object}
     */
    OBJECT_METHOD,
    /**
     * The method that belongs to the interface using a custom repository.
     */
    CUSTOM_REPOSITORY

}