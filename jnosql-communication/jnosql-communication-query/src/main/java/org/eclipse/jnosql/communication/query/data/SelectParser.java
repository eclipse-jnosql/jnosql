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

import jakarta.data.Sort;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.jnosql.communication.query.SelectQuery;
import org.eclipse.jnosql.query.grammar.data.JDQLParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Implements the selection logic for a NoSQL query processing system by extending {@link AbstractWhere}.
 * This class is responsible for configuring and executing SELECT queries based on provided query strings
 * and entity information. It parses the query to extract relevant conditions, sorting, and aggregation instructions,
 * and then constructs a {@link SelectQuery} object that can be used to fetch results according to these specifications.
 */
public final class SelectParser extends AbstractWhere implements BiFunction<String, String, SelectQuery> {

    private final List<Sort<?>> sorts = new ArrayList<>();

    private final List<String> fields = new ArrayList<>();

    private boolean count = false;

    @Override
    public SelectQuery apply(String query, String entity) {
        Objects.requireNonNull(query, " query is required");
        this.entity = entity;
        runQuery(query);
        if(this.entity == null) {
            throw new IllegalArgumentException("The entity is required in the query");
        }
        return new JDQLSelectQuery(fields, this.entity, sorts, where, count);
    }

    @Override
    public void exitAggregate_expression(JDQLParser.Aggregate_expressionContext ctx) {
        super.exitAggregate_expression(ctx);
        this.count = true;
    }

    @Override
    public void exitOrderby_clause(JDQLParser.Orderby_clauseContext ctx) {
     ctx.orderby_item().stream().forEach(o -> {
         String field = o.state_field_path_expression().getText();

         boolean desc = o.getChild(1) != null && o.getChild(1).getText().equals("DESC");
         sorts.add(desc ? Sort.desc(field) : Sort.asc(field));
     });
    }

    @Override
    public void exitSelect_list(JDQLParser.Select_listContext ctx) {
        var stateField = ctx.state_field_path_expression();
        var aggregate = ctx.aggregate_expression();

        if (stateField != null) {
            stateField.stream().map(ParseTree::getText).forEach(fields::add);
        } else if (aggregate != null) {
           this.count = true;
        }
    }

    @Override
    JDQLParser.Select_statementContext getTree(JDQLParser parser) {
        return parser.select_statement();
    }
}
