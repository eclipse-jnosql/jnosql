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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WishCollection {

    private final List<String> wishes = new ArrayList<>();

     public List<String> getWishes() {
        return Collections.unmodifiableList(wishes);
    }

    @Override
    public String toString() {
        return String.join(",", this.wishes);
    }

    public static WishCollection parse(String value) {
        var wishCollection = new WishCollection();
        wishCollection.wishes.addAll(List.of(value.split(",")));
        return wishCollection;
    }

     public void addWish(String wish) {
        this.wishes.add(wish);
    }
}
