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
 * It defines the operation that might be from the Method
 */
public enum RepositoryType {

    /**
     * Methods from either {@link CrudRepository}, {@link  BasicRepository} and {@link  org.eclipse.jnosql.mapping.NoSQLRepository}
     */
    DEFAULT,
    /**
     * General query method returning the repository type.It starts with "findBy" key word
     */
    FIND_BY,
    /**
     * Delete query method returning either no result (void) or the delete count. It starts with "deleteBy" keyword
     */
    DELETE_BY,
    /**
     * Method that has the "FindAll" keyword
     */
    FIND_ALL,
    /**
     * Count projection returning a numeric result. It starts and ends with "countAll" keyword
     */
    COUNT_ALL,
    /**
     * Count projection returning a numeric result. It starts with "countBy" keyword
     */
    COUNT_BY,
    /**
     * Exists projection, returning typically a boolean result. It starts with "existsBy" keyword
     */
    EXISTS_BY,

    /**
     * Methods from {@link Object}
     */
    OBJECT_METHOD,
    /**
     * The method that belongs to the interface using a default method.
     */
    DEFAULT_METHOD,
    /**
     * The method that belongs to the interface using a custom repository.
     */
    CUSTOM_REPOSITORY,
    /**
     * Method that has {@link jakarta.data.repository.Query} annotation
     */
    QUERY,
    /**
     * Method that has {@link jakarta.data.repository.Save} annotation
     */
    SAVE,
    /**
     * The last condition is parameter based. That will match the parameter in a simple query.
     * It will check by {@link jakarta.data.repository.Find} annotation
     */
    PARAMETER_BASED,
    /**
     * Method that has {@link jakarta.data.repository.Insert} annotation
     */
    INSERT,
    /**
     * Method that has {@link jakarta.data.repository.Delete} annotation
     */
    DELETE,
    /**
     * Method that has {@link jakarta.data.repository.Update} annotation
     */
    UPDATE,
}