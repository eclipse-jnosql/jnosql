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
 * Represents a boolean entity attribute in the {@link jakarta.data.metamodel.StaticMetamodel}.
 * This interface extends {@link SortableAttribute} to include sorting capabilities
 * for boolean attributes and provides methods to generate query conditions
 * based on the true or false state of the attribute.
 * <p>
 * Boolean attributes are commonly used in filtering queries to include or exclude
 * records based on a true/false condition. The methods {@code isTrue()} and
 * {@code isFalse()} allow for the creation of such conditions in a type-safe and
 * fluent manner.
 * </p>
 *
 * @param <T> the entity class of the static metamodel that the attribute belongs to.
 */
public interface BooleanAttribute<T> extends SortableAttribute<T> {


    /**
     * Generates a criteria condition representing a query where the boolean
     * attribute is true.
     * <p>
     * This method can be used in constructing queries to filter entities
     * based on the attribute being true.
     * </p>
     *
     * @return A {@link CriteriaCondition} representing the condition where
     * the boolean attribute is true.
     */
    CriteriaCondition isTrue();

    /**
     * Generates a criteria condition representing a query where the boolean
     * attribute is false.
     * <p>
     * This method can be used in constructing queries to filter entities
     * based on the attribute being false.
     * </p>
     *
     * @return A {@link CriteriaCondition} representing the condition where
     * the boolean attribute is false.
     */
    CriteriaCondition isFalse();

}