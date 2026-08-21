/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *
 *   The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *
 *   Contributors:
 *
 *   Mohan Lal
 *
 */
package org.eclipse.jnosql.mapping.core.repository.returns;

import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturnConverter;
import org.eclipse.jnosql.mapping.core.repository.RepositoryReturn;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * A repository return strategy that supports repository methods returning
 * {@link CompletionStage}.
 *
 * <p>The generic type declared by the {@link CompletionStage} determines
 * the repository return strategy used to convert the underlying result.
 * For example, {@code CompletionStage<Person>} uses the repository return
 * strategy for {@code Person}, while {@code CompletionStage<List<Person>>}
 * uses the repository return strategy for {@code List}.</p>
 *
 * <p>The converted result is wrapped in a completed
 * {@link CompletableFuture}.</p>
 */
public class CompletionStageRepositoryReturn implements RepositoryReturn {

    @Override
    public boolean isCompatible(Class<?> entity, Class<?> returnType) {
        return CompletionStage.class.equals(returnType);
    }

    @Override
    public <T> Object convert(DynamicReturn<T> dynamicReturn) {
        return CompletableFuture.completedFuture(convertResult(dynamicReturn));
    }

    @Override
    public <T> Object convertPageRequest(DynamicReturn<T> dynamicReturn) {
        return CompletableFuture.completedFuture(
                convertPageResult(dynamicReturn));
    }

    private <T> Object convertResult(DynamicReturn<T> dynamicReturn) {
        Class<?> returnType = getReturnType(dynamicReturn);

        return getRepositoryReturn(dynamicReturn, returnType)
                .convert(dynamicReturn);
    }

    private <T> Object convertPageResult(DynamicReturn<T> dynamicReturn) {
        Class<?> returnType = getReturnType(dynamicReturn);

        return getRepositoryReturn(dynamicReturn, returnType)
                .convertPageRequest(dynamicReturn);
    }

    private <T> RepositoryReturn getRepositoryReturn(
            DynamicReturn<T> dynamicReturn, Class<?> returnType) {

        return DynamicReturnConverter.INSTANCE
                .findRepositoryReturn(dynamicReturn.typeClass(), returnType);
    }

    private Class<?> getReturnType(DynamicReturn<?> dynamicReturn) {
        Type genericReturnType =
                dynamicReturn.getMethod().getGenericReturnType();

        if (genericReturnType instanceof ParameterizedType parameterizedType) {
            Type type = parameterizedType.getActualTypeArguments()[0];

            if (type instanceof Class<?> typeClass) {
                return typeClass;
            }

            if (type instanceof ParameterizedType nestedParameterizedType
                    && nestedParameterizedType.getRawType()
                    instanceof Class<?> rawType) {
                return rawType;
            }
        }

        throw new IllegalArgumentException(
                "Cannot determine CompletionStage generic return type for method: "
                        + dynamicReturn.getMethod());
    }
}
