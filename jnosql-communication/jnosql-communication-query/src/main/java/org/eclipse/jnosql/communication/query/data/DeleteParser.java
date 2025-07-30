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

import org.eclipse.jnosql.communication.query.DeleteQuery;
import org.eclipse.jnosql.query.grammar.data.JDQLParser;

import java.util.Objects;
import java.util.function.Function;

/**
 * Provides the mechanism to process and convert a DELETE query string into a {@link DeleteQuery} object.
 * This class extends {@link AbstractWhere}, utilizing its capabilities to parse conditional expressions
 * and determine the target entity for deletion based on the parsed query.
 *
 * <p>The class implements {@link Function}, accepting a query string and returning
 * a configured {@link DeleteQuery} that encapsulates the entity to be deleted and any applicable conditions
 * specified in the WHERE clause.</p>
 */
public final class DeleteParser extends AbstractWhere implements Function<String, DeleteQuery> {

    @Override
    public DeleteQuery apply(String query) {
        Objects.requireNonNull(query, " query is required");
        runQuery(query);
        if(this.entity == null) {
            throw new IllegalArgumentException("The entity is required in the query");
        }
        return DeleteQuery.of(entity, where);
    }

    @Override
    JDQLParser.Delete_statementContext getTree(JDQLParser parser) {
        return parser.delete_statement();
    }
}
