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

import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

class ReflectionRepositorySupplier implements Function<Class<?>, RepositoryMetadata> {

    private static final Logger LOGGER = Logger.getLogger(ReflectionRepositorySupplier.class.getName());

    @Override
    public RepositoryMetadata apply(Class<?> type) {
        Objects.requireNonNull(type, "type is required");
        if(!type.isInterface()) {
            throw new IllegalArgumentException("The type " + type.getName() + " is not an interface");
        }
        Class<?> entity = findEntity(type.getGenericInterfaces());
        List<RepositoryMethod> methods = new ArrayList<>(type.getDeclaredMethods().length);
        Map<Method, RepositoryMethod> methodByMethodReflection = new HashMap<>(type.getDeclaredMethods().length);
        for (Method method : type.getDeclaredMethods()) {
            RepositoryMethod repositoryMethod = to(type, method);

        }
        return new ReflectionRepositoryMetadata(type, entity, methods, methodByMethodReflection);
    }

    private RepositoryMethod to(Class<?> type, Method method) {
        return null;
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
