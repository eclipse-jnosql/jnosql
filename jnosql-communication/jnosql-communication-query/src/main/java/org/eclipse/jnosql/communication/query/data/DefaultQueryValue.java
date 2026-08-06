/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.data;


import org.eclipse.jnosql.communication.query.ParamQueryValue;

/**
 * The default implementation of {@link ParamQueryValue}
 */
public record DefaultQueryValue(String value) implements ParamQueryValue {


    @Override
    public String get() {
        return value;
    }

    @Override
    public String toString() {
        return "@" + value;
    }

    /**
     * Creates a default query value, stripping a leading named-parameter marker when present.
     *
     * @param text the value text
     * @return the query value
     */
    public static DefaultQueryValue of(String text) {
        if(text != null && text.startsWith(":")) {
            return new DefaultQueryValue(text.substring(1));
        }
        return new DefaultQueryValue(text);
    }

}