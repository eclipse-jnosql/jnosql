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

import jakarta.data.page.CursoredPage;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import jakarta.data.restrict.Restriction;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.core.repository.operations.NoVendorSampleQueryProvider;
import org.eclipse.jnosql.mapping.core.repository.operations.SampleQueryProvider;
import org.eclipse.jnosql.mapping.metadata.repository.spi.CursorPaginationOperation;

import java.util.List;
import java.util.stream.Stream;

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

    @Delete
    void invalidDelete();

    @Delete
    ComicBook invalidDelete(ComicBook book);

    @Delete
    int deleteReturn(ComicBook book);

    @Delete
    void delete(ComicBook book);

    @Delete
    void delete(List<ComicBook> books);

    @Delete
    void delete(ComicBook[] books);

    @Save
    void invalidSave();

    @Save
    void saveVoid(ComicBook book);

    @Save
    ComicBook save(ComicBook book);

    @Save
    List<ComicBook> save(List<ComicBook> books);

    @Save
    ComicBook[] save(ComicBook[] books);


    List<ComicBook> findByName(String name);

    Stream<ComicBook> findAll();

    long countByName(String name);


    long countAll();

    boolean existsByName(String name);

    void deleteByName(String name);

    @Find
    List<ComicBook> find(String name);

    CursoredPage<ComicBook> cursor();

    default String defaultMethod() {
        return "defaultMethod";
    }

    @SampleQueryProvider("Sample of query using provider")
    String sampleProvider(String name);

    @NoVendorSampleQueryProvider("Sample of query using provider")
    String invalidProvider(String name);

    @Delete
    void delete(Restriction<ComicBook> restriction);
}
