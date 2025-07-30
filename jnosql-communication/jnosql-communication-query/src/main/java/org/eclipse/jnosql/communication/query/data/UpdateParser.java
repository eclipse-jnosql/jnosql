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

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.jnosql.communication.query.UpdateItem;
import org.eclipse.jnosql.communication.query.UpdateQuery;
import org.eclipse.jnosql.query.grammar.data.JDQLParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Implements the logic to process and convert an UPDATE query string into an {@link UpdateQuery} object.
 * This class extends {@link AbstractWhere}, leveraging inherited parsing capabilities to interpret the
 * UPDATE query syntax and extract the necessary information for constructing an {@link UpdateQuery}.
 *
 * <p>This class is responsible for initiating the parsing of the UPDATE query and translating the
 * parsed structure into a programmatically usable {@link UpdateQuery} that can be executed against
 * a NoSQL database.</p>
 */
public final class UpdateParser extends AbstractWhere implements Function<String, UpdateQuery> {

    private final List<UpdateItem> items = new ArrayList<>();

    @Override
    public UpdateQuery apply(String query) {
        Objects.requireNonNull(query, " query is required");
        runQuery(query);
        if(this.entity == null) {
            throw new IllegalArgumentException("The entity is required in the query");
        }
        return new JDQLUpdateQuery(this.entity, items, where);
    }

    @Override
    public void exitUpdate_item(JDQLParser.Update_itemContext ctx) {
        super.exitUpdate_item(ctx);
        String name = ctx.state_field_path_expression().getText();
        var scalarContext = ctx.scalar_expression();
        if(isArithmeticOperation(scalarContext)) {
            throw new UnsupportedOperationException("Eclipse JNoSQL does not support arithmetic operations in the UPDATE clause: " + scalarContext.getText());
        }
        if(hasParenthesis(scalarContext)) {
            throw new UnsupportedOperationException("Eclipse JNoSQL does not support parenthesis in the UPDATE clause: " + scalarContext.getText());
        }
        var primaryExpression = scalarContext.primary_expression();
        var value = PrimaryFunction.INSTANCE.apply(primaryExpression);
        items.add(JDQLUpdateItem.of(name, value));

    }



    @Override
    public void exitEntity_name(JDQLParser.Entity_nameContext ctx) {
        super.exitEntity_name(ctx);
        this.entity = ctx.getText();
    }

    @Override
    ParserRuleContext getTree(JDQLParser parser) {
        return parser.update_statement();
    }

    private static boolean isArithmeticOperation(JDQLParser.Scalar_expressionContext scalarContext) {
        return Objects.nonNull(scalarContext.MUL())
                || Objects.nonNull(scalarContext.DIV())
                || Objects.nonNull(scalarContext.PLUS())
                || Objects.nonNull(scalarContext.MINUS())
                || Objects.nonNull(scalarContext.CONCAT());
    }
    private boolean hasParenthesis(JDQLParser.Scalar_expressionContext scalarContext) {
        return Objects.nonNull(scalarContext.LPAREN()) || Objects.nonNull(scalarContext.RPAREN());
    }

}
