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
 *  Matheus Oliveira
 */
package org.eclipse.jnosql.communication.query.data;

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.NullQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.StringQueryValue;
import org.eclipse.jnosql.communication.query.Where;
import org.eclipse.jnosql.query.grammar.data.JDQLParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.eclipse.jnosql.communication.Condition.AND;
import static org.eclipse.jnosql.communication.Condition.EQUALS;
import static org.eclipse.jnosql.communication.Condition.NOT;
import static org.eclipse.jnosql.communication.Condition.OR;

abstract class AbstractWhere extends AbstractJDQLProvider {

    protected Where where;

    protected QueryCondition condition;

    protected String entity;

    @Override
    protected void runQuery(String query) {
        this.condition = null;
        this.where = null;
        super.runQuery(query);

        if (Objects.nonNull(condition)) {
            this.where = Where.of(condition);
        }
    }

    @Override
    public void exitFrom_clause(JDQLParser.From_clauseContext ctx) {
        this.entity = ctx.entity_name().getText();
    }

    @Override
    public void exitWhere_clause(JDQLParser.Where_clauseContext ctx) {
        super.exitWhere_clause(ctx);
        this.condition = toCondition(ctx.conditional_expression());
    }

    private QueryCondition toCondition(JDQLParser.Conditional_expressionContext ctx) {
        if (Objects.nonNull(ctx.LPAREN())) {
            return toCondition(ctx.conditional_expression(0));
        }
        if (Objects.nonNull(ctx.AND())) {
            return logical(AND, ctx);
        }
        if (Objects.nonNull(ctx.OR())) {
            return logical(OR, ctx);
        }
        if (Objects.nonNull(ctx.NOT())) {
            return negate(toCondition(ctx.conditional_expression(0)));
        }
        if (Objects.nonNull(ctx.null_comparison_expression())) {
            return nullComparison(ctx.null_comparison_expression());
        }
        if (Objects.nonNull(ctx.in_expression())) {
            return inCondition(ctx.in_expression());
        }
        if (Objects.nonNull(ctx.between_expression())) {
            return betweenCondition(ctx.between_expression());
        }
        if (Objects.nonNull(ctx.like_expression())) {
            return likeCondition(ctx.like_expression());
        }
        return comparison(ctx.comparison_expression());
    }

    private QueryCondition logical(Condition operator, JDQLParser.Conditional_expressionContext ctx) {
        List<QueryCondition> operands = new ArrayList<>();
        for (var operand : ctx.conditional_expression()) {
            var condition = toCondition(operand);
            if (operator.equals(condition.condition())) {
                operands.addAll(((ConditionQueryValue) condition.value()).get());
            } else {
                operands.add(condition);
            }
        }
        return new DefaultQueryCondition("_" + operator.name(), operator, ConditionQueryValue.of(operands));
    }

    private QueryCondition comparison(JDQLParser.Comparison_expressionContext ctx) {
        boolean hasNot = false;
        var contexts = ctx.scalar_expression();
        var contextCondition = getCondition(ctx);

        if (contextCondition.equals(NOT)) {
            contextCondition = EQUALS;
            hasNot = !hasNot;
        }
        var name = contexts.get(0).getText();
        var value = contexts.get(1);
        var left = contexts.get(0);
        var literal = PrimaryFunction.INSTANCE.apply(value.primary_expression());
        return negateIf(leaf(left, name, contextCondition, literal), hasNot);
    }

    private QueryCondition leaf(JDQLParser.Scalar_expressionContext left, String name, Condition condition,
                                QueryValue<?> value) {
        var primary = unwrap(left).primary_expression();
        if (primary != null && primary.function_expression() != null) {
            var function = ((FunctionQueryValue) PrimaryFunction.INSTANCE.apply(primary)).get();
            return new FunctionQueryCondition(name, condition, value, function);
        }
        return new DefaultQueryCondition(name, condition, value);
    }

