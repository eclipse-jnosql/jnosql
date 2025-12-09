/*
 *  Copyright (c) 2022,2025 Contributors to the Eclipse Foundation
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
import org.eclipse.jnosql.mapping.core.NoSQLPage;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * The converter within the return method at Repository class.
 */
public enum DynamicReturnConverter {

    INSTANCE;

    private final RepositoryReturn defaultReturn = new DefaultRepositoryReturn();

    private final List<RepositoryReturn> repositoryReturns = ServiceLoader.load(RepositoryReturn.class) .stream()
            .map(ServiceLoader.Provider::get)
            .toList();

    /**
     * Converts the entity from the Method return type.
     *
     * @param dynamic the information about the method and return source
     * @return the conversion result
     * @throws NullPointerException when the dynamic is null
     */
    public Object convert(DynamicReturn<?> dynamic) {

        Class<?> typeClass = dynamic.typeClass();
        Class<?> returnType = dynamic.returnType();

        RepositoryReturn repositoryReturn = repositoryReturns
                .stream()
                .filter(Objects::nonNull)
                .filter(r -> r.isCompatible(typeClass, returnType))
                .findFirst().orElse(defaultReturn);

        if (dynamic.hasPagination()) {
            return repositoryReturn.convertPageRequest(dynamic);
        } else {
            return repositoryReturn.convert(dynamic);
        }
    }

    /**
     * Reads and execute JNoSQL query from the Method that has the {@link jakarta.data.repository.Query} annotation
     *
     * @return the result from the query annotation
     */
    @SuppressWarnings({"unchecked"})
    public Object convert(DynamicQueryMethodReturn<?> dynamicQueryMethod) {
        Function<String, PreparedStatement> prepareConverter = dynamicQueryMethod.prepareConverter();
        Class<?> typeClass = dynamicQueryMethod.typeClass();

        String queryString = dynamicQueryMethod.querySupplier();

        Map<String, Object> params = dynamicQueryMethod.params();
        boolean namedParameters = queryContainsNamedParameters(queryString);
        PreparedStatement prepare = prepareConverter.apply(queryString);
                    params.entrySet().stream()
                        .filter(namedParameters ?
                                        (parameter -> !isOrdinalParameter(parameter))
                                        : parameter -> isOrdinalParameter(parameter))
                        .forEach(param -> prepare.bind(param.getKey(), param.getValue()));

        if (prepare.isCount()) {
            return prepare.count();
        }

        var pageRequest = dynamicQueryMethod.pageRequest();

        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .classSource(typeClass)
                .methodName(dynamicQueryMethod.querySupplier())
                .returnType(dynamicQueryMethod.returnType())
                .result(() -> prepare.result().map(dynamicQueryMethod.queryMapper()))
                .singleResult(() -> prepare.singleResult().map(dynamicQueryMethod.queryMapper()))
                .pagination(pageRequest)
                .streamPagination(p -> prepare.result().map(dynamicQueryMethod.queryMapper()))
                .singleResultPagination(p -> prepare.singleResult().map(dynamicQueryMethod.queryMapper()))
                .page(p -> {
                    Stream<?> entities = prepare.result().map(dynamicQueryMethod.queryMapper());
                    return NoSQLPage.of(entities.toList(), (PageRequest) p);
                }).build();

        return convert(dynamicReturn);
    }

    public static boolean queryContainsNamedParameters(final String query) {

        if (query == null || query.isEmpty()) {
            return false;
        }

        final int length = query.length();
        for (int index = 0; index < length; index++) {
            final char current = query.charAt(index);

            if (current == '?') {
                final int nextIndex = index + 1;
                if (nextIndex < length && Character.isDigit(query.charAt(nextIndex))) {
                    return false;
                }
            } else if (current == ':') {
                final int nextIndex = index + 1;
                if (nextIndex < length && isIdentifierStart(query.charAt(nextIndex))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isIdentifierStart(final char ch) {
        return Character.isLetter(ch) || ch == '_' || ch == '$';
    }

    private static boolean isOrdinalParameter(Map.Entry<String, Object> parameter) {
        return parameter.getKey().startsWith("?");
    }

}
