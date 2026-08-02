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
package org.eclipse.jnosql.mapping.core.entities;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;

@Repository
public interface MagicPortionRepository extends BasicRepository<MagicPotion, Long> {

    @Insert
    MagicPotion insert(MagicPotion magicPotion);

    @Update
    MagicPotion update(MagicPotion magicPotion);

    @Delete
    void delete(MagicPotion magicPotion);

    @Save
    MagicPotion save(MagicPotion magicPotion);
}
