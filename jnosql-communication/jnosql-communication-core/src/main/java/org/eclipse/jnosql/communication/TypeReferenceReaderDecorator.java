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


import org.eclipse.jnosql.communication.util.ServiceDiscovery;

/**
 * Decorators of all {@link TypeReferenceReader}
 *
 * @see ValueReader
 */
public final class TypeReferenceReaderDecorator implements TypeReferenceReader {

    private static final TypeReferenceReaderDecorator INSTANCE = new TypeReferenceReaderDecorator();

    // Asked on every use rather than copied once, so that each application in an application server gets the
    // readers of its own database driver next to the built-in ones
    private final ServiceDiscovery<TypeReferenceReader> readers =
            ServiceDiscovery.of(TypeReferenceReader.class, TypeReferenceReader.class);

    /**
     * Returns the shared type reference reader decorator instance.
     *
     * @return the type reference reader decorator
     */
    public static TypeReferenceReaderDecorator getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean test(TypeSupplier type) {
        return readers.all().stream().anyMatch(r -> r.test(type));
    }

    @Override
    public <T> T convert(TypeSupplier<T> typeReference, Object value) {

        TypeReferenceReader valueReader = readers.all().stream().filter(r -> r.test(typeReference)).findFirst().
                orElseThrow(() -> new UnsupportedOperationException("The type " + typeReference + " is not supported yet"));
        return valueReader.convert(typeReference, value);
    }

    @Override
    public String toString() {
        return "TypeReferenceReaderDecorator{" + "readers=" + readers.all() +
                '}';
    }


}
