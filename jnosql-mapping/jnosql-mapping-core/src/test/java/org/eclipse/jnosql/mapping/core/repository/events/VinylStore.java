/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.repository.events;

import jakarta.data.repository.Delete;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;

import java.util.List;

@Repository
interface VinylStore {

    @Insert
    void insert(VinylRecord vinylRecord);
    @Insert
    void insert(List<VinylRecord> vinylRecords);
    @Insert
    void insert(VinylRecord[] vinylRecords);

    @Update
    void update(VinylRecord vinylRecord);
    @Update
    void update(List<VinylRecord> vinylRecords);
    @Update
    void update(VinylRecord[] vinylRecords);


    @Save
    void save(VinylRecord vinylRecord);
    @Save
    void save(List<VinylRecord> vinylRecords);
    @Save
    void save(VinylRecord[] vinylRecords);

    @Delete
    void delete(VinylRecord vinylRecord);
    @Delete
    void delete(List<VinylRecord> vinylRecords);
    @Delete
    void delete(VinylRecord[] vinylRecords);

}
