/*
 *
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import org.eclipse.jnosql.communication.util.ServiceDiscovery;

/**
 * Decorators of all {@link ValueWriter} supported by Diana
 *
 * @param <T> current type
 * @param <S> the converted type
 * @see ValueWriter
 */
public final class ValueWriterDecorator<T, S> implements ValueWriter<T, S> {

    private static final ValueWriter INSTANCE = new ValueWriterDecorator<>();

    // Asked on every use rather than copied once, so that each application in an application server gets the
    // writers of its own database driver next to the built-in ones
    private final ServiceDiscovery<ValueWriter> writers = ServiceDiscovery.of(ValueWriter.class, ValueWriter.class);

    private ValueWriterDecorator() {
    }

    /**
     * Returns the shared value writer decorator instance.
     *
     * @return the value writer decorator
     */
    public static ValueWriter getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean test(Class<?> type) {
        return writers.all().stream().anyMatch(writerField -> writerField.test(type));
    }

    @Override
    public Object write(Object object) {
        Class<?> type = object.getClass();
        ValueWriter valueWriter = writers.all().stream().filter(r -> r.test(type)).findFirst().orElseThrow(
                () -> new UnsupportedOperationException("The type " + type + " is not supported yet"));
        return valueWriter.write(object);
    }

    @Override
    public String toString() {
        return "ValueWriterDecorator{" + "writers=" + writers.all() +
                '}';
    }

}
