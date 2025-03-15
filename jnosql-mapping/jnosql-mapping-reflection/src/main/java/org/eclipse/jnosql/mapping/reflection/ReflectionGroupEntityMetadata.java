/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.GroupEntityMetadata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@ApplicationScoped
public class ReflectionGroupEntityMetadata implements GroupEntityMetadata {

    private final Map<String, EntityMetadata> mappings = new ConcurrentHashMap<>();

    private final Map<Class<?>, EntityMetadata> classes = new ConcurrentHashMap<>();

    public ReflectionGroupEntityMetadata() {
        ClassConverter converter = ClassConverter.load();
        ClassScanner scanner = ClassScanner.load();
        for (Class<?> entity : scanner.entities()) {
            EntityMetadata entityMetadata = converter.apply(entity);
            if (entityMetadata.hasEntityName()) {
                mappings.put(entityMetadata.name(), entityMetadata);
            }
            classes.put(entity, entityMetadata);
        }
        for (Class<?> embeddable : scanner.embeddables()) {
            EntityMetadata entityMetadata = converter.apply(embeddable);
            classes.put(embeddable, entityMetadata);
        }
    }

    @Override
    public Map<String, EntityMetadata> mappings() {
        return mappings;
    }

    @Override
    public Map<Class<?>, EntityMetadata> classes() {
        return classes;
    }
}
