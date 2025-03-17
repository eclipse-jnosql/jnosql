/*
 *
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
 *
 */
package org.eclipse.jnosql.communication;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents conditions that can be used in a query.
 * These conditions are used to filter query results based on specific criteria.
 * The conditions include equality, comparison operators, set membership, pattern matching, logical conjunctions,
 * logical disjunctions, negation, and range checks.
 */
public enum Condition {
    /**
     * Represents an equality comparison condition.
     */
    EQUALS,
    /**
     * Represents a greater than comparison condition.
     */
    GREATER_THAN,
    /**
     * Represents a greater than or equals to comparison condition.
     */
    GREATER_EQUALS_THAN,
    /**
     * Represents a lesser than comparison condition.
     */
    LESSER_THAN,
    /**
     * Represents a lesser than or equals to comparison condition.
     */
    LESSER_EQUALS_THAN,
    /**
     * Represents a set membership comparison condition.
     */
    IN,
    /**
     * Represents a pattern matching comparison condition.
     */
    LIKE,
    /**
     * Represents a text contains another text condition.
     */
    CONTAINS,
    /**
     * Represents a text contains another text at the end condition.
     */
    ENDS_WITH,
    /**
     * Represents a logical conjunction condition.
     */
    AND,
    /**
     * Represents a logical disjunction condition.
     */
    OR,
    /**
     * Represents a negation condition.
     */
    NOT,
    /**
     * Represents a condition with ignore case flag.
     */
    IGNORE_CASE,
    /**
     * Represents a range check condition.
     */
    BETWEEN;

    /**
     * Return tne field as name to both document and column.
     * The goal is to be a reserved word.
     * The formula is: underscore plus the {@link Enum#name()}
     * So, call this method on {@link Condition#EQUALS}  will return "_EQUALS"
     *
     * @return the keyword to condition
     */
    public String getNameField() {
        return '_' + this.name();
    }

    /**
     * Retrieve the condition from {@link Condition#getNameField()} on case-sensitive
     *
     * @param condition the condition converted to field
     * @return the condition instance
     * @throws NullPointerException     when condition is null
     * @throws IllegalArgumentException when the condition is not found
     */
    public static Condition parse(String condition) {
        Objects.requireNonNull(condition, "condition is required");
        return Arrays.stream(values())
                .filter(c -> c.getNameField()
                        .equals(condition)).findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(String.format("The condition %s is not found", condition)));
    }
}
