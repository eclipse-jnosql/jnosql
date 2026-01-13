/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.reflection.repository;

import jakarta.data.page.CursoredPage;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

enum RepositoryMethodTypeConverter {
    INSTANCE;

    private static final MethodPattern FIND_BY =
            MethodPattern.of("find", RepositoryMethodType.FIND_BY);

    private static final MethodPattern DELETE_BY =
            MethodPattern.of("delete", RepositoryMethodType.DELETE_BY);

    private static final MethodPattern COUNT_ALL =
            MethodPattern.of("countAll", RepositoryMethodType.COUNT_ALL);

    private static final MethodPattern COUNT_BY =
            MethodPattern.of("count", RepositoryMethodType.COUNT_BY);

    private static final MethodPattern EXISTS_BY =
            MethodPattern.of("exists", RepositoryMethodType.EXISTS_BY);

    private static final List<MethodPattern> METHOD_PATTERNS =
            List.of(FIND_BY, DELETE_BY, COUNT_ALL, COUNT_BY, EXISTS_BY);

    private static final MethodOperation INSERT =
            MethodOperation.of(Insert.class, RepositoryMethodType.INSERT);

    private static final MethodOperation SAVE =
            MethodOperation.of(Save.class, RepositoryMethodType.SAVE);

    private static final MethodOperation DELETE =
            MethodOperation.of(Delete.class, RepositoryMethodType.DELETE);

    private static final MethodOperation UPDATE =
            MethodOperation.of(Update.class, RepositoryMethodType.UPDATE);

    private static final MethodOperation QUERY =
            MethodOperation.of(Query.class, RepositoryMethodType.QUERY);

    private static final MethodOperation FIND_QUERY =
            MethodOperation.of(Find.class, RepositoryMethodType.PARAMETER_BASED);

    private static final Set<MethodOperation> OPERATION_ANNOTATIONS =
            Set.of(INSERT, SAVE, DELETE, UPDATE, QUERY, FIND_QUERY);


    private static final String FIND_ALL = "findAll";


    public static RepositoryMethodType of(Method method) {
        Objects.requireNonNull(method, "method is required");

        if (method.isDefault()) {
            return RepositoryMethodType.DEFAULT_METHOD;
        }

        if (method.getReturnType().equals(CursoredPage.class)) {
            return RepositoryMethodType.CURSOR_PAGINATION;
        }

        Predicate<MethodOperation> hasAnnotation =
                op -> method.getAnnotation(op.annotation()) != null;

        Optional<RepositoryMethodType> annotationMatch = OPERATION_ANNOTATIONS.stream()
                .filter(hasAnnotation)
                .map(MethodOperation::type)
                .findFirst();

        if (annotationMatch.isPresent()) {
            return annotationMatch.get();
        }

        if (FIND_ALL.equals(method.getName())) {
            return RepositoryMethodType.FIND_ALL;
        }

        return METHOD_PATTERNS.stream()
                .filter(pattern -> method.getName().startsWith(pattern.keyword()))
                .findFirst()
                .map(MethodPattern::type)
                .orElse(RepositoryMethodType.UNKNOWN);
    }


    private record MethodPattern(String keyword, RepositoryMethodType type) {
        static MethodPattern of(String keyword, RepositoryMethodType type) {
            return new MethodPattern(keyword, type);
        }
    }

    private record MethodOperation(Class<? extends Annotation> annotation,
                                   RepositoryMethodType type) {
        static MethodOperation of(Class<? extends Annotation> annotation, RepositoryMethodType type) {
            return new MethodOperation(annotation, type);
        }
    }
}