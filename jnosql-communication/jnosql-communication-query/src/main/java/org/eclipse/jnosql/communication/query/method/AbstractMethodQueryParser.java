/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.query.method;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.query.ArrayQueryValue;
import org.eclipse.jnosql.communication.query.BooleanQueryValue;
import org.eclipse.jnosql.communication.query.ConditionQueryValue;
import org.eclipse.jnosql.communication.query.ParamQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryErrorListener;
import org.eclipse.jnosql.communication.query.StringQueryValue;
import org.eclipse.jnosql.communication.query.Where;
import org.eclipse.jnosql.query.grammar.method.MethodBaseListener;
import org.eclipse.jnosql.query.grammar.method.MethodLexer;
import org.eclipse.jnosql.query.grammar.method.MethodParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.eclipse.jnosql.communication.Condition.AND;
import static org.eclipse.jnosql.communication.Condition.BETWEEN;
import static org.eclipse.jnosql.communication.Condition.EQUALS;
import static org.eclipse.jnosql.communication.Condition.GREATER_EQUALS_THAN;
import static org.eclipse.jnosql.communication.Condition.GREATER_THAN;
import static org.eclipse.jnosql.communication.Condition.IN;
import static org.eclipse.jnosql.communication.Condition.LESSER_EQUALS_THAN;
import static org.eclipse.jnosql.communication.Condition.LESSER_THAN;
import static org.eclipse.jnosql.communication.Condition.LIKE;
import static org.eclipse.jnosql.communication.Condition.NOT;
import static org.eclipse.jnosql.communication.Condition.OR;

abstract class AbstractMethodQueryParser extends MethodBaseListener {

    private static final String SUB_ENTITY_FLAG = "_";
    protected Where where;

    protected QueryCondition condition;

    protected boolean and = true;

    protected boolean shouldCount = false;

    protected void runQuery(String query) {

        CharStream stream = CharStreams.fromString(query);
        MethodLexer lexer = new MethodLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MethodParser parser = new MethodParser(tokens);
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(QueryErrorListener.INSTANCE);
        parser.addErrorListener(QueryErrorListener.INSTANCE);

        ParseTree tree = getParserTree().apply(parser);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(this, tree);

        if (Objects.nonNull(condition)) {
            this.where = Where.of(condition);
        }
    }

    abstract Function<MethodParser, ParseTree> getParserTree();

    @Override
    public void exitSelectStart(MethodParser.SelectStartContext ctx) {
        this.shouldCount = ctx.getText().startsWith("count");
    }

    @Override
    public void exitEq(MethodParser.EqContext ctx) {
        Condition operator = EQUALS;
        boolean hasNot = Objects.nonNull(ctx.not());
        String variable = getVariable(ctx.variable());
        appendCondition(hasNot, variable, operator);
    }

    @Override
    public void exitTruth(MethodParser.TruthContext ctx) {
        String variable = getVariable(ctx.variable());
        checkCondition(new MethodCondition(variable, EQUALS, BooleanQueryValue.TRUE), false);
    }

    @Override
    public void exitUntruth(MethodParser.UntruthContext ctx) {
        String variable = getVariable(ctx.variable());
        checkCondition(new MethodCondition(variable, EQUALS, BooleanQueryValue.FALSE), false);
    }

    @Override
    public void exitGt(MethodParser.GtContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String variable = getVariable(ctx.variable());
        Condition operator = GREATER_THAN;
        appendCondition(hasNot, variable, operator);
    }

    @Override
    public void exitGte(MethodParser.GteContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String variable = getVariable(ctx.variable());
        Condition operator = GREATER_EQUALS_THAN;
        appendCondition(hasNot, variable, operator);
    }

    @Override
    public void exitLt(MethodParser.LtContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String variable = getVariable(ctx.variable());
        Condition operator = LESSER_THAN;
        appendCondition(hasNot, variable, operator);
    }

    @Override
    public void exitLte(MethodParser.LteContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String variable = getVariable(ctx.variable());
        Condition operator = LESSER_EQUALS_THAN;
        appendCondition(hasNot, variable, operator);
    }

    @Override
    public void exitLike(MethodParser.LikeContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String variable = getVariable(ctx.variable());
        Condition operator = LIKE;
        appendCondition(hasNot, variable, operator);
    }

    @Override
    public void exitIn(MethodParser.InContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String variable = getVariable(ctx.variable());
        Condition operator = IN;
        appendCondition(hasNot, variable, operator);
    }

