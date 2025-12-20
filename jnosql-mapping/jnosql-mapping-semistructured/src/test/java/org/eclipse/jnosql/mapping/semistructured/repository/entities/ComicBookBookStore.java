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


import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Select;
import org.junit.jupiter.api.Order;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComicBookBookStore {

    List<ComicBook> findAll();

    List<ComicBook> findByName(String name);

    @Query("FROM ComicBook WHERE year > 2000")
    List<ComicBook> query();

    @Query("FROM ComicBook WHERE year > ?1")
    List<ComicBook> query(int year);

    @Select("name")
    @OrderBy("name")
    @OrderBy(value = "year", descending = true)
    List<String> findByNameAndYear(String name, int year);

    Optional<ComicBook> findById(String id);

    Page<ComicBook> findByName(String name, PageRequest pageRequest);

    long countAll();

    long countByName(String name);

    boolean existsByName(String name);

    void deleteByName(String name);

    int deleteByYear(int year);
}
