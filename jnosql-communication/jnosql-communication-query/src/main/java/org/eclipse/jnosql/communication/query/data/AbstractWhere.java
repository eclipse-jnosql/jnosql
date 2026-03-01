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

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.NullQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.StringQueryValue;
import org.eclipse.jnosql.communication.query.Where;
import org.eclipse.jnosql.query.grammar.data.JDQLParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.eclipse.jnosql.communication.Condition.AND;
import static org.eclipse.jnosql.communication.Condition.EQUALS;
import static org.eclipse.jnosql.communication.Condition.NOT;
import static org.eclipse.jnosql.communication.Condition.OR;

abstract class AbstractWhere extends AbstractJDQLProvider {

    protected Where where;

    protected QueryCondition condition;

    protected boolean and = true;

    protected String entity;

    @Override
    protected void runQuery(String query) {
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
    public void exitComparison_expression(JDQLParser.Comparison_expressionContext ctx) {
        super.exitComparison_expression(ctx);
        boolean hasNot = false;
        boolean andCondition = true;
        if (ctx.getParent() instanceof JDQLParser.Conditional_expressionContext ctxParent
                && ctxParent.getParent() instanceof JDQLParser.Conditional_expressionContext grandParent) {
            hasNot = Objects.nonNull(grandParent.NOT());
            andCondition = Objects.isNull(grandParent.OR());
        }
        var contexts = ctx.scalar_expression();
        var contextCondition = getCondition(ctx);

        if (contextCondition.equals(NOT)) {
            contextCondition = EQUALS;
            hasNot = !hasNot;
        }
        var name = contexts.get(0).getText();
        var value = contexts.get(1);
        var literal = PrimaryFunction.INSTANCE.apply(value.primary_expression());
        if (this.condition != null && this.condition.value() instanceof ConditionQueryValue) {
            and = andCondition;
        }
        checkCondition(new DefaultQueryCondition(name, contextCondition, literal), hasNot);
        and = andCondition;
    }

    @Override
    public void exitConditional_expression(JDQLParser.Conditional_expressionContext ctx) {
        super.exitConditional_expression(ctx);
       if(Objects.nonNull(ctx.LPAREN()) || Objects.nonNull(ctx.RPAREN())) {
           throw new UnsupportedOperationException("Eclipse JNoSQL does not support parenthesis is not supported in the query: " + ctx.getText());
        }
    }

    @Override
    public void exitNull_comparison_expression(JDQLParser.Null_comparison_expressionContext ctx) {
        super.exitNull_comparison_expression(ctx);
        boolean hasNot = Objects.nonNull(ctx.NOT());
        boolean andCondition = true;
        if (ctx.getParent() instanceof JDQLParser.Conditional_expressionContext ctxParent
                && ctxParent.getParent() instanceof JDQLParser.Conditional_expressionContext grandParent) {
            andCondition = Objects.isNull(grandParent.OR());
        }
        var stateFieldPathExpressionContext = ctx.state_field_path_expression();
        var name = stateFieldPathExpressionContext.getText();
        if (this.condition != null && this.condition.value() instanceof ConditionQueryValue) {
            and = andCondition;
        }
        checkCondition(new DefaultQueryCondition(name, EQUALS, NullQueryValue.INSTANCE), hasNot);
        and = andCondition;
    }

    @Override
    public void exitLike_expression(JDQLParser.Like_expressionContext ctx) {
        super.exitLike_expression(ctx);
        boolean hasNot = Objects.nonNull(ctx.NOT());
        boolean andCondition = true;

        if (ctx.getParent() instanceof JDQLParser.Conditional_expressionContext ctxParent
                && ctxParent.getParent() instanceof JDQLParser.Conditional_expressionContext grandParent) {
            andCondition = Objects.isNull(grandParent.OR());
        }

        var contexts = ctx.scalar_expression();
        var name = contexts.getText();
        var contextCondition = Condition.LIKE;
        QueryValue<?> value = likeQueryValue(ctx, contexts);

        if (this.condition != null && this.condition.value() instanceof ConditionQueryValue) {
            and = andCondition;
        }
        checkCondition(new DefaultQueryCondition(name, contextCondition, value), hasNot);
        and = andCondition;
    }

    @Override
    public void exitBetween_expression(JDQLParser.Between_expressionContext ctx) {
        super.exitBetween_expression(ctx);
        boolean hasNot = Objects.nonNull(ctx.NOT());
        boolean andCondition = true;

        if (ctx.getParent() instanceof JDQLParser.Conditional_expressionContext ctxParent
                && ctxParent.getParent() instanceof JDQLParser.Conditional_expressionContext grandParent) {
            andCondition = Objects.isNull(grandParent.OR());
        }

        var contexts = ctx.scalar_expression();
        var name = contexts.get(0).getText();
        var firstValue = PrimaryFunction.INSTANCE.apply(contexts.get(1).primary_expression());
        var secondValue = PrimaryFunction.INSTANCE.apply(contexts.get(2).primary_expression());
        var contextCondition = Condition.BETWEEN;
        if (this.condition != null && this.condition.value() instanceof ConditionQueryValue) {
            and = andCondition;
        }
        DataArrayQueryValue value = new DataArrayQueryValue(List.of(firstValue, secondValue));
        checkCondition(new DefaultQueryCondition(name, contextCondition, value), hasNot);
        and = andCondition;
    }

    @Override
    public void exitIn_expression(JDQLParser.In_expressionContext ctx) {
        super.exitIn_expression(ctx);
        boolean hasNot = Objects.nonNull(ctx.NOT());
        boolean andCondition = true;

        if (ctx.getParent() instanceof JDQLParser.Conditional_expressionContext ctxParent
                && ctxParent.getParent() instanceof JDQLParser.Conditional_expressionContext grandParent) {
            andCondition = Objects.isNull(grandParent.OR());
        }

        var name = ctx.state_field_path_expression().getText();
        var contextCondition = Condition.IN;

        List<QueryValue<?>> values = new ArrayList<>();
        if (ctx.input_parameter() != null) {
            values.add(DefaultQueryValue.of(ctx.input_parameter().getText()));
        }
        for (JDQLParser.In_itemContext item : ctx.in_item()) {
            values.add(InItemFunction.INSTANCE.apply(item));
        }

        if (this.condition != null && this.condition.value() instanceof ConditionQueryValue) {
            and = andCondition;
        }
        DataArrayQueryValue value = new DataArrayQueryValue(values);
        checkCondition(new DefaultQueryCondition(name, contextCondition, value), hasNot);
        and = andCondition;
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

    private void checkCondition(QueryCondition condition, boolean hasNot) {
        QueryCondition newCondition = checkNotCondition(condition, hasNot);
        if (Objects.isNull(this.condition)) {
            this.condition = newCondition;
            return;
        }
        if (and) {
            appendCondition(AND, newCondition);
        } else {
            appendCondition(OR, newCondition);
        }

    }

    private void appendCondition(Condition operator, QueryCondition newCondition) {

        if (operator.equals(this.condition.condition())) {
            ConditionQueryValue conditionValue = ConditionQueryValue.class.cast(this.condition.value());
            List<QueryCondition> conditions = new ArrayList<>(conditionValue.get());
            conditions.add(newCondition);
            this.condition = new DefaultQueryCondition("_" + operator.name(), operator, ConditionQueryValue.of(conditions));
        } else if (isNotAppendable()) {
            List<QueryCondition> conditions = Arrays.asList(this.condition, newCondition);
            this.condition = new DefaultQueryCondition("_" + operator.name(), operator, ConditionQueryValue.of(conditions));
        } else {
            List<QueryCondition> conditions = ConditionQueryValue.class.cast(this.condition.value()).get();
            QueryCondition lastCondition = conditions.getLast();

            if (isAppendable(lastCondition) && operator.equals(lastCondition.condition())) {
                List<QueryCondition> lastConditions = new ArrayList<>(ConditionQueryValue.class
                        .cast(lastCondition.value()).get());
                lastConditions.add(newCondition);

                QueryCondition newAppendable = new DefaultQueryCondition("_" + operator.name(),
                        operator, ConditionQueryValue.of(lastConditions));

                List<QueryCondition> newConditions = new ArrayList<>(conditions.subList(0, conditions.size() - 1));
                newConditions.add(newAppendable);
                this.condition = new DefaultQueryCondition(this.condition.name(), this.condition.condition(),
                        ConditionQueryValue.of(newConditions));
            } else {
                QueryCondition newAppendable = new DefaultQueryCondition("_" + operator.name(),
                        operator, ConditionQueryValue.of(Collections.singletonList(newCondition)));

                List<QueryCondition> newConditions = new ArrayList<>(conditions);
                newConditions.add(newAppendable);
                this.condition = new DefaultQueryCondition(this.condition.name(), this.condition.condition(),
                        ConditionQueryValue.of(newConditions));
            }

        }
    }

    private boolean isAppendable(QueryCondition condition) {
        return (AND.equals(condition.condition()) || OR.equals(condition.condition()));
    }

    private boolean isNotAppendable() {
        return !isAppendable(this.condition);
    }

    private QueryCondition checkNotCondition(QueryCondition condition, boolean hasNot) {
        if (hasNot) {
            ConditionQueryValue conditions = ConditionQueryValue.of(Collections.singletonList(condition));
            return new DefaultQueryCondition("_NOT", NOT, conditions);
        } else {
            return condition;
        }
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
