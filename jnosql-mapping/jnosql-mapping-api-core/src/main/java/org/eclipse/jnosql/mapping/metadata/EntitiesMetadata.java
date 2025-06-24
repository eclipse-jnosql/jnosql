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
package org.eclipse.jnosql.mapping.metadata;


import java.util.Map;
import java.util.Optional;

/**
 * This class is the {@link EntityMetadata} repository.
 * So it contains entities that has the {@link jakarta.nosql.Entity} annotation.
 */
public interface EntitiesMetadata {

    /**
     * Find a class in the cached way and return in a class,
     * if it's not found the class will be both, loaded and cached, when this method is called
     *
     * @param entity the class of entity
     * @return the {@link EntityMetadata}
     * @throws NullPointerException when class entity is null
     */
    EntityMetadata get(Class<?> entity);

    /**
     * Find the {@link InheritanceMetadata} where the parameter is the parent parameter
     * and it returns a map group by the {@link jakarta.nosql.DiscriminatorValue}
     * @param parent the parent
     * @return a {@link Map}
     * @throws NullPointerException when parent is null
     */
    Map<String, InheritanceMetadata> findByParentGroupByDiscriminatorValue(Class<?> parent);

    /**
     * Returns the {@link EntityMetadata} instance from {@link EntityMetadata#name()} in ignore case
     *
     * @param name the name to select ah {@link EntityMetadata} instance
     * @return the {@link EntityMetadata} from name
     * @throws ClassInformationNotFoundException when the class is not loaded
     * @throws NullPointerException              when the name is null
     */
    EntityMetadata findByName(String name);

    /**
     * Returns the {@link EntityMetadata} instance from {@link Class#getSimpleName()}
     *
     * @param name the name of {@link Class#getSimpleName()} instance
     * @return the {@link EntityMetadata} from name otherwise {@link Optional#empty()}
     * @throws NullPointerException              when the name is null
     */
    Optional<EntityMetadata> findBySimpleName(String name);

    /**
     * Returns the {@link EntityMetadata} instance from {@link Class#getName()}
     *
     * @param name the name of {@link Class#getName()} instance
     * @return the {@link EntityMetadata} from name otherwise {@link Optional#empty()}
     * @throws NullPointerException              when the name is null
     */
    Optional<EntityMetadata> findByClassName(String name);

    /**
     * Returns the {@link ProjectionMetadata} for the given projector class.
     *
     * @param projection the projector class
     * @return the {@link ProjectionMetadata} for the projector class
     * @throws NullPointerException when projector is null
     */
    Optional<ProjectionMetadata> projection(Class<?> projection);

}
