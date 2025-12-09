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
package org.eclipse.jnosql.mapping.core.repository;


import jakarta.data.page.PageRequest;
import org.eclipse.jnosql.mapping.PreparedStatement;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This instance has the information to run the JNoSQL native query at {@link jakarta.data.repository.CrudRepository}
 */
public final class DynamicQueryMethodReturn<T> implements MethodDynamicExecutable {
    private final Object[] args;
    private final Class<?> typeClass;
    private final Function<String, PreparedStatement> prepareConverter;
    private final PageRequest pageRequest;
    private final Function<Object, T> queryMapper;
    private final Supplier<String> querySupplier;
    private final Supplier<Map<String, Object>> paramsSupplier;
    private final Class<?> returnType;

    private final String methodName;

    private DynamicQueryMethodReturn(Object[] args, Class<?> typeClass,
                                     Function<String,
                                             PreparedStatement> prepareConverter,
                                     PageRequest pageRequest,
                                     Function<Object, T> queryMapper,
                                     Supplier<String> querySupplier,
                                     Supplier<Map<String, Object>> paramsSupplier,
                                     Class<?> returnType,
                                     String methodName) {
        this.querySupplier = querySupplier;
        this.args = args;
        this.typeClass = typeClass;
        this.prepareConverter = prepareConverter;
        this.pageRequest = pageRequest;
        this.queryMapper = queryMapper;
        this.paramsSupplier = paramsSupplier;
        this.returnType = returnType;
        this.methodName = methodName;
    }

    String querySupplier() {
        return querySupplier.get();
    }

    Map<String, Object> params() {
        return paramsSupplier.get();
    }

    Class<?> returnType() {
        return returnType;
    }

    Object[] args() {
        return args;
    }

    Class<?> typeClass() {
        return typeClass;
    }

    Function<String, PreparedStatement> prepareConverter() {
        return prepareConverter;
    }

    PageRequest pageRequest() {
        return pageRequest;
    }

    public String methodName() {
        return methodName;
    }

    Function<Object, T> queryMapper() {
        return queryMapper;
    }

    boolean hasPagination() {
        return pageRequest != null;
    }

    public static <T> DynamicQueryMethodReturnBuilder<T> builder() {
        return new DynamicQueryMethodReturnBuilder<>();
    }

    @Override
    public Object execute() {
        return DynamicReturnConverter.INSTANCE.convert(this);
    }



    public static final class DynamicQueryMethodReturnBuilder<T> {

        private Object[] args;
        private Class<?> typeClass;
        private Function<String, PreparedStatement> prepareConverter;
        private PageRequest pageRequest;

        private Supplier<String> querySupplier;

        private Supplier<Map<String, Object>> paramsSupplier;
        @SuppressWarnings("unchecked")
        private Function<Object, T> queryMapper = (Function<Object, T>) Function.identity();
        private Class<?> returnType;

        private String methodName;

        private DynamicQueryMethodReturnBuilder() {
        }

        public DynamicQueryMethodReturnBuilder<T> querySupplier(Supplier<String> querySupplier) {
            this.querySupplier = querySupplier;
            return this;
        }

        public DynamicQueryMethodReturnBuilder<T> paramsSupplier(Supplier<Map<String, Object>> paramsSupplier) {
            this.paramsSupplier = paramsSupplier;
            return this;
        }

        public DynamicQueryMethodReturnBuilder<T> args(Object[] args) {
            if (args != null) {
                this.args = args.clone();
            }
            return this;
        }

        public DynamicQueryMethodReturnBuilder<T> typeClass(Class<?> typeClass) {
            this.typeClass = typeClass;
            return this;
        }

        public DynamicQueryMethodReturnBuilder<T> prepareConverter(Function<String, PreparedStatement> prepareConverter) {
            this.prepareConverter = prepareConverter;
            return this;
        }

        public DynamicQueryMethodReturnBuilder<T> pageRequest(PageRequest pageRequest) {
            this.pageRequest = pageRequest;
            return this;
        }

        public DynamicQueryMethodReturnBuilder<T> mapper(Function<Object, T> queryMapper) {
            this.queryMapper = queryMapper;
            return this;
        }

        public DynamicQueryMethodReturnBuilder<T> returnType(Class<?> returnType) {
            this.returnType = returnType;
            return this;
        }

        public DynamicQueryMethodReturnBuilder<T> methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public DynamicQueryMethodReturn<T> build() {
            Objects.requireNonNull(typeClass, "typeClass is required");
            Objects.requireNonNull(prepareConverter, "prepareConverter is required");
            Objects.requireNonNull(querySupplier, "querySupplier is required");
            Objects.requireNonNull(paramsSupplier, "paramsSupplier is required");
            Objects.requireNonNull(queryMapper, "queryMapper is required");
            Objects.requireNonNull(returnType, "returnType is required");
            Objects.requireNonNull(methodName, "methodName is required");
            return new DynamicQueryMethodReturn<>(args,
                    typeClass,
                    prepareConverter,
                    pageRequest,
                    queryMapper,
                    querySupplier,
                    paramsSupplier,
                    returnType,
                    methodName);
        }
    }


}