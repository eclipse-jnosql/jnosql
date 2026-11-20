/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 */
package org.eclipse.jnosql.mapping.metadata.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An immutable description of a repository method return type that may hold other types, such as
 * {@code CompletionStage<Optional<List<Person>>>}.
 *
 * <p>Each level has a {@link #rawType() generic type}, for example {@code List}, and the
 * {@link #arguments() arguments} that specialize it, for example {@code Person}. An argument is itself a
 * {@code ReturnType}, so any nesting is expressed by the same structure. The description is plain data with no
 * {@link java.lang.reflect.Type} dependency, so it can be produced by reflection or by an annotation processor.</p>
 *
 * <pre>{@code
 * ReturnType type = ReturnType.of(CompletionStage.class,
 *         ReturnType.of(List.class, ReturnType.of(Person.class)));
 *
 * type.rawType();            // CompletionStage
 * type.argument();           // List<Person>, as an Optional
 * type.element().rawType();  // Person
 * }</pre>
 *
 * @param rawType   the generic type of this level
 * @param arguments the types that specialize it, empty for a plain class
 */
public record ReturnType(Class<?> rawType, List<ReturnType> arguments) {

    /**
     * Validates and defensively copies the components.
     *
     * @throws NullPointerException when a component is null
     */
    public ReturnType {
        Objects.requireNonNull(rawType, "rawType is required");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments is required"));
    }

    /**
     * Creates a description of a type with the given arguments.
     *
     * @param rawType   the generic type
     * @param arguments the types that specialize it, possibly none
     * @return the description
     * @throws NullPointerException when the raw type or an argument is null
     */
    public static ReturnType of(Class<?> rawType, ReturnType... arguments) {
        Objects.requireNonNull(arguments, "arguments is required");
        return new ReturnType(rawType, List.of(arguments));
    }

    /**
     * Returns the first argument, which is the specialization of a single-argument type such as
     * {@code List<Person>}.
     *
     * @return the first argument, or empty for a plain class
     */
    public Optional<ReturnType> argument() {
        return arguments.stream().findFirst();
    }

    /**
     * Returns the innermost type following the first arguments, which is the entity or projection in
     * {@code CompletionStage<List<Person>>}.
     *
     * @return the innermost description, this one when it has no arguments
     */
    public ReturnType element() {
        return argument().map(ReturnType::element).orElse(this);
    }

    /**
     * Indicates whether the generic type is, or extends, the given type.
     *
     * @param type the type to compare
     * @return {@code true} when the raw type is assignable to the given type
     * @throws NullPointerException when the type is null
     */
    public boolean isA(Class<?> type) {
        return Objects.requireNonNull(type, "type is required").isAssignableFrom(rawType);
    }
}
