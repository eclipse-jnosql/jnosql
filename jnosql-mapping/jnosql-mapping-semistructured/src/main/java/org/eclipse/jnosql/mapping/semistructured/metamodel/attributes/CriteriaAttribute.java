/*
 *  Copyright (c) 2024 Otávio Santana and others
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
package org.eclipse.jnosql.mapping.semistructured.metamodel.attributes;

import jakarta.data.metamodel.SortableAttribute;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;

/**
 * Represents a criteria-capable entity attribute in the {@link jakarta.data.metamodel.StaticMetamodel}.
 * This interface extends {@link SortableAttribute} to include sorting capabilities
 * and introduces methods to generate various comparison-based criteria conditions.
 * <p>
 * The {@code CriteriaAttribute} interface is designed to facilitate the construction
 * of query conditions based on comparisons such as equality, greater than/less than checks,
 * and range checks (between). This enables the creation of dynamic and type-safe queries
 * for entity attributes.
 * </p>
 *
 * @param <T> the type of the entity attribute this criteria attribute represents,
 *            ensuring type safety in comparison operations.
 */
public interface CriteriaAttribute<T> extends SortableAttribute<T> {

    /**
     * Generates a criteria condition for an equality comparison on the attribute.
     *
     * @param value The value to compare the attribute against for equality.
     * @return A {@link CriteriaCondition} representing the equality comparison condition.
     * @throws NullPointerException if the value is null
     */
    CriteriaCondition eq(Object value);

    /**
     * Generates a criteria condition for a greater than comparison on the attribute.
     *
     * @param value The value to compare the attribute against for being greater than.
     * @return A {@link CriteriaCondition} representing the greater than comparison condition.
     * @throws NullPointerException if the value is null
     */
    CriteriaCondition gt(Object value);

    /**
     * Generates a criteria condition for a greater than or equal to comparison on the attribute.
     *
     * @param value The value to compare the attribute against for being greater than or equal to.
     * @return A {@link CriteriaCondition} representing the greater than or equal to comparison condition.
     * @throws NullPointerException if the value is null
     */
    CriteriaCondition gte(Object value);

    /**
     * Generates a criteria condition for a less than comparison on the attribute.
     *
     * @param value The value to compare the attribute against for being less than.
     * @return A {@link CriteriaCondition} representing the less than comparison condition.
     * @throws NullPointerException if the value is null
     */
    CriteriaCondition lt(Object value);

    /**
     * Generates a criteria condition for a less than or equal to comparison on the attribute.
     *
     * @param value The value to compare the attribute against for being less than or equal to.
     * @return A {@link CriteriaCondition} representing the less than or equal to comparison condition.
     * @throws NullPointerException if the value is null
     */
    CriteriaCondition lte(Object value);

    /**
     * Generates a criteria condition for a between comparison on the attribute.
     * This checks if the attribute's value falls within the specified range, inclusive.
     *
     * @param valueA The lower bound of the range.
     * @param valueB The upper bound of the range.
     * @return A {@link CriteriaCondition} representing the between comparison condition.
     * @throws NullPointerException if either value is null
     */
    CriteriaCondition between(Object valueA, Object valueB);

}