    @Override
    public void exitBetween(MethodParser.BetweenContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String variable = getVariable(ctx.variable());
        Condition operator = BETWEEN;
        ArrayQueryValue value = MethodArrayValue.of(variable);
        checkCondition(new MethodCondition(variable, operator, value), hasNot);
    }

    @Override
    public void exitNullable(MethodParser.NullableContext ctx) {
        boolean hasNot = Objects.nonNull(ctx.not());
        String variable = getVariable(ctx.variable());
        checkCondition(new MethodCondition(variable, EQUALS, StringQueryValue.of(null)), hasNot);
    }

    @Override
    public void exitAnd(MethodParser.AndContext ctx) {
        this.and = true;
    }

    @Override
    public void exitOr(MethodParser.OrContext ctx) {
        this.and = false;
    }

    @Override
    public void exitContains(MethodParser.ContainsContext ctx) {
        throw new UnsupportedOperationException("Contains is not supported in Eclipse JNoSQL method query");
    }

    @Override
    public void exitEndsWith(MethodParser.EndsWithContext ctx) {
        throw new UnsupportedOperationException("EndsWith is not supported in Eclipse JNoSQL method query");
    }

    @Override
    public void exitStartsWith(MethodParser.StartsWithContext ctx) {
        throw new UnsupportedOperationException("StartsWith is not supported in Eclipse JNoSQL method query");
    }

    @Override
    public void exitIgnoreCase(MethodParser.IgnoreCaseContext ctx) {
        throw new UnsupportedOperationException("IgnoreCase is not supported in Eclipse JNoSQL method query");
    }

    private void appendCondition(boolean hasNot, String variable, Condition operator) {
        ParamQueryValue queryValue = new MethodParamQueryValue(variable);
        checkCondition(new MethodCondition(variable, operator, queryValue), hasNot);
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

    private String getVariable(MethodParser.VariableContext ctx) {
        return getFormatField(ctx.getText());
    }

    protected String getFormatField(String text) {
        if (text.contains(SUB_ENTITY_FLAG)) {
            return Stream.of(text.split(SUB_ENTITY_FLAG)).map(this::formatField).collect(joining("."));
        } else {
            return formatField(text);
        }
    }

    private String formatField(String text) {
        String lowerCase = String.valueOf(text.charAt(0)).toLowerCase(Locale.US);
        return lowerCase.concat(text.substring(1));
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
            return new MethodCondition("_NOT", NOT, conditions);
        } else {
            return condition;
        }
    }

    private void appendCondition(Condition operator, QueryCondition newCondition) {

        if (operator.equals(this.condition.condition())) {
            ConditionQueryValue conditionValue = ConditionQueryValue.class.cast(this.condition.value());
            List<QueryCondition> conditions = new ArrayList<>(conditionValue.get());
            conditions.add(newCondition);
            this.condition = new MethodCondition(SUB_ENTITY_FLAG + operator.name(), operator, ConditionQueryValue.of(conditions));
        } else if (isNotAppendable()) {
            List<QueryCondition> conditions = Arrays.asList(this.condition, newCondition);
            this.condition = new MethodCondition(SUB_ENTITY_FLAG + operator.name(), operator, ConditionQueryValue.of(conditions));
        } else {
            List<QueryCondition> conditions = ConditionQueryValue.class.cast(this.condition.value()).get();
            QueryCondition lastCondition = conditions.getLast();

            if (isAppendable(lastCondition) && Condition.EQUALS.equals(lastCondition.condition())) {
                List<QueryCondition> lastConditions = new ArrayList<>(ConditionQueryValue.class.cast(lastCondition.value()).get());
                lastConditions.add(newCondition);

                QueryCondition newAppendable = new MethodCondition(SUB_ENTITY_FLAG + operator.name(),
                        operator, ConditionQueryValue.of(lastConditions));

                List<QueryCondition> newConditions = new ArrayList<>(conditions.subList(0, conditions.size() - 1));
                newConditions.add(newAppendable);
                this.condition = new MethodCondition(this.condition.name(), this.condition.condition(),
                        ConditionQueryValue.of(newConditions));
            } else {
                QueryCondition newAppendable = new MethodCondition(SUB_ENTITY_FLAG + operator.name(),
                        operator, ConditionQueryValue.of(Collections.singletonList(newCondition)));

                List<QueryCondition> newConditions = new ArrayList<>(conditions);
                newConditions.add(newAppendable);
                this.condition = new MethodCondition(this.condition.name(), this.condition.condition(),
                        ConditionQueryValue.of(newConditions));
            }

        }
    }
}
