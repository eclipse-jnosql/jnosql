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
package org.eclipse.jnosql.mapping.core.entities;

import jakarta.data.Limit;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

import java.util.List;

@Repository
public interface People extends BasicRepository<Person, Long> {

    @Query("FROM Person")
    Page<Person> query();

    @Query("FROM Person")
    Page<Person> query0(Limit limit, PageRequest page);

    @Query("Person WHERE name = :native")
    Page<Person> query1(@Param("native") String name, PageRequest request);


    @Query("Person WHERE name = :arg0")
    Page<Person> query2(String name, PageRequest request);

    @Query("Person WHERE name = :name AND p.age = ?1")
    Page<Person> query3(@Param("name") String name, int age, PageRequest request);
}
