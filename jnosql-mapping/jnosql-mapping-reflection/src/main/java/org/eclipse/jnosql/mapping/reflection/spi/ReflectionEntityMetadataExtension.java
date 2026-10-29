/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
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

    // Kept per extension instance, which is one per CDI container, so that the containers in an application
    // server, one per application, do not share their entities
    private final Map<Class<?>, EntityMetadata> entityMetadataByClass = new ConcurrentHashMap<>();
    private final Map<String, EntityMetadata> entityMetadataByEntityName = new ConcurrentHashMap<>();
    private final Map<Class<?>, ProjectionMetadata> projectorMetadataByClass = new ConcurrentHashMap<>();

    /**
     * Registers reflection entity metadata beans after CDI bean discovery.
     *
     * @param event the CDI after-bean-discovery event
     * @param bm the bean manager
     */
    public void afterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager bm) {

        scanEntitiesAndEmbeddableEntities();

        event.addBean()
                .read(bm.createAnnotatedType(CDIGroupEntityMetadata.class))
                .beanClass(CDIGroupEntityMetadata.class)
                .alternative(true)
                .priority(Interceptor.Priority.APPLICATION)
                .scope(ApplicationScoped.class)
                .createWith(context -> new CDIGroupEntityMetadata(entityMetadataByEntityName,
                        entityMetadataByClass, projectorMetadataByClass));
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
                        entityMetadataByEntityName.put(entityMetadata.name(), entityMetadata);
                    }
                    entityMetadataByClass.put(entity, entityMetadata);
                });

        scanner.embeddables()
                .forEach(embeddable -> {
                    EntityMetadata entityMetadata = converter.apply(embeddable);
                    entityMetadataByClass.put(embeddable, entityMetadata);
                });

        scanner.projections().forEach(projection -> {
            var projectionMetadata = projectionConverter.apply(projection);
            projectorMetadataByClass.put(projection, projectionMetadata);
        });

        LOGGER.fine(() ->"Finishing the scanning with: %d Entity and Embeddable scanned classes and %s Named entities"
                .formatted(entityMetadataByClass.size(), entityMetadataByEntityName.size()));
    }

    public static class CDIGroupEntityMetadata implements GroupEntityMetadata {

        private final Map<String, EntityMetadata> mappings;
        private final Map<Class<?>, EntityMetadata> classes;
        private final Map<Class<?>, ProjectionMetadata> projections;

        CDIGroupEntityMetadata(Map<String, EntityMetadata> mappings, Map<Class<?>, EntityMetadata> classes,
                               Map<Class<?>, ProjectionMetadata> projections) {
            this.mappings = mappings;
            this.classes = classes;
            this.projections = projections;
        }

        /**
         * Required by CDI for the client proxy.
         */
        protected CDIGroupEntityMetadata() {
            this(Map.of(), Map.of(), Map.of());
        }

        @Override
        public Map<String, EntityMetadata> mappings() {
            return mappings;
        }

        @Override
        public Map<Class<?>, EntityMetadata> classes() {
            return classes;
        }

        @Override
        public Map<Class<?>, ProjectionMetadata> projections() {
            return projections;
        }

    }
}
