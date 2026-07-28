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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.enterprise.util.TypeLiteral;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds a {@link TypeLiteral} that represents a parameterized Jakarta Data lifecycle event
 * type, such as {@code PreInsertEvent<Book>}, whose entity type is only known at runtime.
 * <p>
 * A {@code TypeLiteral} normally resolves its type from the generic superclass of an anonymous
 * subclass, so {@code new TypeLiteral<PreInsertEvent<T>>() {}} resolves to a type that still
 * contains the unresolved type variable {@code T}. CDI then falls back to the raw runtime class
 * of the event object, and an observer declared as {@code @Observes PreInsertEvent<Book>} is
 * never notified: under the CDI assignability rules a raw event type is not assignable to a
 * parameterized observed type.
 * </p>
 * <p>
 * Since {@link TypeLiteral#getType()} is {@code final}, the only way to supply a type computed
 * at runtime is to write the resolved type into the {@code actualType} field that
 * {@code getType()} caches. That field belongs to the CDI API class itself, so the technique
 * behaves the same on every CDI implementation, but it is a private, undocumented detail:
 * </p>
 * <ul>
 * <li>the {@code jakarta.cdi} module <em>exports</em> {@code jakarta.enterprise.util} without
 *     <em>opening</em> it, so the field is inaccessible when the CDI API is loaded as a named
 *     module;</li>
 * <li>the field name carries no specification guarantee and may change between API versions;</li>
 * <li>ahead-of-time compiled runtimes require explicit reflection registration for it.</li>
 * </ul>
 * <p>
 * Accessibility is therefore probed once, and {@link #of(Class, Class)} returns an empty
 * {@link Optional} when the probe fails so that callers can degrade instead of failing. Literals
 * are cached per event type and entity type because they are immutable and the same pair recurs
 * on every repository call.
 * </p>
 */
final class LifecycleEventTypeLiteral {

    private static final Logger LOGGER = Logger.getLogger(LifecycleEventTypeLiteral.class.getName());

    private static final String ACTUAL_TYPE = "actualType";

    private static final Field ACTUAL_TYPE_FIELD = actualTypeField();

    private static final Map<Key, TypeLiteral<?>> CACHE = new ConcurrentHashMap<>();

    private LifecycleEventTypeLiteral() {
    }

    /**
     * Returns a literal for {@code eventType} parameterized with {@code entityType}.
     *
     * @param eventType  the raw lifecycle event class, such as {@code PreInsertEvent.class}
     * @param entityType the entity type to use as the event type argument
     * @param <E>        the parameterized event type
     * @return the literal, or {@link Optional#empty()} if this runtime does not allow the
     * resolved type to be supplied to CDI
     */
    @SuppressWarnings("unchecked")
    static <E> Optional<TypeLiteral<E>> of(Class<?> eventType, Class<?> entityType) {
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(entityType, "entityType must not be null");
        if (ACTUAL_TYPE_FIELD == null) {
            return Optional.empty();
        }
        var literal = CACHE.computeIfAbsent(new Key(eventType, entityType), LifecycleEventTypeLiteral::create);
        return Optional.ofNullable((TypeLiteral<E>) literal);
    }

    private static TypeLiteral<?> create(Key key) {
        TypeLiteral<?> literal = new TypeLiteral<>() {
        };
        try {
            ACTUAL_TYPE_FIELD.set(literal, new EntityParameterizedType(key.eventType(), key.entityType()));
            return literal;
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.log(Level.WARNING, e, () -> "Unable to resolve the lifecycle event type "
                    + key.eventType().getName() + '<' + key.entityType().getName() + '>');
            return null;
        }
    }

    private static Field actualTypeField() {
        try {
            Field field = TypeLiteral.class.getDeclaredField(ACTUAL_TYPE);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.log(Level.WARNING, e, () -> "This runtime does not allow access to "
                    + TypeLiteral.class.getName() + '.' + ACTUAL_TYPE
                    + ", so Jakarta Data lifecycle events are fired with their raw type. Observers "
                    + "declaring a parameterized event type, such as @Observes PreInsertEvent<Book>, "
                    + "are not notified. Add --add-opens jakarta.cdi/jakarta.enterprise.util=ALL-UNNAMED "
                    + "when the CDI API is loaded as a named module.");
            return null;
        }
    }

    private record Key(Class<?> eventType, Class<?> entityType) {
    }

    /**
     * The resolved event type. {@code equals} accepts any {@link ParameterizedType} and
     * {@code hashCode} follows the convention of the JDK implementation so that this type
     * interoperates with the type representations a CDI implementation uses internally.
     */
    private static final class EntityParameterizedType implements ParameterizedType {

        private final Class<?> rawType;

        private final Type[] actualTypeArguments;

        private EntityParameterizedType(Class<?> rawType, Type... actualTypeArguments) {
            this.rawType = rawType;
            this.actualTypeArguments = actualTypeArguments;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments.clone();
        }

        @Override
        public Class<?> getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ParameterizedType that)) {
                return false;
            }
            return that.getOwnerType() == null
                    && Objects.equals(rawType, that.getRawType())
                    && Arrays.equals(actualTypeArguments, that.getActualTypeArguments());
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(actualTypeArguments) ^ Objects.hashCode(rawType);
        }

        @Override
        public String toString() {
            var arguments = new StringJoiner(", ", "<", ">");
            for (Type argument : actualTypeArguments) {
                arguments.add(argument.getTypeName());
            }
            return rawType.getTypeName() + arguments;
        }
    }
}
