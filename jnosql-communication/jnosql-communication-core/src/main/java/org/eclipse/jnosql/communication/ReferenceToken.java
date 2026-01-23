/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication;

import java.util.Objects;


/**
 * Represents a provider-defined reference token used to identify
 * an addressable element within a database structure.
 *
 * <p>The semantic meaning of a {@code ReferenceToken} is entirely
 * provider-defined. It may refer to a document field, column,
 * property, or any other addressable element supported by the
 * underlying database.</p>
 *
 * <p>This type is an opaque value object. The specification does
 * not impose validation rules or structural guarantees.</p>
 *
 * @param value provider-defined reference expression
 */
public record ReferenceToken(String value) {

    public ReferenceToken {
        Objects.requireNonNull(value, "value must not be null");
    }

    /**
     * Creates a new instance of {@code ReferenceToken} with the specified attribute.
     *
     * @param attribute the attribute used to construct the {@code ReferenceToken}; must not be null
     * @return a new {@code ReferenceToken} instance containing the specified attribute
     * @throws NullPointerException if the attribute is {@code null}
     */
    public static ReferenceToken of(String attribute) {
        return new ReferenceToken(attribute);
    }
}