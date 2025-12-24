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

import jakarta.data.Limit;
import jakarta.data.repository.Query;

import java.util.List;

public interface IdOperations {

    long countByIdBetween(long minimum, long maximum);

    boolean existsById(long id);

    @Query("SELECT id WHERE id >= :inclusiveMin ORDER BY id ASC")
    List<Long> withIdEqualOrAbove(long inclusiveMin, Limit limit);

}