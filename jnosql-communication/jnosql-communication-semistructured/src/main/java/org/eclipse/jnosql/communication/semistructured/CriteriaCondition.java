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


import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;


/**
 * Represents a condition in a query, including an element and a condition type, forming a predicate.
 * This class is used in conjunction with {@link DatabaseManager#select(SelectQuery)}.
 * It encapsulates a condition for filtering data in a semi-structured NoSQL database.
 *
 * @see Condition
 * @see DatabaseManager#select(SelectQuery)
 */
public final class CriteriaCondition {

    private final Element element;
    private final Condition condition;
    private final boolean readOnly;

    private CriteriaCondition(Element element, Condition condition) {
        this.element = element;
        this.condition = condition;
        this.readOnly = false;
    }

    private CriteriaCondition(Element element, Condition condition, boolean readOnly) {
        this.element = element;
        this.condition = condition;
        this.readOnly = readOnly;
    }

    /**
     * Retrieves the element used in the condition.
     *
     * @return the element
     */
    public Element element() {
        return element;
    }

    /**
     * Retrieves the condition type used in the condition.
     *
     * @return the condition
     * @see Condition
     */
    public Condition condition() {
        return condition;
    }

    /**
     * Creates a new {@link CriteriaCondition} with the {@link Condition#AND} conjunction.
     *
     * @param condition the condition to be combined with
     * @return the combined condition
     * @throws NullPointerException when the condition is null
     */
    public CriteriaCondition and(CriteriaCondition condition) {
        validateReadOnly();
        Objects.requireNonNull(condition, "Conditions are required");
        if (Condition.AND.equals(this.condition)) {
            Element newElement = getConditions(condition, Condition.AND);
            return new CriteriaCondition(newElement, Condition.AND);
        }
        return and(this, condition);
    }

    /**
     * Creates a new {@link CriteriaCondition} by negating the current one.
     *
     * @return the negated condition
     * @see Condition#NOT
     */
    public CriteriaCondition negate() {
        validateReadOnly();
        if (Condition.NOT.equals(this.condition)) {
            return this.element.get(CriteriaCondition.class);
        } else {
            Element newElement = Element.of(Condition.NOT.getNameField(), this);
            return new CriteriaCondition(newElement, Condition.NOT);
        }

    }

    /**
     * Creates a new {@link CriteriaCondition} with the {@link Condition#OR} conjunction.
     *
     * @param condition the condition to be combined with
     * @return the combined condition
     * @throws NullPointerException when the condition is null
     */
    public CriteriaCondition or(CriteriaCondition condition) {
        validateReadOnly();
        Objects.requireNonNull(condition, "Condition is required");
        if (Condition.OR.equals(this.condition)) {
            Element newElement = getConditions(condition, Condition.OR);
            return new CriteriaCondition(newElement, Condition.OR);
        }
        return or(this, condition);
    }

    private Element getConditions(CriteriaCondition criteriaCondition, Condition condition) {
        var conditions = new ArrayList<>(element.get(new TypeReference<List<CriteriaCondition>>() {
        }));
        conditions.add(criteriaCondition);
        return Element.of(condition.getNameField(), conditions);
    }

