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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.data.exceptions.MappingException;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.TypeLiteral;
import org.eclipse.jnosql.mapping.metadata.ProjectionBuilder;
import org.eclipse.jnosql.mapping.metadata.ProjectionConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionParameterMetadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

class ReflectionProjectionBuilder implements ProjectionBuilder {

    private final List<Object> values = new ArrayList<>();

    private final ProjectionConstructorMetadata metadata;


    ReflectionProjectionBuilder(ProjectionConstructorMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public List<ProjectionParameterMetadata> parameters() {
        return metadata.parameters();
    }

    @Override
    public void add(Object value) {
        this.values.add(value);
    }

    @Override
    public void addEmptyParameter() {
        Constructor<?> constructor = ((ReflectionProjectionConstructorMetadata) this.metadata).constructor();
        Class<?> type = constructor.getParameterTypes()[this.values.size()];
        if(boolean.class.equals(type)) {
            this.values.add(Boolean.FALSE);
        } else if(char.class.equals(type)) {
            this.values.add((char) 0);
        } else if(type.isPrimitive()) {
            this.values.add((byte) 0);
        } else {
            this.values.add(null);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T build() {
        Constructor<?> constructor = ((ReflectionProjectionConstructorMetadata) this.metadata).constructor();
        try {
            Instance<Event<ConstructorEvent>> instance = CDI.current().select(new TypeLiteral<>() {
            });
            Event<ConstructorEvent> event = instance.get();
            event.fire(ConstructorEvent.of(constructor, values.toArray()));
            return (T) constructor.newInstance(values.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new MappingException("There is an issue to create a new instance of this class" +
                    " using this constructor: " + constructor, e);
        }
    }
}
