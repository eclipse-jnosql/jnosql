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
package org.eclipse.jnosql.mapping.semistructured.entities.autoconverter;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.UUID;

@Entity
public class NewYearWishList {

    @Id
    private UUID uuid;

    @Column
    private WishCollection wishCollection;


    public UUID getUuid() {
        return uuid;
    }

    public WishCollection getWishCollection() {
        return wishCollection;
    }


    public NewYearWishList(@Id UUID uuid, @Column("wishCollection") WishCollection wishCollection) {
        this.uuid = uuid;
        this.wishCollection = wishCollection;
    }

    public static NewYearWishList of(WishCollection wishCollection) {
        return new NewYearWishList(UUID.randomUUID(), wishCollection);
    }
}