    private void validateReadOnly() {
        if (readOnly) {
            throw new IllegalStateException("You cannot change the status after building the query");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CriteriaCondition that = (CriteriaCondition) o;
        return Objects.equals(element, that.element)
                && condition == that.condition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(element, condition, readOnly);
    }

    @Override
    public String toString() {
        return "CriteriaCondition{" +
                "element=" + element +
                ", condition=" + condition +
                '}';
    }

    /**
     * Creates a new read-only {@link CriteriaCondition} based on the provided condition.
     *
     * @param condition the condition to be marked as read-only
     * @return a new read-only condition
     * @throws NullPointerException when the condition is null
     */
    public static CriteriaCondition readOnly(CriteriaCondition condition) {
        Objects.requireNonNull(condition, "Condition is required");
        return new CriteriaCondition(condition.element(), condition.condition(), true);
    }

    static CriteriaCondition of(Element element, Condition condition) {
        return new CriteriaCondition(Objects.requireNonNull(element, "Column is required"), condition);
    }


    /**
     * Creates a new {@link CriteriaCondition} with a {@link Condition#EQUALS} condition.
     * This indicates that a select operation will scan data with the same element name and an equal value to the provided value.
     *
     * @param element the element representing the data to match
     * @return a {@link CriteriaCondition} with {@link Condition#EQUALS}
     * @throws NullPointerException when the element is null
     */
    public static CriteriaCondition eq(Element element) {
        return new CriteriaCondition(element, Condition.EQUALS);
    }

    /**
     * An alias method to {@link CriteriaCondition#eq(Element)}, which first creates an {@link Element}
     * instance and then applies the condition.
     *
     * @param name  the name of the element
     * @param value the value of the element
     * @return a {@link CriteriaCondition} with {@link Condition#EQUALS}
     * @throws NullPointerException when either the name or the value is null
     */
    public static CriteriaCondition eq(String name, Object value) {
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(value, "Value is required");
        return eq(Element.of(name, value));
    }

    /**
     * Creates a new {@link CriteriaCondition} with a {@link Condition#GREATER_THAN} condition.
     * This indicates that a select operation will scan data with the same element name and a value greater than the provided value.
     *
     * @param element the element representing the data to match
     * @return a {@link CriteriaCondition} with {@link Condition#GREATER_THAN}
     * @throws NullPointerException when the element is null
     */
    public static CriteriaCondition gt(Element element) {
        return new CriteriaCondition(element, Condition.GREATER_THAN);
    }

    /**
     * An alias method to {@link CriteriaCondition#gt(Element)}, which first creates an {@link Element}
     * instance and then applies the condition.
     *
     * @param name  the name of the element
     * @param value the value of the element
     * @return a {@link CriteriaCondition} with {@link Condition#GREATER_THAN}
     * @throws NullPointerException when either the name or the value is null
     */
    public static CriteriaCondition gt(String name, Object value) {
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(value, "Value is required");
        return gt(Element.of(name, value));
    }

    /**
     * Creates a new {@link CriteriaCondition} with a {@link Condition#GREATER_EQUALS_THAN} condition.
     * This indicates that a select operation will scan data with the same element name and a value greater than or equal to the provided value.
     *
     * @param element the element representing the data to match
     * @return a {@link CriteriaCondition} with {@link Condition#GREATER_EQUALS_THAN}
     * @throws NullPointerException when the element is null
     */
    public static CriteriaCondition gte(Element element) {
        return new CriteriaCondition(element, Condition.GREATER_EQUALS_THAN);
    }

    /**
     * An alias method to {@link CriteriaCondition#gte(Element)}, which first creates an {@link Element}
     * instance and then applies the condition.
     *
     * @param name  the name of the element
     * @param value the value of the element
     * @return a {@link CriteriaCondition} with {@link Condition#GREATER_EQUALS_THAN}
     * @throws NullPointerException when either the name or the value is null
     */
    public static CriteriaCondition gte(String name, Object value) {
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(value, "Value is required");
        return gte(Element.of(name, value));
    }

    /**
     * Creates a new {@link CriteriaCondition} with a {@link Condition#LESSER_THAN} condition.
     * This indicates that a select operation will scan data with the same element name and a value lesser than the provided value.
     *
     * @param element the element representing the data to match
     * @return a {@link CriteriaCondition} with {@link Condition#LESSER_THAN}
     * @throws NullPointerException when the element is null
     */
    public static CriteriaCondition lt(Element element) {
        return new CriteriaCondition(element, Condition.LESSER_THAN);
    }

    /**
     * An alias method to {@link CriteriaCondition#lt(Element)}, which first creates an {@link Element}
     * instance and then applies the condition.
     *
     * @param name  the name of the element
     * @param value the value of the element
     * @return a {@link CriteriaCondition} with {@link Condition#LESSER_THAN}
     * @throws NullPointerException when either the name or the value is null
     */
    public static CriteriaCondition lt(String name, Object value) {
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(value, "Value is required");
        return lt(Element.of(name, value));
    }

    /**
     * Creates a new {@link CriteriaCondition} with a {@link Condition#LESSER_EQUALS_THAN} condition.
     * This indicates that a select operation will scan data with the same element name and a value lesser than or equal to the provided value.
     *
     * @param element the element representing the data to match
     * @return a {@link CriteriaCondition} with {@link Condition#LESSER_EQUALS_THAN}
     * @throws NullPointerException when the element is null
     */
    public static CriteriaCondition lte(Element element) {
        return new CriteriaCondition(element, Condition.LESSER_EQUALS_THAN);
    }

    /**
     * An alias method to {@link CriteriaCondition#lte(Element)}, which first creates an {@link Element}
     * instance and then applies the condition.
     *
     * @param name  the name of the element
     * @param value the value of the element
     * @return a {@link CriteriaCondition} with {@link Condition#LESSER_EQUALS_THAN}
     * @throws NullPointerException when either the name or the value is null
     */
    public static CriteriaCondition lte(String name, Object value) {
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(value, "Value is required");
        return lte(Element.of(name, value));
    }

    /**
     * Creates a {@link CriteriaCondition} with a {@link Condition#IN}, indicating that a select will scan a semistructured NoSQL
     * database with the same name and the value within the provided collection.
     *
     * @param element an element instance
     * @return a {@link CriteriaCondition} with {@link Condition#IN}
     * @throws NullPointerException     when the element is null
     * @throws IllegalArgumentException when the {@link Element#get()} is not an iterable implementation
     */
    public static CriteriaCondition in(Element element) {
        Objects.requireNonNull(element, "element is required");
        checkInClause(element.value());
        return new CriteriaCondition(element, Condition.IN);
    }

    /**
     * An alias method to {@link CriteriaCondition#in(Element)}, which first creates an {@link Element}
     * instance and then applies the condition.
     *
     * @param name  the name of the element
     * @param value the element information
     * @return a {@link CriteriaCondition} with {@link Condition#IN}
     * @throws NullPointerException when either the name or the value is null
     */
    public static CriteriaCondition in(String name, Object value) {
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(value, "Value is required");
        return in(Element.of(name, value));
    }

    /**
     * Creates a {@link CriteriaCondition} with a {@link Condition#LIKE}, indicating that a select will scan a
     * semistructured NoSQL database with the same name and the value is like the one provided in this element.
     *
     * @param element an element instance
     * @return a {@link CriteriaCondition} with {@link Condition#LIKE}
     * @throws NullPointerException when the element is null
     */
    public static CriteriaCondition like(Element element) {
        return new CriteriaCondition(element, Condition.LIKE);
    }

    /**
     * An alias method to {@link CriteriaCondition#like(Element)}, which first creates an {@link Element}
     * instance and then applies the condition.
     *
     * @param name  the name of the element
     * @param value the element information
     * @return a {@link CriteriaCondition} with {@link Condition#LIKE}
     * @throws NullPointerException when either the name or the value is null
     */
    public static CriteriaCondition like(String name, Object value) {
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(value, "Value is required");
        return like(Element.of(name, value));
    }

    /**
     * Creates a new {@link CriteriaCondition} with a {@link Condition#BETWEEN} condition.
     * This indicates that a select operation will scan data with the same element name and a value between the two provided values.
     * The element must contain an iterable with exactly two elements.
     *
     * @param element the element representing the data to match
     * @return a {@link CriteriaCondition} with {@link Condition#BETWEEN}
     * @throws NullPointerException when the element is null
     * @throws IllegalArgumentException when the element is not an iterable with two elements
     */
    public static CriteriaCondition between(Element element) {
        Objects.requireNonNull(element, "element is required");
        checkBetweenClause(element.get());
        return new CriteriaCondition(element, Condition.BETWEEN);
    }

    /**
     * An alias method to {@link CriteriaCondition#between(Element)}, which first creates an {@link Element}
     * instance and then applies the condition.
     *
     * @param name  the name of the element
     * @param value the value of the element
     * @return a {@link CriteriaCondition} with {@link Condition#BETWEEN}
     * @throws NullPointerException when either the name or the value is null
     */
    public static CriteriaCondition between(String name, Object value) {
        Objects.requireNonNull(name, "Name is required");
        Objects.requireNonNull(value, "Value is required");
        return between(Element.of(name, value));
    }

    /**
     * Returns a condition that is the negation of the supplied condition.
     * This is accomplished by returning the result of calling {@code target.negate()}.
     *
     * @param condition the condition
     * @return a condition that negates the results of the supplied condition
     * @throws NullPointerException when the condition is null
     */
    public static CriteriaCondition not(CriteriaCondition condition) {
        Objects.requireNonNull(condition, "Condition is required");
        return condition.negate();
    }

    /**
     * Returns a new {@link CriteriaCondition} aggregating all the conditions as just one condition with "AND".
     * The {@link Element} will store the {@link Condition#getNameField()} as the key and the value will be
     * the {@link java.util.List} of all conditions.
     * <p>Example:</p>
     * <pre>{@code
     * Element age = Element.of("age", 26);
     * Element name = Element.of("name", "otavio");
     * CriteriaCondition condition = CriteriaCondition.eq(name).and(CriteriaCondition.gte(age));
     * }</pre>
     * The {@link CriteriaCondition#element()} will have "_AND" as the key and the list of conditions as the value.
     *
     * @param conditions the conditions to be aggregated
     * @return the new {@link CriteriaCondition} instance
     * @throws NullPointerException when the conditions are null
     */
    public static CriteriaCondition and(CriteriaCondition... conditions) {
        Objects.requireNonNull(conditions, "Condition is required");
        Element element = Element.of(Condition.AND.getNameField(), Arrays.asList(conditions));
        return of(element, Condition.AND);
    }

    /**
     * Returns a new {@link CriteriaCondition} aggregating all the conditions as just one condition with "OR".
     * The {@link Element} will store the {@link Condition#getNameField()} as the key and the value will be
     * the {@link java.util.List} of all conditions.
     * <p>Example:</p>
     * <pre>{@code
     * Element age = Element.of("age", 26);
     * Element name = Element.of("name", "otavio");
     * CriteriaCondition condition = CriteriaCondition.eq(name).or(CriteriaCondition.gte(age));
     * }</pre>
     * The {@link CriteriaCondition#element()} will have "_OR" as the key and the list of conditions as the value.
     *
     * @param conditions the conditions to be aggregated
     * @return the new {@link CriteriaCondition} instance
     * @throws NullPointerException when the condition is null
     */
    public static CriteriaCondition or(CriteriaCondition... conditions) {
        Objects.requireNonNull(conditions, "Condition is required");
        Element element = Element.of(Condition.OR.getNameField(), Arrays.asList(conditions));
        return of(element, Condition.OR);
    }

    /**
     * Creates a {@link CriteriaCondition} with a {@link Condition#CONTAINS}, indicating that a select will scan a
     * semistructured NoSQL database with the same name and the value contains the one provided in this element.
     *
     * @param element an element instance
     * @return a {@link CriteriaCondition} with {@link Condition#CONTAINS}
     * @throws NullPointerException when the element is null
     */
    public static CriteriaCondition contains(Element element) {
        return new CriteriaCondition(element, Condition.CONTAINS);
    }

    /**
     * Creates a {@link CriteriaCondition} with a {@link Condition#ENDS_WITH}, indicating that a select will scan a
     * semistructured NoSQL database with the same name and the value ends with the one provided in this element.
     *
     * @param element an element instance
     * @return a {@link CriteriaCondition} with {@link Condition#CONTAINS}
     * @throws NullPointerException when the element is null
     */
    public static CriteriaCondition endsWith(Element element) {
        return new CriteriaCondition(element, Condition.ENDS_WITH);
    }

    /**
     * Creates a {@link CriteriaCondition} with a {@link Condition#IGNORE_CASE}, indicating that a select will
     * scan a semistructured NoSQL database with the same name and the value matches the underlying condition
     * ignoring the case.
     *
     * @param element an element instance
     * @return a {@link CriteriaCondition} with {@link Condition#IGNORE_CASE}
     * @throws NullPointerException when the element is null
     */
    public static CriteriaCondition ignoreCase(CriteriaCondition condition) {
        Element element = Element.of(Condition.IGNORE_CASE.getNameField(), condition);
        return of(element, Condition.IGNORE_CASE);
    }

    private static void checkInClause(Value value) {
        if (!value.isInstanceOf(Iterable.class)) {
            throw new IllegalArgumentException("On CriteriaCondition#in, you must use an iterable" +
                    " instead of class: " + value.getClass().getName());
        }
    }

    private static void checkBetweenClause(Object value) {
        if (Iterable.class.isInstance(value)) {

            long count = StreamSupport.stream(Iterable.class.cast(value).spliterator(), false).count();
            if (count != 2) {
                throw new IllegalArgumentException("On CriteriaCondition#between, you must use an iterable" +
                        " with two elements");
            }

        } else {
            throw new IllegalArgumentException("On CriteriaCondition#between, you must use an iterable" +
                    " with two elements instead of class: " + value.getClass().getName());
        }
    }
}
