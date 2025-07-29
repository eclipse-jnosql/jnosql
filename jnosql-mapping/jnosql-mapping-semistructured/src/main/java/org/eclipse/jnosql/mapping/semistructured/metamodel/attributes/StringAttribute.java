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

import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;

/**
 * Represents a string-based entity attribute in the {@link jakarta.data.metamodel.StaticMetamodel}.
 * This interface extends {@link CriteriaAttribute} to include comparison
 * operations specific to string attributes and introduces a method to generate
 * criteria conditions for string pattern matching using the "LIKE" operation.
 * <p>
 * The {@code StringAttribute} interface is tailored for constructing dynamic,
 * type-safe queries involving string attributes, facilitating easy construction
 * of conditions for partial string matching. This enhances the capability of the
 * criteria API by allowing the specification of sophisticated string-based filters.
 * </p>
 *
 * @param <T> the type of the entity that the string attribute belongs to,
 *            ensuring type safety in pattern matching operations.
 */
public interface StringAttribute<T> extends CriteriaAttribute<T> {

    /**
     * Generates a criteria condition for a "LIKE" pattern matching comparison
     * on the string attribute.
     * <p>
     * This method is used for constructing queries that filter entities based on
     * whether their string attribute matches a specified pattern. The pattern can
     * include like wildcards, such as '%' for zero or more characters and '_'
     * for a single character, enabling flexible and powerful search capabilities.
     * </p>
     *
     * @param value The pattern to compare the attribute against for matching.
     * @return A {@link CriteriaCondition} representing the "LIKE" pattern matching condition.
     */
    CriteriaCondition like(String value);
}