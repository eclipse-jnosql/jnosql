/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */

package org.eclipse.jnosql.communication.semistructured;


import org.eclipse.jnosql.communication.TypeSupplier;
import org.eclipse.jnosql.communication.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * Represents a communication level entity in the Eclipse JNoSQL framework.
 * It serves as an API entity between the database and the communication level.
 * Each {@code CommunicationEntity} consists of a name and one or more {@link Element} objects.
 *
 * <p>{@code CommunicationEntity} provides methods to manipulate and retrieve data in a semistructured manner,
 * allowing for flexible handling of data that does not strictly adhere to a predefined schema.</p>
 *
 * @see Element
 * @see CommunicationEntity#elements()
 * @see CommunicationEntity#name()
 */
public class CommunicationEntity {

    private final Map<String, Element> elements = new HashMap<>();

    private final String name;

    CommunicationEntity(String name) {
        this.name = name;
    }


    /**
     * Adds all elements in the specified list to this communication entity.
     *
     * @param elements the elements to be added
     * @throws NullPointerException if the specified list is {@code null}
     */
    public void addAll(List<Element> elements) {
        requireNonNull(elements, "The object column is required");
        elements.forEach(this::add);
    }

    /**
     * Adds the specified element to this communication entity.
     *
     * @param element the element to be added
     * @throws NullPointerException if the specified element is {@code null}
     */
    public void add(Element element) {
        requireNonNull(element, "Column is required");
        this.elements.put(element.name(), element);
    }

    /**
     * Adds a column with the specified name and value to this communication entity.
     *
     * @param name  the name of the column
     * @param value the value of the column
     * @throws NullPointerException if the specified name is {@code null}
     */
    public void add(String name, Object value) {
        requireNonNull(name, "name is required");
        this.elements.put(name, Element.of(name, Value.of(value)));
    }

    /**
     * Adds a column with the specified name and value to this communication entity.
     *
     * @param name  the name of the column
     * @param value the value of the column
     * @throws NullPointerException if the specified name is {@code null}
     */
    public void add(String name, Value value) {
        requireNonNull(name, "name is required");
        this.elements.put(name, Element.of(name, value));
    }

    /**
     * Adds a column with a null value to this communication entity.
     *
     * @param name the name of the column to add; must not be {@code null}
     * @throws NullPointerException if the provided {@code name} is {@code null}
     */
    public void addNull(String name){
        requireNonNull(name, "name is required");
        this.elements.put(name, Element.of(name, Value.ofNull()));
    }

