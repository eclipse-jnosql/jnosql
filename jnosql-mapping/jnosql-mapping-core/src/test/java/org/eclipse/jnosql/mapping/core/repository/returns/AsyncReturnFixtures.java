/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.core.repository.returns;

import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Builds asynchronous {@link DynamicReturn} instances for the asynchronous return handler tests.
 */
final class AsyncReturnFixtures {

    record Product(String name) {
    }

    private AsyncReturnFixtures() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static DynamicReturn<Product> asynchronous(Class<?> wrapper, Class<?> nested,
                                               Supplier<Optional<Product>> single,
                                               Supplier<Stream<Product>> result) {
        return (DynamicReturn<Product>) DynamicReturn.builder()
                .classSource(Product.class)
                .methodName("find")
                .returnType(wrapper)
                .asyncResultType(nested)
                .singleResult((Supplier) single)
                .result((Supplier) result)
                .build();
    }
}
