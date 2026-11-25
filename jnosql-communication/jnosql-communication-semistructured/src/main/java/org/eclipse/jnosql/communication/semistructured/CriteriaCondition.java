/*
 *  Copyright (c) 2024,2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 * and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;


import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.QueryException;
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
    private final CriteriaOperand left;
    private final CriteriaOperand right;

    private CriteriaCondition(Element element, Condition condition) {
        this(element, condition, false);
    }

    private CriteriaCondition(Element element, Condition condition, boolean readOnly) {
        this.element = element;
        this.condition = condition;
        this.readOnly = readOnly;
        this.left = null;
        this.right = null;
    }

    private CriteriaCondition(CriteriaOperand left, Condition condition, CriteriaOperand right, boolean readOnly) {
        this.element = null;
        this.condition = condition;
        this.readOnly = readOnly;
        this.left = left;
        this.right = right;
    }

    /**
     * Retrieves the element used in the condition.
     *
     * @return the element
     * @throws UnsupportedOperationException when the predicate has a native function operand: such a predicate is
     *                                       not an attribute-value pair, so it is never exposed through this legacy
     *                                       contract, which prevents drivers that cannot translate functions from
     *                                       silently dropping or weakening it
     */
    public Element element() {
        if (isNative()) {
            throw new UnsupportedOperationException("This condition has a native function operand ("
                    + left + " " + condition + " " + right + ") that cannot be exposed as an attribute-value element."
                    + " Read it through leftOperand() and rightOperand(), or reject the query.");
        }
        return element;
    }

    /**
     * Returns the left operand of a predicate. For an ordinary predicate this is the {@link CriteriaOperand.Attribute}
     * named by {@link #element()}; for a native function predicate it is the attribute or
     * {@link CriteriaOperand.Function} given at construction. The result is derived from the canonical state, so it
     * is always consistent with {@link #element()} when that is available.
     *
     * @return the left operand
     * @throws UnsupportedOperationException when this is a logical condition (AND, OR, NOT, IGNORE_CASE),
     *                                       which has nested conditions rather than operands
     */
    public CriteriaOperand leftOperand() {
        requirePredicate();
        return isNative() ? left : new CriteriaOperand.Attribute(element.name());
    }

    /**
     * Returns the right operand of a predicate. For an ordinary predicate this is the
     * {@link CriteriaOperand.Constant} wrapping {@code element().value()}.
     *
     * @return the right operand
     * @throws UnsupportedOperationException when this is a logical condition
     */
    public CriteriaOperand rightOperand() {
        requirePredicate();
        return isNative() ? right : new CriteriaOperand.Constant(element.value());
    }

    /**
     * Indicates whether this predicate has a {@link CriteriaOperand.Function} operand and therefore cannot be
     * consumed through the legacy {@link #element()} contract. Logical conditions are never native themselves;
     * inspect their nested conditions.
     *
     * @return {@code true} when {@link #element()} would fail because of a native function operand
     */
    public boolean isNative() {
        return left != null;
    }

    private void requirePredicate() {
        switch (condition) {
            case AND, OR, NOT, IGNORE_CASE -> throw new UnsupportedOperationException(
                    "A " + condition + " condition has nested conditions, not operands");
            default -> {
            }
        }
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
                && condition == that.condition
                && Objects.equals(left, that.left)
                && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element, condition, readOnly, left, right);
    }

    @Override
    public String toString() {
        if (isNative()) {
            return "CriteriaCondition{left=" + left + ", condition=" + condition + ", right=" + right + '}';
        }
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
        if (condition.isNative()) {
            return new CriteriaCondition(condition.left, condition.condition, condition.right, true);
        }
        return new CriteriaCondition(condition.element(), condition.condition(), true);
    }


    /**
     * Creates a new {@link CriteriaCondition} with the specified element and condition.
     *
     * @param element   the element representing the data to match
     * @param condition the condition to apply
     * @return a new {@link CriteriaCondition}
     * @throws NullPointerException when the element or condition is null
     */
    public static CriteriaCondition of(Element element, Condition condition) {
        return new CriteriaCondition(Objects.requireNonNull(element, "Column is required"), condition);
    }


    /**
     * Creates a predicate from two operands, the single operand-oriented way to build a predicate with a
     * {@link CriteriaOperand.Function}. Attribute and value operands give the same condition as the element-based
     * factories, for example {@code of(new Attribute("name"), EQUALS, new Constant(Value.of("OTAVIO")))} equals
     * {@code eq("name", "OTAVIO")}. Examples:
     * <pre>{@code
     * // WHERE UPPER(name) = 'OTAVIO'
     * var upper = NativeFunction.of("UPPER", new FunctionArgument.AttributePath("name"));
     * CriteriaCondition.of(new CriteriaOperand.Function(upper), Condition.EQUALS,
     *         new CriteriaOperand.Constant(Value.of("OTAVIO")));
     *
     * // WHERE name = UPPER(otherName)
     * CriteriaCondition.of(new CriteriaOperand.Attribute("name"), Condition.EQUALS,
     *         new CriteriaOperand.Function(NativeFunction.of("UPPER", new FunctionArgument.AttributePath("otherName"))));
     * }</pre>
     * Supported conditions are EQUALS, GREATER_THAN, GREATER_EQUALS_THAN, LESSER_THAN and LESSER_EQUALS_THAN with any
     * attribute, value or function operands, and LIKE, IN and BETWEEN with an attribute or function on the left and a
     * value on the right. A value cannot be the left operand, and a predicate with no function must be an attribute
     * compared with a value (field-to-field comparison is not supported). Negation (NOT EQUALS, IS NULL and so on)
     * is expressed with {@link #negate()} or an equality with a null value, as for ordinary predicates.
     * This only represents the predicate; executing a native function is the responsibility of the database driver.
     *
     * @param left      the left operand
     * @param condition the comparison condition
     * @param right     the right operand
     * @return the predicate
     * @throws NullPointerException if any argument is null
     * @throws QueryException       if the combination of condition and operands is not supported
     */
    public static CriteriaCondition of(CriteriaOperand left, Condition condition, CriteriaOperand right) {
        Objects.requireNonNull(left, "left is required");
        Objects.requireNonNull(condition, "condition is required");
        Objects.requireNonNull(right, "right is required");
        if (left instanceof CriteriaOperand.Attribute attribute && right instanceof CriteriaOperand.Constant constant) {
            return fromElement(Element.of(attribute.name(), constant.value()), condition);
        }
        if (left instanceof CriteriaOperand.Constant) {
            throw new QueryException("A value cannot be the left operand of " + condition + ": " + left);
        }
        if (!(left instanceof CriteriaOperand.Function || right instanceof CriteriaOperand.Function)) {
            throw new QueryException("Comparing " + left + " with " + right + " is not supported: "
                    + "at least one operand must be a native function");
        }
        switch (condition) {
            case EQUALS, GREATER_THAN, GREATER_EQUALS_THAN, LESSER_THAN, LESSER_EQUALS_THAN -> {
            }
            case LIKE, IN, BETWEEN -> {
                if (!(right instanceof CriteriaOperand.Constant constant)) {
                    throw new QueryException("A native function predicate " + condition
                            + " requires a value as the right operand: " + right);
                }
                if (condition == Condition.IN) {
                    checkInClause(constant.value());
                } else if (condition == Condition.BETWEEN) {
                    checkBetweenClause(constant.value().get());
                }
            }
            default -> throw new QueryException("The condition " + condition
                    + " is not supported with a native function operand: " + left + " " + condition + " " + right);
        }
        return new CriteriaCondition(left, condition, right, false);
    }

    private static CriteriaCondition fromElement(Element element, Condition condition) {
        return switch (condition) {
            case IN -> in(element);
            case BETWEEN -> between(element);
            case AND, OR, NOT, IGNORE_CASE -> throw new QueryException("The logical condition " + condition
                    + " cannot be built from operands");
            default -> new CriteriaCondition(element, condition);
        };
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

    private static void checkInClause(Value value) {
        if (!value.isInstanceOf(Iterable.class)) {
            throw new IllegalArgumentException("On CriteriaCondition#in, you must use an iterable" +
                    " instead of class: " + value.getClass().getName());
        }
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
     * Creates a {@link CriteriaCondition} with a {@link Condition#STARTS_WITH}, indicating that a select will scan a
     * semistructured NoSQL database with the same name and the value starts with the one provided in this element.
     *
     * @param element an element instance
     * @return a {@link CriteriaCondition} with {@link Condition#STARTS_WITH}
     * @throws NullPointerException when the element is null
     */
    public static CriteriaCondition startsWith(Element element) {
        return new CriteriaCondition(element, Condition.STARTS_WITH);
    }

    /**
     * Creates a {@link CriteriaCondition} with a {@link Condition#ENDS_WITH}, indicating that a select will scan a
     * semistructured NoSQL database with the same name and the value ends with the one provided in this element.
     *
     * @param element an element instance
     * @return a {@link CriteriaCondition} with {@link Condition#ENDS_WITH}
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
     * @param condition an element instance
     * @return a {@link CriteriaCondition} with {@link Condition#IGNORE_CASE}
     * @throws NullPointerException when the element is null
     */
    public static CriteriaCondition ignoreCase(CriteriaCondition condition) {
        Element element = Element.of(Condition.IGNORE_CASE.getNameField(), condition);
        return of(element, Condition.IGNORE_CASE);
    }

    private static void checkBetweenClause(Object value) {
        if (value instanceof Iterable<?> iterable) {

            long count = StreamSupport.stream(iterable.spliterator(), false).count();
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
