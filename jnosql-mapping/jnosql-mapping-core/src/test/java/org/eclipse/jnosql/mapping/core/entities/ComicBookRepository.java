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

import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;
import org.eclipse.jnosql.mapping.NoSQLRepository;

import java.util.List;

@Repository
public interface ComicBookRepository extends NoSQLRepository<ComicBook, String>, BookComponent {

    @Insert
    void invalidInsert();

    @Insert
    void insertVoid(ComicBook book);

    @Insert
    ComicBook insert(ComicBook book);

    @Insert
    List<ComicBook> insert(List<ComicBook> books);

    @Insert
    ComicBook[] insert(ComicBook[] books);


    @Update
    void invalidUpdate();

    @Update
    void updateVoid(ComicBook book);

    @Update
    ComicBook update(ComicBook book);

    @Update
    List<ComicBook> update(List<ComicBook> books);

    @Update
    ComicBook[] update(ComicBook[] books);
}
