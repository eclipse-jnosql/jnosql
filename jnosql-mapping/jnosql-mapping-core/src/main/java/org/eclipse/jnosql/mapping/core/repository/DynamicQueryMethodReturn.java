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

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;

/**
 * This instance has the information to run the JNoSQL native query at {@link jakarta.data.repository.CrudRepository}
 */
public final class DynamicQueryMethodReturn<T> implements MethodDynamicExecutable {


    private final Method method;
    private final Object[] args;
    private final Class<?> typeClass;
    private final Function<String, PreparedStatement> prepareConverter;
    private final PageRequest pageRequest;

    private final Function<Object, T> queryMapper;

    private DynamicQueryMethodReturn(Method method, Object[] args, Class<?> typeClass,
                                     Function<String, PreparedStatement> prepareConverter,
                                     PageRequest pageRequest,
                                     Function<Object, T> queryMapper) {
        this.method = method;
        this.args = args;
        this.typeClass = typeClass;
        this.prepareConverter = prepareConverter;
        this.pageRequest = pageRequest;
        this.queryMapper = queryMapper;
    }

    Method method() {
        return method;
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

        private Method method;
        private Object[] args;
        private Class<?> typeClass;
        private Function<String, PreparedStatement> prepareConverter;
        private PageRequest pageRequest;
        @SuppressWarnings("unchecked")
        private Function<Object, T> queryMapper = (Function<Object, T>) Function.identity();

        private DynamicQueryMethodReturnBuilder() {
        }

        public DynamicQueryMethodReturnBuilder<T> method(Method method) {
            this.method = method;
            return this;
        }

        public DynamicQueryMethodReturnBuilder<T> args(Object[] args) {
            if(args != null) {
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

        public DynamicQueryMethodReturn<T> build() {
            Objects.requireNonNull(method, "method is required");
            Objects.requireNonNull(typeClass, "typeClass is required");
            Objects.requireNonNull(prepareConverter, "prepareConverter is required");
            return new DynamicQueryMethodReturn<>(method, args, typeClass, prepareConverter, pageRequest, queryMapper);
        }
    }


}