/*
 *
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
 *
 */

package org.eclipse.jnosql.communication;


import java.util.ArrayList;
import java.util.List;

/**
 * Decorators of all {@link ValueWriter} supported by Diana
 *
 * @param <T> current type
 * @param <S> the converted type
 * @see ValueWriter
 */
public final class ValueWriterDecorator<T, S> implements ValueWriter<T, S> {

    private static final ValueWriter INSTANCE = new ValueWriterDecorator<>();

    private final List<ValueWriter> writers = new ArrayList<>();

    {
        ServiceProviderLoader.loadAll(ValueWriter.class).forEach(writers::add);
    }

    private ValueWriterDecorator() {
    }

    public static ValueWriter getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean test(Class<?> type) {
        return writers.stream().anyMatch(writerField -> writerField.test(type));
    }

    @Override
    public Object write(Object object) {
        Class<?> type = object.getClass();
        ValueWriter valueWriter = writers.stream().filter(r -> r.test(type)).findFirst().orElseThrow(
                () -> new UnsupportedOperationException("The type " + type + " is not supported yet"));
        return valueWriter.write(object);
    }

    @Override
    public String toString() {
        return "ValueWriterDecorator{" + "writers=" + writers +
                '}';
    }

}
