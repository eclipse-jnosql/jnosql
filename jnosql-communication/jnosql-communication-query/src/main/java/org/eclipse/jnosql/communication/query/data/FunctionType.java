/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Matheus Oliveira
 */
package org.eclipse.jnosql.communication.query.data;

import org.eclipse.jnosql.query.grammar.data.JDQLParser;

import java.util.function.Predicate;

/**
 * Maps each JDQL function keyword token to its canonical function name string.
 *
 * <p>Use {@link #from(JDQLParser.Function_expressionContext)} to resolve the
 * function name from a grammar context without if-else chains.</p>
 */
enum FunctionType {

    ABS(ctx -> ctx.ABS() != null, 1),
    LENGTH(ctx -> ctx.LENGTH() != null, 1),
    LOWER(ctx -> ctx.LOWER() != null, 1),
    UPPER(ctx -> ctx.UPPER() != null, 1),
    LEFT(ctx -> ctx.LEFT() != null, 2),
    RIGHT(ctx -> ctx.RIGHT() != null, 2);

    private final Predicate<JDQLParser.Function_expressionContext> matcher;
    private final int arity;

    FunctionType(Predicate<JDQLParser.Function_expressionContext> matcher, int arity) {
        this.matcher = matcher;
        this.arity = arity;
    }

    /**
     * Resolves the canonical function name from a grammar context,
     * validating that the argument count matches the expected arity.
     *
     * @param ctx the function expression context; must not be {@code null}
     * @return the uppercase function name (e.g., {@code "UPPER"})
     * @throws UnsupportedOperationException if no known function matches
     * @throws IllegalArgumentException if the argument count does not match
     */
    static String from(JDQLParser.Function_expressionContext ctx) {
        for (FunctionType type : values()) {
            if (type.matcher.test(ctx)) {
                int actual = ctx.scalar_expression().size();
                if (actual != type.arity) {
                    throw new IllegalArgumentException(
                            "Function " + type.name() + " expects " + type.arity
                                    + " argument(s) but got " + actual);
                }
                return type.name();
            }
        }
        throw new UnsupportedOperationException("The function is not supported yet: " + ctx.getText());
    }
}
