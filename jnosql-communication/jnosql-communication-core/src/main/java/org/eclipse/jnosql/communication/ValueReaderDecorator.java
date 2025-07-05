/*
 *
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
 *
 */
package org.eclipse.jnosql.communication;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decorators of all {@link ValueReader} supported by Jakarta NoSQL
 *
 * @see ValueReader
 */
public final class ValueReaderDecorator implements ValueReader {

    private static final ValueReaderDecorator INSTANCE = new ValueReaderDecorator();
    private final Map<Class<?>, ValueReader> cache = new ConcurrentHashMap<>();

    private final List<ValueReader> readers = new ArrayList<>();

    {
        readers.addAll(ServiceProviderLoader.loadAll(ValueReader.class));
    }

    public static ValueReaderDecorator getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean test(Class type) {
        return readers.stream().anyMatch(r -> r.test(type));
    }

    @Override
    public <T> T read(Class<T> type, Object value) {
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        ValueReader valueReader = getReader(type)
                .orElseThrow(() -> new UnsupportedOperationException("The type " + type + " is not supported yet"));

        return valueReader.read(type, value);
    }

    private Optional<ValueReader> getReader(Class<?> type) {
        return Optional.ofNullable(cache.computeIfAbsent(type, t ->
                readers.stream().filter(r -> r.test(t)).findFirst().orElse(null)
        ));
    }


    @Override
    public String toString() {
        return "ValueReaderDecorator{" + "readers=" + readers +
                '}';
    }


}
