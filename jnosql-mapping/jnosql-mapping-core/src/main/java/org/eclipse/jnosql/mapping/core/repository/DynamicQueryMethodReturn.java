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
import java.util.function.LongSupplier;
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
    private final LongSupplier totalSupplier;

    private DynamicQueryMethodReturn(Object[] args, Class<?> typeClass,
                                     Function<String,
                                             PreparedStatement> prepareConverter,
                                     PageRequest pageRequest,
                                     Function<Object, T> queryMapper,
                                     Supplier<String> querySupplier,
                                     Supplier<Map<String, Object>> paramsSupplier,
                                     Class<?> returnType,
                                     String methodName,
                                     LongSupplier totalSupplier) {
        this.querySupplier = querySupplier;
        this.args = args;
        this.typeClass = typeClass;
        this.prepareConverter = prepareConverter;
        this.pageRequest = pageRequest;
        this.queryMapper = queryMapper;
        this.paramsSupplier = paramsSupplier;
        this.returnType = returnType;
        this.methodName = methodName;
        this.totalSupplier = totalSupplier;
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

    /**
     * Returns the repository method name.
     *
     * @return the method name
     */
    public String methodName() {
        return methodName;
    }

    Function<Object, T> queryMapper() {
        return queryMapper;
    }

    LongSupplier totalSupplier() {
        return totalSupplier;
    }

    boolean hasPagination() {
        return pageRequest != null;
    }

    /**
     * Creates a dynamic query method return builder.
     *
     * @param <T> the mapped result type
     * @return the builder
     */
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
        private LongSupplier totalSupplier;

        private DynamicQueryMethodReturnBuilder() {
        }

        /**
         * Sets the query supplier.
         *
         * @param querySupplier the query supplier
         * @return this builder
         */
        public DynamicQueryMethodReturnBuilder<T> querySupplier(Supplier<String> querySupplier) {
            this.querySupplier = querySupplier;
            return this;
        }

        /**
         * Sets the named parameter supplier.
         *
         * @param paramsSupplier the parameter supplier
         * @return this builder
         */
        public DynamicQueryMethodReturnBuilder<T> paramsSupplier(Supplier<Map<String, Object>> paramsSupplier) {
            this.paramsSupplier = paramsSupplier;
            return this;
        }

        /**
         * Sets invocation arguments.
         *
         * @param args the invocation arguments
         * @return this builder
         */
        public DynamicQueryMethodReturnBuilder<T> args(Object[] args) {
            if (args != null) {
                this.args = args.clone();
            }
            return this;
        }

        /**
         * Sets the entity type.
         *
         * @param typeClass the entity type
         * @return this builder
         */
        public DynamicQueryMethodReturnBuilder<T> typeClass(Class<?> typeClass) {
            this.typeClass = typeClass;
            return this;
        }

        /**
         * Sets the prepared statement converter.
         *
         * @param prepareConverter the prepared statement converter
         * @return this builder
         */
        public DynamicQueryMethodReturnBuilder<T> prepareConverter(Function<String, PreparedStatement> prepareConverter) {
            this.prepareConverter = prepareConverter;
            return this;
        }

        /**
         * Sets the page request.
         *
         * @param pageRequest the page request
         * @return this builder
         */
        public DynamicQueryMethodReturnBuilder<T> pageRequest(PageRequest pageRequest) {
            this.pageRequest = pageRequest;
            return this;
        }

        /**
         * Sets the result mapper.
         *
         * @param queryMapper the result mapper
         * @return this builder
         */
        public DynamicQueryMethodReturnBuilder<T> mapper(Function<Object, T> queryMapper) {
            this.queryMapper = queryMapper;
            return this;
        }

        /**
         * Sets the repository method return type.
         *
         * @param returnType the return type
         * @return this builder
         */
        public DynamicQueryMethodReturnBuilder<T> returnType(Class<?> returnType) {
            this.returnType = returnType;
            return this;
        }

        /**
         * Sets the repository method name.
         *
         * @param methodName the method name
         * @return this builder
         */
        public DynamicQueryMethodReturnBuilder<T> methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        /**
         * Sets the total result supplier.
         *
         * @param totalSupplier the total result supplier
         * @return this builder
         */
        public DynamicQueryMethodReturnBuilder<T> totalSupplier(LongSupplier totalSupplier) {
            this.totalSupplier = totalSupplier;
            return this;
        }

        /**
         * Builds the dynamic query method return.
         *
         * @return the dynamic query method return
         */
        public DynamicQueryMethodReturn<T> build() {
            Objects.requireNonNull(typeClass, "typeClass is required");
            Objects.requireNonNull(prepareConverter, "prepareConverter is required");
            Objects.requireNonNull(querySupplier, "querySupplier is required");
            Objects.requireNonNull(paramsSupplier, "paramsSupplier is required");
            Objects.requireNonNull(queryMapper, "queryMapper is required");
            Objects.requireNonNull(returnType, "returnType is required");
            Objects.requireNonNull(methodName, "methodName is required");
            Objects.requireNonNull(totalSupplier, "totalSupplier is required");
            return new DynamicQueryMethodReturn<>(args,
                    typeClass,
                    prepareConverter,
                    pageRequest,
                    queryMapper,
                    querySupplier,
                    paramsSupplier,
                    returnType,
                    methodName,
                    totalSupplier);
        }
    }


}