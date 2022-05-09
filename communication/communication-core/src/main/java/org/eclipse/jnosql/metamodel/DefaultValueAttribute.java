/*
 *  Copyright (c) 2022 Otávio Santana and others
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
 *   Alessandro Moscatelli
 */
package org.eclipse.jnosql.metamodel;

import jakarta.nosql.metamodel.ValueAttribute;

/**
 * Default metamodel value attribute implementation
 * @param <X> The Entity type the attribute belongs to
 * @param <T> The value attribute type (cannot be an entity)
 */
public class DefaultValueAttribute<X, T> extends DefaultSingularAttribute<X, T> implements ValueAttribute<X, T>{

    public DefaultValueAttribute(Class<X> type, Class<T> attributeType, String name) {
        super(type, attributeType, name);
    }
    
}
