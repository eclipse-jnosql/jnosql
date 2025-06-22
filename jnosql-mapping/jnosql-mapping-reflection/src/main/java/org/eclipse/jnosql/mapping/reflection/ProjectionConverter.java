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
 */
package org.eclipse.jnosql.mapping.reflection;

import jakarta.data.repository.Select;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionParameterMetadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

class ProjectionConverter implements Function<Class<?>, ProjectionMetadata> {

    private static final Logger LOGGER = Logger.getLogger(ProjectionConverter.class.getName());

    private final Reflections reflections;

    ProjectionConverter() {
        this.reflections = new Reflections();
    }

    @Override
    public ProjectionMetadata apply(Class<?> type) {
        Objects.requireNonNull(type, "type is required");
        if(!type.isRecord()) {
            throw new IllegalArgumentException("The type " + type.getName() + " is not record");
        }
        LOGGER.fine(() -> "Converting " + type.getName() + " to ProjectionMetadata");

        var className = type.getName();
        var constructor = type.getDeclaredConstructors()[0];
        var projectionConstructor = new ReflectionProjectionConstructorMetadata(parameters(type.getRecordComponents()), constructor);
        return new ReflectionProjectionMetadata(className, type, projectionConstructor);
    }

    private List<ProjectionParameterMetadata> parameters(RecordComponent[] components){
        List<ProjectionParameterMetadata> parameters = new ArrayList<>();
        for (var component : components) {
            var name = Optional.ofNullable(component.getAnnotation(Select.class)).map(Select::value)
                    .orElse(component.getName());
            var type = component.getType();
            parameters.add(new ReflectionProjectionParameterMetadata(name, type));
        }
        return parameters;
    }
}
