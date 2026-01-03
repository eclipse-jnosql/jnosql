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
package org.eclipse.jnosql.communication.reader;


import org.eclipse.jnosql.communication.TypeReferenceReader;
import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.ValueReader;
import org.eclipse.jnosql.communication.ValueReaderDecorator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SequencedCollection;

import static java.util.stream.StreamSupport.stream;


/**
 * The {@link TypeReferenceReader} to {@link java.util.List} and {@link java.lang.Iterable}
 */
public class ListTypeReferenceReader implements TypeReferenceReader {

    private static final ValueReader SERVICE_PROVIDER = ValueReaderDecorator.getInstance();

    @Override
    public boolean test(TypeSupplier<?> typeReference) {
        Type type = typeReference.get();
        if (type instanceof ParameterizedType parameterizedType) {

            return (List.class.equals(parameterizedType.getRawType())
                    || Iterable.class.equals(parameterizedType.getRawType())
                    || SequencedCollection.class.equals(parameterizedType.getRawType())
                    || Collection.class.equals(parameterizedType.getRawType()))
                    && parameterizedType.getActualTypeArguments()[0] instanceof Class;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T convert(TypeSupplier<T> typeReference, Object value) {
        Type type = typeReference.get();
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Class<?> classType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        if (value instanceof Iterable<?> iterable) {
            return (T) stream(iterable.spliterator(), false).map(o -> SERVICE_PROVIDER.read(classType, o))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        }
        return (T) new ArrayList<>(Collections.singletonList(SERVICE_PROVIDER.read(classType, value)));
    }


}
