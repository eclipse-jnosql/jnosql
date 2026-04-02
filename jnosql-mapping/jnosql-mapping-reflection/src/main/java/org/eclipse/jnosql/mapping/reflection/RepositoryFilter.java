/*
 *  Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.data.repository.Repository;
import jakarta.nosql.Entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A filter validate Repository that Eclipse JNoSQL supports. It will check the first parameter
 * on the repository, and if the entity has not had an unsupported annotation,
 * it will return false and true to supported Repository.
 */
enum RepositoryFilter implements Predicate<Class<?>> {

    INSTANCE;

    private static final String PROVIDER = "Eclipse_JNoSQL"; //TODO move this to a public location accessible to users

    @Override
    public boolean test(Class<?> type) {
        return isSupported(type) && isValid(type);
    }

    /**
     * Supported if the provided repository type is Annotated with
     * {@link Repository} and has a provider of 
     * {@link RepositoryFilter#PROVIDER} or {@link Repository#ANY_PROVIDER}
     * 
     * @param type The repository type
     * @return if the repository is supported
     */
    public boolean isSupported(Class<?> type) {
        Optional<String> provider = getProvider(type);
        return provider.map(p -> Repository.ANY_PROVIDER.equals(p) || PROVIDER.equalsIgnoreCase(p))
                       .orElse(false);
    }

    /**
     * Invalid if the provided repository type is parameterized with
     * an entity type that is not annotated with the {@link Entity} annotation.
     * If the entity cannot be determined from generic parameters, it will
     * attempt to extract it from the repository method signatures.
     *
     * @param type The repository type
     * @return if the repository is valid
     */
    public boolean isValid(Class<?> type) {
        Optional<Class<?>> entity = getEntity(type);
        
        // If entity not found from generic interface, try to extract from methods
        if (entity.isEmpty()) {
            entity = getEntityFromMethods(type);
        }

        return entity.map(c -> c.getAnnotation(Entity.class))
                .isPresent();
    }

    private Optional<String> getProvider(Class<?> repository) {
        Annotation[] annos = repository.getAnnotations();       
        return Stream.of(annos)
                .filter(Repository.class::isInstance)
                .map(a -> ((Repository) a).provider())
                .findAny(); // @Repostiory and provider are not repeatable and thus only 1 or 0 can be present
    }


    private Optional<Class<?>> getEntity(Class<?> repository) {
        Type[] interfaces = repository.getGenericInterfaces();
        if (interfaces.length == 0) {
            return Optional.empty();
        }
        ParameterizedType param = (ParameterizedType) interfaces[0];
        Type[] arguments = param.getActualTypeArguments();
        if (arguments.length == 0) {
            return Optional.empty();
        }
        Type argument = arguments[0];
        if (argument instanceof Class<?> entity) {
            return Optional.of(entity);
        }
        return Optional.empty();
    }

    /**
     * Checks the repository methods to find the entity class, including the first level
     * of parameterized types, as these can contain the entity class.
     *
     * @param repository The repository type
     * @return The entity class if found
     */
    private Optional<Class<?>> getEntityFromMethods(Class<?> repository) {
        List<Method> methods = Arrays.asList(repository.getDeclaredMethods());
        for (Method method : methods) {
            Optional<Class<?>> clazz = extractClass(method.getGenericReturnType());
            if (clazz.filter(c -> c.isAnnotationPresent(Entity.class)).isPresent()) { 
                return clazz; 
            }
            for (Type type : method.getGenericParameterTypes()) {
                clazz = extractClass(type);
                if (clazz.filter(c -> c.isAnnotationPresent(Entity.class)).isPresent()) { 
                    return clazz; 
                }
            }
        }
        return Optional.empty();
    }
    
    /**
     * Extracts a concrete class from a Type, unwrapping parameterized types.
     * Only unwraps one level of parameterized types, and only the first actual type argument.
     */
    private Optional<Class<?>> extractClass(Type type) {
        Type classType = type;
        if (classType instanceof ParameterizedType paramType) {
            classType = paramType.getActualTypeArguments().length > 0 ? paramType.getActualTypeArguments()[0] : null;
        }
        if (classType != null && classType instanceof Class<?> clazz) {
            return Optional.of(clazz);
        }
        return Optional.empty();
    }

}
