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

import jakarta.data.exceptions.NonUniqueResultException;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * This instance has information to return at the dynamic query in Repository. To create an instance, use,
 * {@link DynamicReturn#builder()}
 *
 * @param <T> the source type
 */
public final class DynamicReturn<T> implements MethodDynamicExecutable {

    private final Class<T> classSource;

    private final Supplier<Optional<T>> singleResult;

    private final Supplier<Stream<T>> result;

    private final PageRequest pageRequest;

    private final Function<PageRequest, Optional<T>> singleResultPagination;

    private final Function<PageRequest, Stream<T>> streamPagination;

    private final Function<PageRequest, Page<T>> page;

    private final String methodName;

    private final Class<?> returnType;

    /**
     * A predicate to check it the object is instance of {@link PageRequest}
     */
    private static final Predicate<Object> IS_PAGINATION = PageRequest.class::isInstance;

    /**
     * A wrapper function that convert a result as a list to a result as optional
     *
     * @param methodName the method name
     * @return the function that does this conversion
     */
    public static Function<Supplier<Stream<?>>, Supplier<Optional<?>>> toSingleResult(final String methodName) {
        return new SupplierConverter(methodName);
    }

    /**
     * Finds {@link SpecialParameters} from array object
     *
     * @param params     the params
     * @param sortParser the sort parser
     * @return a {@link SpecialParameters} instance
     */
    public static SpecialParameters findSpecialParameters(Object[] params, Function<String, String> sortParser) {
        if (params == null || params.length == 0) {
            return SpecialParameters.EMPTY;
        }
        return SpecialParameters.of(params, sortParser);
    }

    /**
     * Finds {@link PageRequest} from array object
     *
     * @param params the params
     * @return a {@link PageRequest} or null
     */
    public static PageRequest findPageRequest(Object[] params) {
        if (params == null || params.length == 0) {
            return null;
        }
        return Stream.of(params)
                .filter(IS_PAGINATION)
                .map(PageRequest.class::cast)
                .findFirst().orElse(null);
    }

    public String methodName() {
        return methodName;
    }

    public Class<?> returnType() {
        return returnType;
    }

    @Override
    public Object execute() {
        return DynamicReturnConverter.INSTANCE.convert(this);
    }

    private record SupplierConverter(
            String methodName) implements Function<Supplier<Stream<?>>, Supplier<Optional<?>>> {

        @Override
        public Supplier<Optional<?>> apply(Supplier<Stream<?>> supplier) {
            return () -> {
                Stream<?> entities = supplier.get();
                final Iterator<?> iterator = entities.iterator();
                if (!iterator.hasNext()) {
                    return Optional.empty();
                }
                final Object entity = iterator.next();
                if (!iterator.hasNext()) {
                    return Optional.ofNullable(entity);
                }
                throw new NonUniqueResultException("No unique result to the method: " + methodName);
            };
        }
    }


    private DynamicReturn(Class<T> classSource,
                          Supplier<Optional<T>> singleResult,
                          Supplier<Stream<T>> result, PageRequest pageRequest,
                          Function<PageRequest, Optional<T>> singleResultPagination,
                          Function<PageRequest, Stream<T>> streamPagination,
                          Function<PageRequest, Page<T>> page,
                          String methodName,
                          Class<?> returnType) {
        this.classSource = classSource;
        this.singleResult = singleResult;
        this.result = result;
        this.pageRequest = pageRequest;
        this.singleResultPagination = singleResultPagination;
        this.streamPagination = streamPagination;
        this.page = page;
        this.methodName = methodName;
        this.returnType = returnType;
    }

    /**
     * The repository class type source.
     *
     * @return The repository class type source.
     */
    public Class<T> typeClass() {
        return classSource;
    }

    /**
     * Returns the result as single result
     *
     * @return the result as single result
     */
    public Optional<T> singleResult() {
        return singleResult.get();
    }

    /**
     * Returns the result as {@link List}
     *
     * @return the result as {@link List}
     */
    public Stream<T> result() {
        return result.get();
    }

    /**
     * @return the pagination
     */
    Optional<PageRequest> getPagination() {
        return Optional.ofNullable(pageRequest);
    }

    /**
     * @return returns a single result with pagination
     */
    public Optional<T> singleResultPagination() {
        return singleResultPagination.apply(pageRequest);
    }

    /**
     * @return a list result using pagination
     */
    public Stream<T> streamPagination() {
        return streamPagination.apply(pageRequest);
    }

    /**
     * @return the page
     */
    public Page<T> getPage() {
        return page.apply(pageRequest);
    }

    /**
     * @return check if there is pagination
     */
    public boolean hasPagination() {
        return pageRequest != null;
    }

    /**
     * Creates a builder to DynamicReturn
     *
     * @param <T> the type
     * @return a builder instance
     */
    public static <T> DefaultDynamicReturnBuilder<T> builder() {
        return new DefaultDynamicReturnBuilder<>();
    }

    /**
     * A builder of {@link DynamicReturn}
     *
     * @param <T> the type
     */
    @SuppressWarnings("rawtypes")
    public static final class DefaultDynamicReturnBuilder<T> {

        private Class<?> classSource;

        private Supplier<Optional<T>> singleResult;

        private Supplier<Stream<T>> result;

        private PageRequest pageRequest;

        private Function<PageRequest, Optional<T>> singleResultPagination;

        private Function<PageRequest, Stream<T>> streamPagination;

        private Function<PageRequest, Page<T>> page;

        private String methodName;

        private Class<?> returnType;

        private DefaultDynamicReturnBuilder() {
        }

        /**
         * @param classSource set the classSource
         * @return the instance
         */
        public DefaultDynamicReturnBuilder classSource(Class<?> classSource) {
            this.classSource = classSource;
            return this;
        }

        public DefaultDynamicReturnBuilder<T> methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public DefaultDynamicReturnBuilder<T> returnType(Class<?> returnType) {
            this.returnType = returnType;
            return this;
        }

        /**
         * @param singleResult the singleResult source
         * @return the builder instance
         */
        public DefaultDynamicReturnBuilder singleResult(Supplier<Optional<T>> singleResult) {
            this.singleResult = singleResult;
            return this;
        }

        /**
         * @param result the list
         * @return the builder instance
         */
        public DefaultDynamicReturnBuilder result(Supplier<Stream<T>> result) {
            this.result = result;
            return this;
        }

        /**
         * @param pageRequest the pagination
         * @return the builder instance
         */
        public DefaultDynamicReturnBuilder pagination(PageRequest pageRequest) {
            this.pageRequest = pageRequest;
            return this;
        }

        /**
         * @param singleResultPagination the single result pagination
         * @return the builder instance
         */
        public DefaultDynamicReturnBuilder singleResultPagination(Function<PageRequest, Optional<T>> singleResultPagination) {
            this.singleResultPagination = singleResultPagination;
            return this;
        }

        /**
         * @param listPagination the list pagination
         * @return the builder instance
         */
        public DefaultDynamicReturnBuilder streamPagination(Function<PageRequest, Stream<T>> listPagination) {
            this.streamPagination = listPagination;
            return this;
        }

        /**
         * @param page the page
         * @return the builder instance
         */
        public DefaultDynamicReturnBuilder page(Function<PageRequest, Page<T>> page) {
            this.page = page;
            return this;
        }

        /**
         * Creates a {@link DynamicReturn} from the parameters, all fields are required
         *
         * @return a new instance
         * @throws NullPointerException when there is null attributes
         */
        @SuppressWarnings({"rawtypes", "unchecked"})
        public DynamicReturn<T> build() {
            requireNonNull(classSource, "the class Source is required");
            requireNonNull(singleResult, "the single result supplier is required");
            requireNonNull(result, "the result supplier is required");
            requireNonNull(methodName, "the method name is required");
            requireNonNull(returnType, "the return type is required");

            if (pageRequest != null) {
                requireNonNull(singleResultPagination, "singleResultPagination is required when pagination is not null");
                requireNonNull(streamPagination, "listPagination is required when pagination is not null");
                requireNonNull(page, "page is required when pagination is not null");
            }

            return new DynamicReturn(classSource, singleResult, result,
                    pageRequest, singleResultPagination, streamPagination, page, methodName, returnType);
        }
    }

}
