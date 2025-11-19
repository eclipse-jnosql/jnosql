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
import jakarta.nosql.Column;
import jakarta.nosql.Projection;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionParameterMetadata;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;

public class ProjectionConverter implements Function<Class<?>, ProjectionMetadata> {

    private static final Logger LOGGER = Logger.getLogger(ProjectionConverter.class.getName());

    @Override
    public ProjectionMetadata apply(Class<?> type) {
        Objects.requireNonNull(type, "type is required");
        if(!type.isRecord()) {
            throw new IllegalArgumentException("The type " + type.getName() + " is not record");
        }
        LOGGER.fine(() -> "Converting " + type.getName() + " to ProjectionMetadata");

        var className = type.getName();
        var constructor = type.getDeclaredConstructors()[0];
        var from = Optional.ofNullable(type.getAnnotation(Projection.class))
                .map(Projection::from).orElse(null);
        var projectionConstructor = new ReflectionProjectionConstructorMetadata(parameters(constructor.getParameters()),
                constructor);
        return new ReflectionProjectionMetadata(className, type, from, projectionConstructor);
    }

    private List<ProjectionParameterMetadata> parameters(Parameter[] components){
        List<ProjectionParameterMetadata> parameters = new ArrayList<>();
        for (var component : components) {
            var name = getName(component);
            var type = component.getType();
            parameters.add(new ReflectionProjectionParameterMetadata(name, type));
        }
        return parameters;
    }

    private static String getName(Parameter component) {
        Optional<String> nameFromColumn = Optional.ofNullable(component.getAnnotation(Column.class)).map(Column::value);
        Optional<String> nameFromSelect = Optional.ofNullable(component.getAnnotation(Select.class)).map(Select::value);

        return nameFromColumn
                .or(() -> nameFromSelect)
                .orElse(component.getName());
    }

}
