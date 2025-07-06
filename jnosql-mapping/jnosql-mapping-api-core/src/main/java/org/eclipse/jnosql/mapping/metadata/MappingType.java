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


import jakarta.nosql.Embeddable;
import jakarta.nosql.Entity;

import java.util.Collection;
import java.util.Map;

/**
 * enum that contains kinds of annotations to either fields constructor parameters on java.
 */
public enum MappingType {
    EMBEDDED, EMBEDDED_GROUP, MAP, COLLECTION, DEFAULT, ENTITY, ARRAY;


    /**
     * select you the kind of annotation on field and then define a enum type, follow the sequences:
     * <ul>
     * <li>Collection</li>
     * <li>Map</li>
     * <li>embedded</li>
     * </ul>.
     *
     * @param type - the type
     * @return the type
     */
    public static MappingType of(Class<?> type) {
        if (Collection.class.isAssignableFrom(type)) {
            return COLLECTION;
        }
        if (Map.class.isAssignableFrom(type)) {
            return MAP;
        }
        if (type.isAnnotationPresent(Entity.class)) {
            return ENTITY;
        }
        if (type.isAnnotationPresent(Embeddable.class)) {
            var value = type.getAnnotation(Embeddable.class).value();
            return value == Embeddable.EmbeddableType.FLAT ? EMBEDDED : EMBEDDED_GROUP;
        }
        if(type.isArray()) {
            return ARRAY;
        }
        return DEFAULT;
    }
}