    /**
     * Converts the elements to a {@link Map} where each key represents the name of a column
     * and the corresponding value represents the value of that column obtained from {@link Value#get()}.
     *
     * @return an unmodifiable map containing the elements of this communication entity
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Element> entry : elements.entrySet()) {
            Element element = entry.getValue();
            map.put(entry.getKey(), convert(element.get()));
        }
        return Collections.unmodifiableMap(map);
    }


    /**
     * Returns all elements contained in this communication entity.
     *
     * @return an unmodifiable list containing all elements
     */
    public List<Element> elements() {
        return elements.values()
                .stream()
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    /**
     * Retrieves the name of this communication entity.
     *
     * @return the name of this communication entity
     */
    public String name() {
        return name;
    }

    /**
     * Removes the element with the specified name from this communication entity.
     *
     * @param name the name of the element to be removed
     * @return {@code true} if an element was removed, {@code false} otherwise
     * @throws NullPointerException if the specified name is {@code null}
     */
    public boolean remove(String name) {
        requireNonNull(name, "name is required");
        return elements.remove(name) != null;
    }

    /**
     * Finds and retrieves the element with the specified name from this communication entity.
     *
     * @param columnName the name of the element to find
     * @return an {@link Optional} containing the element, or empty if no such element exists
     * @throws NullPointerException if the specified name is {@code null}
     */
    public Optional<Element> find(String columnName) {
        requireNonNull(columnName, "columnName is required");
        Element element = elements.get(columnName);
        return ofNullable(element);
    }

    /**
     * Finds and retrieves the element with the specified name from this communication entity,
     * converting its value to the specified type.
     *
     * @param <T>          the type of the value to retrieve
     * @param elementName  the name of the element to find
     * @param type         the type to convert the value to
     * @return an {@link Optional} containing the converted value, or empty if no such element exists
     * @throws NullPointerException if either the specified name or type is {@code null}
     */
    public <T> Optional<T> find(String elementName, Class<T> type) {
        requireNonNull(elementName, "elementName is required");
        requireNonNull(type, "type is required");
        return ofNullable(elements.get(elementName))
                .map(c -> c.get(type));
    }

    /**
     * Finds and retrieves the element with the specified name from this communication entity,
     * converting its value to the specified type using a {@link TypeSupplier}.
     *
     * @param <T>          the type of the value to retrieve
     * @param elementName  the name of the element to find
     * @param type         the type supplier to convert the value to
     * @return an {@link Optional} containing the converted value, or empty if no such element exists
     * @throws NullPointerException if either the specified name or type supplier is {@code null}
     */
    public <T> Optional<T> find(String elementName, TypeSupplier<T> type) {
        requireNonNull(elementName, "elementName is required");
        requireNonNull(type, "type is required");
        return ofNullable(elements.get(elementName))
                .map(v -> v.get(type));
    }

    /**
     * Returns the number of elements contained in this communication entity.
     *
     * @return the number of elements in this communication entity
     */
    public int size() {
        return elements.size();
    }

    /**
     * Checks whether this communication entity contains any elements.
     *
     * @return {@code true} if this communication entity contains no elements, {@code false} otherwise
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Creates a copy of this communication entity.
     *
     * @return a new instance of CommunicationEntity with the same elements and name
     */
    public CommunicationEntity copy() {
        CommunicationEntity entity = new CommunicationEntity(this.name);
        entity.elements.putAll(new HashMap<>(this.elements));
        return entity;
    }

    /**
     * Returns a set containing the names of all elements in this communication entity.
     *
     * @return an unmodifiable set containing the names of all elements
     */
    public Set<String> elementNames() {
        return unmodifiableSet(elements.keySet());
    }

    /**
     * Returns a collection containing the values of all elements in this communication entity.
     *
     * @return an unmodifiable collection containing the values of all elements
     */
    public Collection<Value> values() {
        return elements
                .values()
                .stream()
                .map(Element::value)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    /**
     * Checks whether this communication entity contains an element with the specified name.
     *
     * @param name the name of the element to check
     * @return {@code true} if an element with the specified name exists, {@code false} otherwise
     * @throws NullPointerException if the specified name is {@code null}
     */
    public boolean contains(String name) {
        requireNonNull(name, "name is required");
        return elements.containsKey(name);
    }

    /**
     * Removes all elements from this communication entity.
     */
    public void clear() {
        elements.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommunicationEntity that = (CommunicationEntity) o;
        return Objects.equals(elements, that.elements) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, name);
    }

    @Override
    public String toString() {
        return "DefaultColumnEntity{" + "columns=" + toMap() +
                ", name='" + name + '\'' +
                '}';
    }


    /**
     * Creates a new instance of CommunicationEntity with the specified name.
     *
     * @param name the name of the communication entity
     * @return a new CommunicationEntity instance
     * @throws NullPointerException if the provided name is {@code null}
     */
    public static CommunicationEntity of(String name) {
        return new CommunicationEntity(requireNonNull(name, "name is required"));
    }

    /**
     * Creates a new instance of CommunicationEntity with the specified name and elements.
     *
     * @param name     the name of the communication entity
     * @param elements a list of elements to be added to the communication entity
     * @return a new CommunicationEntity instance with the specified name and elements
     * @throws NullPointerException if the provided name is {@code null}
     */
    public static CommunicationEntity of(String name, List<Element> elements) {
        CommunicationEntity communicationEntity = new CommunicationEntity(name);
        communicationEntity.addAll(elements);
        return communicationEntity;
    }

    private Object convert(Object value) {
        if (value instanceof Element element) {
            return Collections.singletonMap(element.name(), convert(element.get()));
        } else if (value instanceof Iterable<?> iterable) {
            List<Object> elements = new ArrayList<>();
            iterable.forEach(e -> elements.add(convert(e)));
            return elements;
        }
        return value;
    }

}
