/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.method;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.jnosql.communication.query.DeleteQuery;
import org.eclipse.jnosql.query.grammar.method.MethodParser;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Parses repository method names into delete queries.
 */
public final class DeleteByMethodQueryParser extends AbstractMethodQueryParser implements BiFunction<String, String, DeleteQuery> {


    @Override
    public DeleteQuery apply(String query, String entity) {
        Objects.requireNonNull(query, " query is required");
        Objects.requireNonNull(entity, " entity is required");
        runQuery(QueryTokenizer.of(query).get());
        return DeleteQuery.of(entity, where);
    }

    @Override
    Function<MethodParser, ParseTree> getParserTree() {
        return MethodParser::deleteBy;
    }
}