    private static JDQLParser.Scalar_expressionContext unwrap(JDQLParser.Scalar_expressionContext context) {
        var current = context;
        while (current.LPAREN() != null && current.scalar_expression().size() == 1) {
            current = current.scalar_expression(0);
        }
        return current;
    }

    private QueryCondition nullComparison(JDQLParser.Null_comparison_expressionContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.NOT());
        var stateFieldPathExpressionContext = ctx.state_field_path_expression();
        var name = stateFieldPathExpressionContext.getText();
        return negateIf(new DefaultQueryCondition(name, EQUALS, NullQueryValue.INSTANCE), hasNot);
    }

    private QueryCondition likeCondition(JDQLParser.Like_expressionContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.NOT());

        var contexts = ctx.scalar_expression();
        var name = contexts.getText();
        var contextCondition = Condition.LIKE;
        QueryValue<?> value = likeQueryValue(ctx, contexts);

        return negateIf(leaf(contexts, name, contextCondition, value), hasNot);
    }

    private QueryCondition betweenCondition(JDQLParser.Between_expressionContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.NOT());

        var contexts = ctx.scalar_expression();
        var name = contexts.get(0).getText();
        var firstValue = PrimaryFunction.INSTANCE.apply(contexts.get(1).primary_expression());
        var secondValue = PrimaryFunction.INSTANCE.apply(contexts.get(2).primary_expression());
        var contextCondition = Condition.BETWEEN;
        DataArrayQueryValue value = new DataArrayQueryValue(List.of(firstValue, secondValue));
        return negateIf(leaf(contexts.get(0), name, contextCondition, value), hasNot);
    }

    private QueryCondition inCondition(JDQLParser.In_expressionContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.NOT());

        var name = ctx.state_field_path_expression().getText();
        var contextCondition = Condition.IN;

        List<QueryValue<?>> values = new ArrayList<>();
        if (ctx.input_parameter() != null) {
            values.add(DefaultQueryValue.of(ctx.input_parameter().getText()));
        }
        for (JDQLParser.In_itemContext item : ctx.in_item()) {
            values.add(InItemFunction.INSTANCE.apply(item));
        }

        DataArrayQueryValue value = new DataArrayQueryValue(values);
        return negateIf(new DefaultQueryCondition(name, contextCondition, value), hasNot);
    }

    private Condition getCondition(JDQLParser.Comparison_expressionContext ctx) {
        var context = ctx.comparison_operator();
        if (context.EQ() != null) {
            return EQUALS;
        } else if (context.LT() != null) {
            return Condition.LESSER_THAN;
        } else if (context.LTEQ() != null) {
            return Condition.LESSER_EQUALS_THAN;
        } else if (context.GT() != null) {
            return Condition.GREATER_THAN;
        } else if (context.GTEQ() != null) {
            return Condition.GREATER_EQUALS_THAN;
        } else if (context.NEQ() != null) {
            return NOT;
        }
        throw new UnsupportedOperationException("The operation does not support: " + ctx.getText());
    }

    private QueryCondition negateIf(QueryCondition condition, boolean hasNot) {
        return hasNot ? negate(condition) : condition;
    }

    private QueryCondition negate(QueryCondition condition) {
        if (NOT.equals(condition.condition())) {
            return ((ConditionQueryValue) condition.value()).get().getFirst();
        }
        return new DefaultQueryCondition("_NOT", NOT, ConditionQueryValue.of(List.of(condition)));
    }

    private static QueryValue<?> likeQueryValue(JDQLParser.Like_expressionContext ctx, JDQLParser.Scalar_expressionContext contexts) {
        if(ctx.input_parameter() != null){
            return DefaultQueryValue.of(ctx.input_parameter().getText());
        } else {
            var likeValueIndex = ctx.getChildCount() - 1;
            var likeValue = contexts.getParent().getChild(likeValueIndex).getText();
            return StringQueryValue.of(likeValue.substring(1, likeValue.length() - 1));
        }
    }
}
