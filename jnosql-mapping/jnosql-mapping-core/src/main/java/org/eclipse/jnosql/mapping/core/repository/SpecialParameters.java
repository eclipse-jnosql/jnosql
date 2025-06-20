/*
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
 */
package org.eclipse.jnosql.mapping.core.repository;


import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.page.PageRequest;
import jakarta.data.Sort;
import jakarta.data.restrict.Restriction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * The repository features has support for specific types like PageRequest and Sort,
 * to apply pagination and sorting to your queries dynamically.
 */
public final class SpecialParameters {
    static final SpecialParameters EMPTY = new SpecialParameters(null, null, Collections.emptyList(), null);

    private final PageRequest pageRequest;

    private final List<Sort<?>> sorts;
    private final Limit limit;

    private final Restriction<?> restriction;

    private SpecialParameters(PageRequest pageRequest, Limit limit, List<Sort<?>> sorts, Restriction<?> restriction) {
        this.pageRequest = pageRequest;
        this.sorts = sorts;
        this.limit = limit;
        this.restriction = restriction;
    }

    /**
     * Returns the PageRequest as optional.
     *
     * @return a {@link PageRequest} or {@link Optional#empty()} when there is not PageRequest instance
     */
    public Optional<PageRequest> pageRequest() {
        return Optional.ofNullable(pageRequest);
    }

    /**
     * Returns the sorts
     *
     * @return the sorts as list
     */
    public List<Sort<?>> sorts() {
        return sorts;
    }

    /**
     * Returns true when {@link SpecialParameters#pageRequest()} is empty and
     * {@link SpecialParameters#isSortEmpty()} is true
     *
     * @return when there is no sort and PageRequest
     */
    public boolean isEmpty() {
        return this.sorts.isEmpty() && pageRequest == null && limit == null && restriction == null;
    }

    /**
     * Return true when there is no sorts
     *
     * @return the sort
     */
    public boolean isSortEmpty() {
        return this.sorts.isEmpty();
    }

    /**
     * Returns true if it only has sort and {@link PageRequest} empty
     *
     * @return true if only have {@link PageRequest}
     */
    public boolean hasOnlySort() {
        return pageRequest == null && !sorts.isEmpty() && restriction == null && limit == null;
    }

    /**
     * Returns the Limit instance or {@link Optional#empty()}
     *
     * @return the Limit instance or {@link Optional#empty()}
     */
    public Optional<Limit> limit() {
        return Optional.ofNullable(limit);
    }

    /**
     * Returns the Restriction instance or {@link Optional#empty()}
     *
     * @return the Restriction instance or {@link Optional#empty()}
     */
    public Optional<Restriction<?>> restriction() {
        return Optional.ofNullable(restriction);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpecialParameters that = (SpecialParameters) o;
        return Objects.equals(pageRequest, that.pageRequest)
                && Objects.equals(sorts, that.sorts)
                && Objects.equals(limit, that.limit)
                && Objects.equals(restriction, that.restriction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pageRequest, sorts, limit, restriction);
    }

    @Override
    public String toString() {
        return "SpecialParameters{" +
                "pageRequest=" + pageRequest +
                ", sorts=" + sorts +
                ", limit=" + limit +
                ", restriction=" + restriction +
                '}';
    }

    static SpecialParameters of(Object[] parameters, Function<String, String> sortParser) {
        List<Sort<?>> sorts = new ArrayList<>();
        PageRequest pageRequest = null;
        Limit limit = null;
        Restriction<?> restriction = null;
        for (Object parameter : parameters) {
            if (parameter instanceof Sort<?> sort) {
                sorts.add(mapper(sort, sortParser));
            } else if (parameter instanceof Limit limitInstance) {
                limit = limitInstance;
            } else if (parameter instanceof Order<?> order) {
                order.sorts().stream().map(s -> mapper(s, sortParser)).forEach(sorts::add);
            } else if(parameter instanceof Sort<?>[] sortArray){
                Arrays.stream(sortArray).map(s -> mapper(s, sortParser)).forEach(sorts::add);
            } else if (parameter instanceof PageRequest request) {
               pageRequest = request;
            } else if (parameter instanceof Restriction<?> restrictionParameter) {
                restriction = restrictionParameter;
            }else {
                if (parameter instanceof Iterable<?> iterable) {
                    for (Object value : iterable) {
                        if (value instanceof Sort<?> sortValue) {
                            sorts.add(mapper(sortValue, sortParser));
                        }
                    }
                }
            }
        }
        return new SpecialParameters(pageRequest, limit, sorts, restriction);
    }

    /**
     * Returns true if the parameter is a special parameter
     *
     * @param parameter the parameter
     * @return true if the parameter is a special parameter
     */
    public static boolean isSpecialParameter(Object parameter) {
        return parameter instanceof Sort<?>
                || parameter instanceof Limit
                || parameter instanceof Order<?>
                || parameter instanceof PageRequest
                || parameter instanceof Sort<?>[];
    }

    /**
     * Returns true if the parameter is not a special parameter
     *
     * @param parameter the parameter
     * @return true if the parameter is not a special parameter
     */
    public static boolean isNotSpecialParameter(Object parameter) {
        return !isSpecialParameter(parameter);
    }

    /**
     * Returns true if the parameter is a special parameter
     *
     * @param parameter the parameter
     * @return true if the parameter is a special parameter
     */
    public static boolean isSpecialParameter(Class<?> parameter) {
        return Sort.class.isAssignableFrom(parameter)
                || Limit.class.isAssignableFrom(parameter)
                || Order.class.isAssignableFrom(parameter)
                || PageRequest.class.isAssignableFrom(parameter)
                || Restriction.class.isAssignableFrom(parameter)
                || parameter.isArray() && Sort.class.isAssignableFrom(parameter.getComponentType());
    }

    /**
     * Returns true if the parameter is not a special parameter
     *
     * @param parameter the parameter
     * @return true if the parameter is not a special parameter
     */
    public static boolean isNotSpecialParameter(Class<?> parameter) {
        return !isSpecialParameter(parameter);
    }

    /**
     * Returns the sort with the property updated from the sort parser
     * @param sort the sort
     * @param sortParser the sort parser
     * @return the special parameters
     */
    private static Sort<?> mapper(Sort<?> sort, Function<String, String> sortParser) {
        var property = sortParser.apply(sort.property());
        return sort.isAscending()? Sort.asc(property): Sort.desc(property);
    }
}
