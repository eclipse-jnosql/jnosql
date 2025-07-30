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


import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.query.data.SelectParser;


/**
 * The result of {@link SelectParser} that has {@link SelectQuery} and {@link Params}.
 * @param query  the {@link SelectQuery}
 * @param params the {@link Params}
 */
public record QueryParams(SelectQuery query, Params params) {

    /**
     * The {@link SelectQuery}
     *
     * @return a {@link SelectQuery} instance
     */
    @Override
    public SelectQuery query() {
        return query;
    }

    /**
     * The {@link Params}
     *
     * @return a {@link Params} instance
     */
    @Override
    public Params params() {
        return params;
    }
}
