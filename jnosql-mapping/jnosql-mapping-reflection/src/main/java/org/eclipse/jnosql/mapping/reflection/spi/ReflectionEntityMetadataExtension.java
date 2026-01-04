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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.reflection.spi;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.interceptor.Interceptor;
import jakarta.nosql.Entity;
import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.GroupEntityMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;
import org.eclipse.jnosql.mapping.reflection.ProjectionConverter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;


/**
 * This class is a CDI extension to load all class that has {@link Entity} annotation.
 * This extension will load all Classes and put in a map.
 * Where the key is {@link Class#getName()} and the value is {@link EntityMetadata}
 */
public class ReflectionEntityMetadataExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(ReflectionEntityMetadataExtension.class.getName());

    private static final Map<Class<?>, EntityMetadata> ENTITY_METADATA_BY_CLASS = new ConcurrentHashMap<>();
    private static final Map<String, EntityMetadata> ENTITY_METADATA_BY_ENTITY_NAME = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ProjectionMetadata> PROJECTOR_METADATA_BY_CLASS = new ConcurrentHashMap<>();

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager bm) {

        scanEntitiesAndEmbeddableEntities();

        event.addBean()
                .read(bm.createAnnotatedType(CDIGroupEntityMetadata.class))
                .beanClass(CDIGroupEntityMetadata.class)
                .alternative(true)
                .priority(Interceptor.Priority.APPLICATION)
                .scope(ApplicationScoped.class);
    }

    private void scanEntitiesAndEmbeddableEntities() {
        LOGGER.fine("Starting the scanning process for Entity and Embeddable annotations: ");
        ClassConverter converter = ClassConverter.load();
        ClassScanner scanner = ClassScanner.load();
        Function<Class<?>, ProjectionMetadata> projectionConverter = new ProjectionConverter();

        scanner.entities()
                .forEach(entity -> {
                    EntityMetadata entityMetadata = converter.apply(entity);
                    if (entityMetadata.hasEntityName()) {
                        ENTITY_METADATA_BY_ENTITY_NAME.put(entityMetadata.mappingName(), entityMetadata);
                    }
                    ENTITY_METADATA_BY_CLASS.put(entity, entityMetadata);
                });

        scanner.embeddables()
                .forEach(embeddable -> {
                    EntityMetadata entityMetadata = converter.apply(embeddable);
                    ENTITY_METADATA_BY_CLASS.put(embeddable, entityMetadata);
                });

        scanner.projections().forEach(projection -> {
            var projectionMetadata = projectionConverter.apply(projection);
            PROJECTOR_METADATA_BY_CLASS.put(projection, projectionMetadata);
        });

        LOGGER.fine(() ->"Finishing the scanning with: %d Entity and Embeddable scanned classes and %s Named entities"
                .formatted(ENTITY_METADATA_BY_CLASS.size(), ENTITY_METADATA_BY_ENTITY_NAME.size()));
    }

    public static class CDIGroupEntityMetadata implements GroupEntityMetadata {

        @Override
        public Map<String, EntityMetadata> mappings() {
            return ENTITY_METADATA_BY_ENTITY_NAME;
        }

        @Override
        public Map<Class<?>, EntityMetadata> classes() {
            return ENTITY_METADATA_BY_CLASS;
        }

        @Override
        public Map<Class<?>, ProjectionMetadata> projections() {
            return PROJECTOR_METADATA_BY_CLASS;
        }

    }
}
