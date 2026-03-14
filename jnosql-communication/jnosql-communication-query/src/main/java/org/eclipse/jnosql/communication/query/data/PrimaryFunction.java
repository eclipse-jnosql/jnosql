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
 *  Matheus Oliveira
 */
package org.eclipse.jnosql.communication.query.data;

import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.query.BooleanQueryValue;
import org.eclipse.jnosql.communication.query.EnumQueryValue;
import org.eclipse.jnosql.communication.query.FunctionQueryValue;
import org.eclipse.jnosql.communication.query.NumberQueryValue;
import org.eclipse.jnosql.communication.query.QueryPath;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.StringQueryValue;
import org.eclipse.jnosql.query.grammar.data.JDQLParser;

import java.util.Locale;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

enum PrimaryFunction implements Function<JDQLParser.Primary_expressionContext, QueryValue<?>> {

    INSTANCE;

    private static final Logger LOGGER = Logger.getLogger(PrimaryFunction.class.getName());

    @Override
    public QueryValue<?> apply(JDQLParser.Primary_expressionContext context) {
        if (context.literal() != null) {
            var literal = context.literal();
            if (literal.STRING() != null) {
                String text = literal.STRING().getText();
                return StringQueryValue.of(text.substring(1, text.length() - 1));
            } else if (literal.INTEGER() != null) {
                return NumberQueryValue.of(Integer.valueOf(literal.INTEGER().getText()));
            } else if (literal.DOUBLE() != null) {
                return NumberQueryValue.of(Double.valueOf(literal.DOUBLE().getText()));
            }
        } else if (context.input_parameter() != null) {
            return DefaultQueryValue.of(context.input_parameter().getText());
        } else if (context.special_expression() != null) {
            var specialExpression = context.special_expression().getText();
            return switch (specialExpression.toUpperCase(Locale.US)) {
                case "TRUE" -> BooleanQueryValue.TRUE;
                case "FALSE" -> BooleanQueryValue.FALSE;
                default ->
                        throw new UnsupportedOperationException("The special expression is not supported yet: " + specialExpression);
            };
        } else if (context.function_expression() != null) {
            var functionExpression = context.function_expression();
            var functionName = getFunctionName(functionExpression);
            var params = functionExpression.scalar_expression().stream()
                    .map(this::processScalar)
                    .toArray();
            return new FunctionQueryValue(new DefaultFunction(functionName, params));
        } else if (context.enum_literal() != null) {
            Enum<?> value = EnumConverter.INSTANCE.apply(context.enum_literal().getText());
            return EnumQueryValue.of(value);
        } else if (context.state_field_path_expression() != null) {
            var stateContext = context.state_field_path_expression();
            var stateContextText = stateContext.getText();
            try {
                Enum<?> value = EnumConverter.INSTANCE.apply(stateContextText);
                return EnumQueryValue.of(value);
            } catch (QueryException exp) {
                LOGGER.log(Level.FINE, "define the enum converter and trying to parse as a path: " + stateContextText, exp);
                return QueryPath.of(stateContextText);
            }
        }
        throw new UnsupportedOperationException("The primary expression is not supported yet: " + context.getText());
    }

    private Object processScalar(JDQLParser.Scalar_expressionContext ctx) {
        if (ctx.primary_expression() != null) {
            return apply(ctx.primary_expression());
        }
        if (ctx.LPAREN() != null && ctx.scalar_expression().size() == 1) {
            return processScalar(ctx.scalar_expression(0));
        }
        return ctx.getText();
    }

    private String getFunctionName(JDQLParser.Function_expressionContext ctx) {
        if (ctx.ABS() != null) {
            return "ABS";
        } else if (ctx.LENGTH() != null) {
            return "LENGTH";
        } else if (ctx.LOWER() != null) {
            return "LOWER";
        } else if (ctx.UPPER() != null) {
            return "UPPER";
        } else if (ctx.LEFT() != null) {
            return "LEFT";
        } else if (ctx.RIGHT() != null) {
            return "RIGHT";
        }
        throw new UnsupportedOperationException("The function is not supported yet: " + ctx.getText());
    }
}
