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

import jakarta.data.Sort;
import jakarta.data.constraint.Constraint;
import jakarta.data.repository.By;
import jakarta.data.repository.First;
import jakarta.data.repository.Is;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Select;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryParam;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

enum ReflectionRepositorySupplier implements Function<Class<?>, RepositoryMetadata> {

    INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(ReflectionRepositorySupplier.class.getName());

    @Override
    public RepositoryMetadata apply(Class<?> type) {
        Objects.requireNonNull(type, "type is required");
        if (!type.isInterface()) {
            throw new IllegalArgumentException("The repository type " + type.getName() + " is not an interface");
        }
        Class<?> entity = findEntity(type.getGenericInterfaces());
        List<RepositoryMethod> methods = new ArrayList<>(type.getDeclaredMethods().length);
        Map<Method, RepositoryMethod> methodByMethodReflection = new HashMap<>(type.getDeclaredMethods().length);
        for (Method method : type.getDeclaredMethods()) {
            RepositoryMethod repositoryMethod = to(method);
            methods.add(repositoryMethod);
            methodByMethodReflection.put(method, repositoryMethod);
        }
        LOGGER.finest(() -> "The repository " + type.getName() + " has " + methods.size() + " methods");
        return new ReflectionRepositoryMetadata(type, entity, methods, methodByMethodReflection);
    }

    private RepositoryMethod to(Method method) {

        String name = method.getName();
        RepositoryType type = RepositoryTypeConverter.of(method);
        String queryValue = Optional.ofNullable(method.getAnnotation(Query.class))
                .map(Query::value).orElse(null);
        Integer firstValue = Optional.ofNullable(method.getAnnotation(First.class))
                .map(First::value).orElse(null);
        Class<?> returnTypeValue = method.getReturnType();
        Class<?> elementTypeValue = getElementTypeValue(method);

        List<RepositoryParam> params = to(method.getParameters());

        List<Sort<?>> sorts = to(method.getAnnotationsByType(OrderBy.class));
        List<String> select = Arrays.stream(method.getDeclaredAnnotationsByType(Select.class))
                .map(Select::value)
                .toList();
        List<String> annotations = Arrays.stream(method.getAnnotations())
                .map(annotation -> annotation.annotationType().getName())
                .distinct()
                .toList();

        return new ReflectionRepositoryMethod(name,
                type,
                queryValue,
                firstValue,
                returnTypeValue,
                elementTypeValue,
                params,
                sorts,
                select,
                annotations);
    }

    private static Class<?> getElementTypeValue(Method method) {
        if( method.getGenericReturnType() instanceof ParameterizedType parameterizedType){
            Type[] arguments = parameterizedType.getActualTypeArguments();
            if(arguments.length > 0){
                return  (Class<?>) arguments[0];
            }
        }
        return null;
    }

    private List<Sort<?>> to(OrderBy[] orderBys) {
        List<Sort<?>> sorts = new ArrayList<>(orderBys.length);
        for (OrderBy orderBy : orderBys) {
            sorts.add(new Sort<>(orderBy.value(), !orderBy.descending(), orderBy.ignoreCase()));
        }
        return sorts;
    }


    @SuppressWarnings("unchecked")
    private List<RepositoryParam> to(Parameter[] parameters) {
        List<RepositoryParam> params = new ArrayList<>(parameters.length);
        for (Parameter parameter : parameters) {
            Class<? extends Constraint<?>> isValue = (Class<? extends Constraint<?>>) Optional.ofNullable(parameter
                    .getAnnotation(Is.class))
                    .map(Is::value)
                    .orElse(null);
            String name = Optional.ofNullable(parameter.getAnnotation(Param.class))
                    .map(Param::value)
                    .orElse(parameter.getName());
            String by = Optional.ofNullable(parameter.getAnnotation(By.class))
                    .map(By::value)
                    .orElse(parameter.getName());
            Class<?> type = parameter.getType();
            params.add(new ReflectionRepositoryParam(isValue, name, by, type));
        }
        return params;
    }

    private Class<?> findEntity(Type[] genericInterfaces) {
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType parameterizedType) {
                Type[] arguments = parameterizedType.getActualTypeArguments();
                if (arguments.length > 0) {
                    Type entityType = arguments[0];
                    if (entityType instanceof Class<?> entity) {
                        return entity;
                    }
                }
            }
        }
        return null;
    }
}
