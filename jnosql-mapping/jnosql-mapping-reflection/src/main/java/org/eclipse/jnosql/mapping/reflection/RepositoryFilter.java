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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
     * @return
     */
    public boolean isSupported(Class<?> type) {
        Optional<String> provider = getProvider(type);
        return provider.map(p -> Repository.ANY_PROVIDER.equals(p) || PROVIDER.equalsIgnoreCase(p))
                .isPresent();
    }

    /**
     * Invalid if the provided repository type is parameterized with
     * an entity type that is not annotated with the {@link Entity} annotation
     * 
     * @param type The repository type
     * @return if the repository is valid
     */
    public boolean isValid(Class<?> type) {
        Optional<Class<?>> entity = getEntity(type);
        return entity.map(c -> c.getAnnotation(Entity.class))
                .isPresent();
    }

    private Optional<String> getProvider(Class<?> repository) {
        Annotation[] annos = repository.getAnnotations();       
        return Stream.of(annos)
                .filter(a -> Repository.class.isInstance(a))
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

}
