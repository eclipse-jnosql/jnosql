/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.reflection.collection;

import org.eclipse.jnosql.mapping.metadata.CollectionSupplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SetSupplierTest {

    private CollectionSupplier<?> supplier;

    @BeforeEach
    void setUp() {
        this.supplier = new SetSupplier();
    }

    @Test
    void shouldTest() {
        Assertions.assertTrue(supplier.test(Set.class));
        Assertions.assertFalse(supplier.test(List.class));
    }

    @Test
    void shouldGet() {
        final Collection<?> collection = this.supplier.get();
        Assertions.assertNotNull(collection);
        Assertions.assertInstanceOf(HashSet.class, collection);
    }

}