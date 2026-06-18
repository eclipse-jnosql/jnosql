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
import jakarta.nosql.Convert;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.UUID;

@Entity
public class TravelWishList {

    @Id
    private UUID uuid;

    @Column
    @Convert(WishCollectionOverwriteConverter.class)
    private WishCollection wishCollection;


    public UUID getUuid() {
        return uuid;
    }

    public WishCollection getWishCollection() {
        return wishCollection;
    }


    public TravelWishList(@Id UUID uuid, @Column("wishCollection") @Convert(WishCollectionOverwriteConverter.class) WishCollection wishCollection) {
        this.uuid = uuid;
        this.wishCollection = wishCollection;
    }

    public static TravelWishList of(WishCollection wishCollection) {
        return new TravelWishList(UUID.randomUUID(), wishCollection);
    }
}
