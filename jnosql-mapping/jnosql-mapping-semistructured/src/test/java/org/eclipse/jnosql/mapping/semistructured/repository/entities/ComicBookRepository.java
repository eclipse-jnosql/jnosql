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
package org.eclipse.jnosql.mapping.semistructured.repository.entities;


import jakarta.data.page.CursoredPage;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.By;
import jakarta.data.repository.Find;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import org.eclipse.jnosql.mapping.NoSQLRepository;

@Repository
public interface ComicBookRepository extends NoSQLRepository<ComicBook, String> {


    long countAll();

    long countByName(String name);

    boolean existsByName(String name);

    void deleteByName(String name);

    int deleteByYear(int year);


    CursoredPage<ComicBook> findByName(String name, PageRequest page);

    @Find
    CursoredPage<ComicBook> findByNameUsingFind(@By("name") String name, PageRequest page);

    @Query("FROM ComicBook WHERE name = :name")
    CursoredPage<ComicBook> query(@Param("name") String name, PageRequest page);

    CursoredPage<ComicBook> invalidCursor(String name);
}
