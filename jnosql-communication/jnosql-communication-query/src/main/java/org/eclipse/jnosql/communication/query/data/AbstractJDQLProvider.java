/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.data;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.eclipse.jnosql.communication.QueryException;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.jnosql.communication.query.QueryErrorListener;
import org.eclipse.jnosql.query.grammar.data.JDQLBaseListener;
import org.eclipse.jnosql.query.grammar.data.JDQLLexer;
import org.eclipse.jnosql.query.grammar.data.JDQLParser;


abstract class AbstractJDQLProvider extends JDQLBaseListener {

    protected void runQuery(String query) {

        CharStream stream = CharStreams.fromString(query);
        JDQLLexer lexer = new JDQLLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        JDQLParser parser = new JDQLParser(tokens);
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(QueryErrorListener.INSTANCE);
        parser.addErrorListener(QueryErrorListener.INSTANCE);

        var tree = getTree(parser);
        var unconsumed = tokens.LT(1);
        if (unconsumed.getType() != Token.EOF) {
            throw new QueryException("line " + unconsumed.getLine() + ":" + unconsumed.getCharPositionInLine()
                    + " extraneous input '" + unconsumed.getText() + "' expecting end of query");
        }
        ParseTreeWalker.DEFAULT.walk(this, tree);
    }

    abstract ParserRuleContext getTree(JDQLParser parser);
}
