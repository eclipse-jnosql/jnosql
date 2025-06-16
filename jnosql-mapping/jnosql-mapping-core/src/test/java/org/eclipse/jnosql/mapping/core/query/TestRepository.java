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
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.core.query;

import jakarta.data.page.CursoredPage;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.restrict.Restriction;

import java.util.List;
import java.util.UUID;

public interface TestRepository {

    void customMethod();
    TestEntity findEntityById(UUID id);
    void deleteById();
    long countBy();
    boolean existsBy();
    List<TestEntity> findAll();

    @Query("")
    List<TestEntity> query(int age);

    CursoredPage<TestEntity> cursor();

    @Find
    List<TestEntity> find();

    List<TestEntity> restriction(Restriction<TestEntity> restriction);

    @Delete
    List<TestEntity> delete(Restriction<TestEntity> restriction);
}
