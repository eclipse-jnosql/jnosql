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

import jakarta.data.constraint.GreaterThan;
import jakarta.data.page.CursoredPage;
import jakarta.data.repository.By;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.First;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Is;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import org.eclipse.jnosql.mapping.reflection.entities.Person;

import java.util.List;

public interface PersonRepository extends CrudRepository<Person, Long> {

    List<Person> findByName(String name);

    @First(12)
    @OrderBy(value = "name", descending = true, ignoreCase = true)
    @OrderBy(value = "age", ignoreCase = true)
    List<Person> findByNameAndAge(String name, int age);
    @Query("From Person where name = :name")
    List<Person> query(@Param("name") String name);

    void deleteByName(String name);

    long countAll();

    long countByName(String name);

    boolean existsByName(String name);

    @Find
    List<Person> find(@By("name") String name);

    @Find
    List<Person> find2(@By("name") @Is(GreaterThan.class) String name);

    @Find
    CursoredPage<Person> cursor(@By("age") int age);

    @Save
    Person savePerson(Person person);

    @Insert
    Person insertPerson(Person person);

    @Update
    Person updatePerson(Person person);

    @Delete
    void deletePerson(Person person);

    @OrderBy("sample")
    @OrderBy("test")
    String unknownMethod();

    default List<Person> customMethod() {
        return List.of();
    }

}
