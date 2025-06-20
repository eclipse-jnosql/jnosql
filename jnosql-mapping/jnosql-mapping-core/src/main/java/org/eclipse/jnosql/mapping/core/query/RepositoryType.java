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
package org.eclipse.jnosql.mapping.core.query;

import jakarta.data.page.CursoredPage;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Query;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import jakarta.enterprise.inject.spi.CDI;
import org.eclipse.jnosql.mapping.NoSQLRepository;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * It defines the operation that might be from the Method
 */
public enum RepositoryType {

    /**
     * Methods from either {@link CrudRepository}, {@link  BasicRepository} and {@link  org.eclipse.jnosql.mapping.NoSQLRepository}
     */
    DEFAULT(""),
    /**
     * General query method returning the repository type.It starts with "findBy" key word
     */
    FIND_BY("find"),
    /**
     * Delete query method returning either no result (void) or the delete count. It starts with "deleteBy" keyword
     */
    DELETE_BY("deleteBy"),
    /**
     * Method that has the "FindAll" keyword
     */
    FIND_ALL("findAll"),
    /**
     * Count projection returning a numeric result. It starts and ends with "countAll" keyword
     */
    COUNT_ALL("countAll"),/**
     * Count projection returning a numeric result. It starts with "countBy" keyword
     */
    COUNT_BY("countBy"),
    /**
     * Exists projection, returning typically a boolean result. It starts with "existsBy" keyword
     */
    EXISTS_BY("existsBy"),

    /**
     * Methods from {@link Object}
     */
    OBJECT_METHOD(""),
    /**
     * The method that belongs to the interface using a default method.
     */
    DEFAULT_METHOD(""),
    /**
     * The method that belongs to the interface using a custom repository.
     */
    CUSTOM_REPOSITORY(""),
    /**
     * Method that has {@link Query} annotation
     */
    QUERY("", Query.class),
    /**
     * Method that has {@link jakarta.data.repository.Save} annotation
     */
    SAVE("", Save.class),
    /**
     * The last condition is parameter based. That will match the parameter in a simple query.
     * It will check by {@link jakarta.data.repository.Find} annotation
     */
    PARAMETER_BASED("", Find.class),
    /**
     * Method that has {@link jakarta.data.repository.Insert} annotation
     */
    INSERT("", Insert.class),
    /**
     * Method that has {@link jakarta.data.repository.Delete} annotation
     */
    DELETE("", Delete.class),
    /**
     * Method that has {@link jakarta.data.repository.Update} annotation
     */
    UPDATE("", Update.class),

    /**
     * This method is defined by the return type of {@link jakarta.data.page.CursoredPage}
     */
    CURSOR_PAGINATION("");

    private static final Predicate<Class<?>> IS_REPOSITORY_METHOD = Predicate.<Class<?>>isEqual(CrudRepository.class)
            .or(Predicate.isEqual(BasicRepository.class))
            .or(Predicate.isEqual(NoSQLRepository.class));

    private static final Set<RepositoryType> KEY_WORLD_METHODS = EnumSet.of(FIND_BY, DELETE_BY, COUNT_ALL, COUNT_BY, EXISTS_BY);

    private static final Set<RepositoryType> OPERATION_ANNOTATIONS = EnumSet.of(INSERT, SAVE, DELETE, UPDATE, QUERY, PARAMETER_BASED);
    private final String keyword;

    private final Class<? extends Annotation> annotation;

    RepositoryType(String keyword) {
        this.keyword = keyword;
        this.annotation = null;
    }

    RepositoryType(String keyword, Class<? extends Annotation> annotation) {
        this.keyword = keyword;
        this.annotation = annotation;
    }


    /**
     * Returns an operation type from the {@link Method}
     *
     * @param method the method
     * @return a repository type
     */
    public static RepositoryType of(Method method, Class<?> repositoryType) {
        Objects.requireNonNull(method, "method is required");

        if (method.isDefault()) {
            return DEFAULT_METHOD;
        }

        Class<?> declaringClass = method.getDeclaringClass();
        if (Object.class.equals(declaringClass)) {
            return OBJECT_METHOD;
        }
        if (IS_REPOSITORY_METHOD.test(declaringClass)) {
            return DEFAULT;
        }
        if (!repositoryType.equals(declaringClass) && isCustomRepository(declaringClass)) {
            return CUSTOM_REPOSITORY;
        }
        if (method.getReturnType().equals(CursoredPage.class)) {
            return CURSOR_PAGINATION;
        }

        Predicate<RepositoryType> hasAnnotation = a -> method.getAnnotation(a.annotation) != null;
        if (OPERATION_ANNOTATIONS.stream().anyMatch(hasAnnotation)) {
            return OPERATION_ANNOTATIONS.stream()
                    .filter(hasAnnotation)
                    .findFirst().orElseThrow();
        }

        String methodName = method.getName();
        if (FIND_ALL.keyword.equals(methodName)) {
            return FIND_ALL;
        }

        return KEY_WORLD_METHODS.stream()
                .filter(k -> methodName.startsWith(k.keyword))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("The method " + method
                        + " is not supported. At the class " + declaringClass));
    }

    private static boolean isCustomRepository(Class<?> type) {
        try {
            return CDI.current().select(type).isResolvable();
        } catch (Exception e) {
            return false;
        }
    }
}
