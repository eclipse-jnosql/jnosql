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
package org.eclipse.jnosql.mapping.semistructured;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionBuilder;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A converter that transforms an entity into a projection based on the provided metadata. This class is designed to be
 * used in a Jakarta EE environment, specifically within an application scope.
 **/
@ApplicationScoped
public class ProjectorConverter {

    private static final Logger LOGGER = Logger.getLogger(ProjectorConverter.class.getName());

    private final EntitiesMetadata entitiesMetadata;


    @Inject
    public ProjectorConverter(EntitiesMetadata entitiesMetadata) {
        this.entitiesMetadata = entitiesMetadata;
    }

    ProjectorConverter() {
        this(null);
    }


    /**
     * Converts the given entity to a projection based on the provided metadata.
     *
     * @param entity   the entity to be converted, must not be null
     * @param metadata the metadata defining the projection, must not be null
     * @param <T>      the type of the entity
     * @param <P>      the type of the projection
     * @return a projection of type P based on the entity and metadata
     * @throws NullPointerException if either entity or metadata is null
     */
    public <T, P> P map(T entity, ProjectionMetadata metadata) {
        Objects.requireNonNull(entity, "entity is required");
        Objects.requireNonNull(metadata, "metadata is required");
        LOGGER.fine(() -> "Converting entity " + entity + " to " + metadata);
        var entityMetadata = entitiesMetadata.get(entity.getClass());
        if (entityMetadata == null) {
            throw new IllegalArgumentException("Entity metadata not found for " + entity.getClass());
        }

        var constructor = metadata.constructor();
        var builder = ProjectionBuilder.of(constructor);

        for (var parameter : constructor.parameters()) {
            String name = parameter.name();
            Optional<Object> value = value(entityMetadata, name, entity);
            if (value.isEmpty()) {
                LOGGER.warning(() -> "Field metadata not found for parameter: " + name);
                builder.addEmptyParameter();
            } else {
                var parameterValue = value.orElseThrow();
                Class<?> parameterType = parameter.type();
                var converted = Value.of(parameterValue).get(parameterType);
                builder.add(converted);
            }
        }
        return builder.build();
    }

    public <T, P> P map(T entity, ProjectionMetadata metadata, List<String> fields) {
        Objects.requireNonNull(entity, "entity is required");
        Objects.requireNonNull(metadata, "metadata is required");
        Objects.requireNonNull(fields, "fields is required");
        LOGGER.fine(() -> "Converting entity " + entity + " to " + metadata + " with fields " + fields);
        var entityMetadata = entitiesMetadata.get(entity.getClass());
        if (entityMetadata == null) {
            throw new IllegalArgumentException("Entity metadata not found for " + entity.getClass());
        }
        var constructor = metadata.constructor();
        var builder = ProjectionBuilder.of(constructor);
        if (constructor.parameters().size() != fields.size()) {
            throw new IllegalArgumentException("The number of parameters for " + entity.getClass() + " is invalid by the fields size:" +
                    " " + fields);
        }

        for (int index = 0; index < fields.size(); index++) {
            var name = fields.get(index);
            var parameter = constructor.parameters().get(index);
            Optional<Object> value = value(entityMetadata, name, entity);
            if (value.isEmpty()) {
                LOGGER.warning(() -> "Field metadata not found for parameter: " + name);
                builder.addEmptyParameter();
            } else {
                var parameterValue = value.orElseThrow();
                Class<?> parameterType = parameter.type();
                var converted = Value.of(parameterValue).get(parameterType);
                builder.add(converted);
            }
        }

        return builder.build();
    }

    private <T> Optional<Object> value(EntityMetadata entityMetadata, String name, T entity) {
        String[] names = name.split("\\.");
        if (names.length == 1) {
            Optional<FieldMetadata> metadata = entityMetadata.fieldMapping(names[0]);
            return metadata.map(f -> f.read(entity));
        } else {
            String first = names[0];

            Optional<FieldMetadata> fieldMetadata = entityMetadata.fieldMapping(first);
            if (fieldMetadata.isEmpty()) {
                return Optional.empty();
            }
            var field = fieldMetadata.orElseThrow();
            Object read = field.read(entity);
            if (read == null) {
                return Optional.empty();
            }
            var embeddedField = entitiesMetadata.get(read.getClass());
            if (embeddedField == null) {
                throw new IllegalArgumentException("Entity metadata not found for " + read.getClass());
            }
            var embeddedName = Stream.of(names).skip(1).collect(Collectors.joining("."));
            return value(embeddedField, embeddedName, read);

        }
    }

}
