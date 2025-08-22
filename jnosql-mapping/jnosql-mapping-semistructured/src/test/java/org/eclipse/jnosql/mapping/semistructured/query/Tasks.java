/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import org.eclipse.jnosql.mapping.semistructured.entities.Task;

import java.util.List;

@Repository
public interface Tasks {

    @Query("from Task where active = true")
    List<Task> listActiveTasks();

    List<Task> findAll();

}
