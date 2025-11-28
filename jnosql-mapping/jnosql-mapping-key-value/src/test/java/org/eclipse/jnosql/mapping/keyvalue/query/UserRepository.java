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
package org.eclipse.jnosql.mapping.keyvalue.query;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.keyvalue.entities.PersonStatisticRepository;
import org.eclipse.jnosql.mapping.keyvalue.entities.User;

import java.util.Optional;

@Repository
public interface UserRepository extends NoSQLRepository<User, String>, CrudRepository<User, String>, KeyValueRepositoryProxyTest.BaseQuery<User>, PersonStatisticRepository {

    Optional<User> findByName(String name);

    @Query("get \"12\"")
    Optional<User> findByQuery();


    @Query("get @id")
    Optional<User> querybyKey(@Param("id") String key);
    default Optional<User> otavio() {
        return querybyKey("otavio");
    }

    @Query("get @id")
    Optional<User> findByQuery(@Param("id") String id);

    @Insert
    User insertUser(User user);
    @Update
    User updateUser(User user);

    @Save
    User saveUser(User user);

    @Delete
    void deleteUser(User user);

    void existsByName(String name);

    User findByAge(Integer age);

    User find(String name);

    void deleteByAge(Integer age);

    int countByName(String name);
}