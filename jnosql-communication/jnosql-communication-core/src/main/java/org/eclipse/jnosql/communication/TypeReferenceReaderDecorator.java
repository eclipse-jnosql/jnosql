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
import java.util.ServiceLoader;

/**
 * Decorators of all {@link TypeReferenceReader}
 *
 * @see ValueReader
 */
public final class TypeReferenceReaderDecorator implements TypeReferenceReader {

    private static final TypeReferenceReaderDecorator INSTANCE = new TypeReferenceReaderDecorator();

    private final List<TypeReferenceReader> readers = new ArrayList<>();

    {
        readers.addAll(ServiceProviderLoader.loadAll(TypeReferenceReader.class));
    }

    public static TypeReferenceReaderDecorator getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean test(TypeSupplier type) {
        return readers.stream().anyMatch(r -> r.test(type));
    }

    @Override
    public <T> T convert(TypeSupplier<T> typeReference, Object value) {

        TypeReferenceReader valueReader = readers.stream().filter(r -> r.test(typeReference)).findFirst().
                orElseThrow(() -> new UnsupportedOperationException("The type " + typeReference + " is not supported yet"));
        return valueReader.convert(typeReference, value);
    }

    @Override
    public String toString() {
        return "TypeReferenceReaderDecorator{" + "readers=" + readers +
                '}';
    }


}
