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
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

enum RepositoryTypeConverter {
    INSTANCE;


    private static final MethodPattern FIND_BY =
            MethodPattern.of("find", RepositoryType.FIND_BY);

    private static final MethodPattern DELETE_BY =
            MethodPattern.of("deleteBy", RepositoryType.DELETE_BY);

    private static final MethodPattern COUNT_ALL =
            MethodPattern.of("countAll", RepositoryType.COUNT_ALL);

    private static final MethodPattern COUNT_BY =
            MethodPattern.of("countBy", RepositoryType.COUNT_BY);

    private static final MethodPattern EXISTS_BY =
            MethodPattern.of("existsBy", RepositoryType.EXISTS_BY);

    private static final Set<MethodPattern> METHOD_PATTERNS =
            Set.of(FIND_BY, DELETE_BY, COUNT_ALL, COUNT_BY, EXISTS_BY);

    private static final MethodOperation INSERT =
            MethodOperation.of(Insert.class, RepositoryType.INSERT);

    private static final MethodOperation SAVE =
            MethodOperation.of(Save.class, RepositoryType.SAVE);

    private static final MethodOperation DELETE =
            MethodOperation.of(Delete.class, RepositoryType.DELETE);

    private static final MethodOperation UPDATE =
            MethodOperation.of(Update.class, RepositoryType.UPDATE);

    private static final MethodOperation QUERY =
            MethodOperation.of(Query.class, RepositoryType.QUERY);

    private static final MethodOperation FIND_QUERY =
            MethodOperation.of(Find.class, RepositoryType.PARAMETER_BASED);

    private static final Set<MethodOperation> OPERATION_ANNOTATIONS =
            Set.of(INSERT, SAVE, DELETE, UPDATE, QUERY, FIND_QUERY);


    private static final String FIND_ALL = "findAll";


    public static RepositoryType of(Method method) {
        Objects.requireNonNull(method, "method is required");

        if (method.isDefault()) {
            return RepositoryType.DEFAULT_METHOD;
        }

        if (method.getReturnType().equals(CursoredPage.class)) {
            return RepositoryType.CURSOR_PAGINATION;
        }

        Predicate<MethodOperation> hasAnnotation =
                op -> method.getAnnotation(op.annotation()) != null;

        Optional<RepositoryType> annotationMatch = OPERATION_ANNOTATIONS.stream()
                .filter(hasAnnotation)
                .map(MethodOperation::type)
                .findFirst();

        if (annotationMatch.isPresent()) {
            return annotationMatch.get();
        }

        if (FIND_ALL.equals(method.getName())) {
            return RepositoryType.FIND_ALL;
        }

        return METHOD_PATTERNS.stream()
                .filter(pattern -> method.getName().startsWith(pattern.keyword()))
                .findFirst()
                .map(MethodPattern::type)
                .orElse(RepositoryType.UNKNOWN);
    }


    private record MethodPattern(String keyword, RepositoryType type) {
        static MethodPattern of(String keyword, RepositoryType type) {
            return new MethodPattern(keyword, type);
        }
    }

    private record MethodOperation(Class<? extends Annotation> annotation,
                                   RepositoryType type) {
        static MethodOperation of(Class<? extends Annotation> annotation, RepositoryType type) {
            return new MethodOperation(annotation, type);
        }
    }
}