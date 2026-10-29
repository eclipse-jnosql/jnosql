/*
 *
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
 *   Mohan Lal
 *
 */
package org.eclipse.jnosql.communication;


import java.util.Optional;
import org.eclipse.jnosql.communication.util.ServiceDiscovery;

/**
 * Decorators of all {@link ValueReader} supported by Jakarta NoSQL
 *
 * @see ValueReader
 */
public final class ValueReaderDecorator implements ValueReader {

    private static final ValueReaderDecorator INSTANCE = new ValueReaderDecorator();

    // Asked on every use rather than copied once, so that each application in an application server gets the
    // readers of its own database driver next to the built-in ones
    private final ServiceDiscovery<ValueReader> readers = ServiceDiscovery.of(ValueReader.class, ValueReader.class);

    /**
     * Returns the shared value reader decorator instance.
     *
     * @return the value reader decorator
     */
    public static ValueReaderDecorator getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean test(Class type) {
        return readers.all().stream().anyMatch(r -> r.test(type));
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
        return readers.all().stream().filter(r -> r.test(type)).findFirst();
    }


    @Override
    public String toString() {
        return "ValueReaderDecorator{" + "readers=" + readers.all() +
                '}';
    }


}
