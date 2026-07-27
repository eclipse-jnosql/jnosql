/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.repository.operations;

import jakarta.data.event.PostDeleteEvent;
import jakarta.data.event.PostInsertEvent;
import jakarta.data.event.PostUpdateEvent;
import jakarta.data.event.PostUpsertEvent;
import jakarta.data.event.PreDeleteEvent;
import jakarta.data.event.PreInsertEvent;
import jakarta.data.event.PreUpdateEvent;
import jakarta.data.event.PreUpsertEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;


@ApplicationScoped
class DefaultLifecycleEventHandler implements LifecycleEventHandler {

    private final Event<Object> events;

    @Inject
    DefaultLifecycleEventHandler(@Any Event<Object> events) {
        this.events = events;
    }

    @Override
    public <T> void preDelete(T entity) {
        events.select(new TypeLiteral<PreDeleteEvent<T>>() {}).fire(new PreDeleteEvent<>(requireEntity(entity)));
    }

    @Override
    public <T> void preInsert(T entity) {
        T safeEntity = requireEntity(entity);
        Class<?> entityClass = safeEntity.getClass(); // This resolves to Book.class

        // 1. Build the dynamic runtime parameterized type structure: PreInsertEvent<Book>
        Type dynamicType = new DynamicParameterizedType(PreInsertEvent.class, entityClass);

        // 2. Instantiate a standard TypeLiteral via a specialized Reflection bypass anonymous block
        TypeLiteral<PreInsertEvent<T>> typeLiteral = new TypeLiteral<PreInsertEvent<T>>() {};

        try {
            // 3. Weld / OpenWebBeans rely on an internal private field called "actualType" inside TypeLiteral.
            // We override this field directly to bypass the final getType() restriction!
            Field field = TypeLiteral.class.getDeclaredField("actualType");
            field.setAccessible(true);
            field.set(typeLiteral, dynamicType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dynamic type definition into CDI TypeLiteral context", e);
        }

        // 4. Build your concrete payload instance
        PreInsertEvent<T> eventInstance = new PreInsertEvent<>(safeEntity);

        // 5. This will compile flawlessly and successfully trigger @Observes PreInsertEvent<Book>
        CDI.current().getBeanManager().getEvent()
                .select(typeLiteral)
                .fire(eventInstance);
    }

    @Override
    public <T> void preUpdate(T entity) {
        events.fire(new PreUpdateEvent<>(requireEntity(entity)));
    }

    @Override
    public <T> void preUpsert(T entity) {
        events.fire(new PreUpsertEvent<>(requireEntity(entity)));
    }

    @Override
    public <T> void postDelete(T entity) {
        events.fire(new PostDeleteEvent<>(requireEntity(entity)));
    }

    @Override
    public <T> void postInsert(T entity) {
        events.fire(new PostInsertEvent<>(requireEntity(entity)));
    }

    @Override
    public <T> void postUpdate(T entity) {
        events.fire(new PostUpdateEvent<>(requireEntity(entity)));
    }

    @Override
    public <T> void postUpsert(T entity) {
        events.fire(new PostUpsertEvent<>(requireEntity(entity)));
    }

    private static <T> T requireEntity(T entity) {
        return Objects.requireNonNull(entity, "entity must not be null");
    }

    static class DynamicParameterizedType implements ParameterizedType {
        private final Class<?> rawType;
        private final Type[] actualTypeArguments;

        // Constructor accepts the raw class (e.g., PreDeleteEvent.class)
        // and the type arguments (e.g., Book.class)
        public DynamicParameterizedType(Class<?> rawType, Type... actualTypeArguments) {
            this.rawType = rawType;
            this.actualTypeArguments = actualTypeArguments;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments;
        }

        @Override
        public Class<?> getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return null; // Nested classes would use this, but for events null is perfect
        }

        // Recommended: Good practice to implement equals and hashCode for CDI caching
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParameterizedType that)) return false;
            return java.util.Objects.equals(rawType, that.getRawType()) &&
                    Arrays.equals(actualTypeArguments, that.getActualTypeArguments());
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(actualTypeArguments) ^ java.util.Objects.hashCode(rawType);
        }
    }


}
