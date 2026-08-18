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
import java.util.function.LongSupplier;

/**
 * This instance has the information to run the JNoSQL native query at {@link jakarta.data.repository.CrudRepository}
 */
public final class DynamicQueryMethodReturn<T> implements MethodDynamicExecutable {

    private static final LongSupplier UNSUPPORTED_TOTAL = () -> {
        throw new UnsupportedOperationException("The database provider does not support page totals");
    };


    private final Method method;
    private final Object[] args;
    private final Class<?> typeClass;
    private final Function<String, PreparedStatement> prepareConverter;
    private final PageRequest pageRequest;
    private final LongSupplier totalSupplier;

    private DynamicQueryMethodReturn(Method method, Object[] args, Class<?> typeClass,
                                     Function<String, PreparedStatement> prepareConverter,
                                     PageRequest pageRequest, LongSupplier totalSupplier) {
        this.method = method;
        this.args = args;
        this.typeClass = typeClass;
        this.prepareConverter = prepareConverter;
        this.pageRequest = pageRequest;
        this.totalSupplier = totalSupplier;
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

    LongSupplier totalSupplier() {
        return totalSupplier;
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
        private LongSupplier totalSupplier = UNSUPPORTED_TOTAL;

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

        public DynamicQueryMethodReturnBuilder<T> totalSupplier(LongSupplier totalSupplier) {
            this.totalSupplier = totalSupplier;
            return this;
        }

        public DynamicQueryMethodReturn<T> build() {
            Objects.requireNonNull(method, "method is required");
            Objects.requireNonNull(typeClass, "typeClass is required");
            Objects.requireNonNull(prepareConverter, "prepareConverter is required");
            Objects.requireNonNull(totalSupplier, "totalSupplier is required");
            return new DynamicQueryMethodReturn<>(method, args, typeClass, prepareConverter, pageRequest, totalSupplier);
        }
    }


}
