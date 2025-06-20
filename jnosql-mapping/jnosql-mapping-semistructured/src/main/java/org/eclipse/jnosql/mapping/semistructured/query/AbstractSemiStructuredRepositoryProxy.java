/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.data.Sort;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Query;
import jakarta.data.restrict.Restriction;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.QueryType;
import org.eclipse.jnosql.mapping.core.repository.DynamicQueryMethodReturn;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.RepositoryReflectionUtils;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.semistructured.MappingDeleteQuery;
import org.eclipse.jnosql.mapping.semistructured.MappingQuery;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Template method to Repository proxy on column
 *
 * @param <T> the entity type
 * @param <K> the K entity
 */
public abstract class AbstractSemiStructuredRepositoryProxy<T, K> extends BaseSemiStructuredRepository<T, K> {

    private static final Logger LOGGER = Logger.getLogger(AbstractSemiStructuredRepositoryProxy.class.getName());

    @Override
    protected Object executeQuery(Object instance, Method method, Object[] params) {
        LOGGER.finest("Executing query on method: " + method);
        Class<?> type = entityMetadata().type();
        var entity = entityMetadata().name();
        var pageRequest = DynamicReturn.findPageRequest(params);
        var queryValue = method.getAnnotation(Query.class).value();
        var queryType = QueryType.parse(queryValue);
        var returnType = method.getReturnType();
        LOGGER.finest("Query: " + queryValue + " with type: " + queryType + " and return type: " + returnType);
        queryType.checkValidReturn(returnType, queryValue);

        var methodReturn = DynamicQueryMethodReturn.builder()
                .args(params)
                .method(method)
                .typeClass(type)
                .pageRequest(pageRequest)
                .prepareConverter(textQuery -> {
                    var prepare = (org.eclipse.jnosql.mapping.semistructured.PreparedStatement) template().prepare(textQuery, entity);
                    List<Sort<?>> sortsFromAnnotation = getSorts(method, entityMetadata());
                    if (sortsFromAnnotation.isEmpty()) {
                        prepare.setSelectMapper(query -> updateQueryDynamically(params, query));
                    } else {
                        prepare.setSelectMapper(query -> {
                            var selectQuery = updateQueryDynamically(params, query);
                            List<Sort<?>> sorts = new ArrayList<>(selectQuery.sorts());
                            sorts.addAll(sortsFromAnnotation);
                            return new MappingQuery(sorts, selectQuery.limit(), selectQuery.skip(),
                                    selectQuery.condition().orElse(null)
                                    , entity);
                        });
                    }
                    return prepare;
                }).build();
        return methodReturn.execute();
    }

    @Override
    protected Object executeCursorPagination(Object instance, Method method, Object[] params) {

        if (method.getAnnotation(Query.class) != null) {
            var entity = entityMetadata().name();
            var textQuery = method.getAnnotation(Query.class).value();
            var prepare = (org.eclipse.jnosql.mapping.semistructured.PreparedStatement)template().prepare(textQuery, entity);
            var argsParams = RepositoryReflectionUtils.INSTANCE.getParams(method, params);
            argsParams.forEach(prepare::bind);
            var selectQuery = updateQueryDynamically(params, prepare.selectQuery().orElseThrow());
            var special = DynamicReturn.findSpecialParameters(params, sortParser());
            var pageRequest = special.pageRequest()
                    .orElseThrow(() -> new IllegalArgumentException("Pageable is required in the method signature as parameter at " + method));

            return this.template().selectCursor(selectQuery, pageRequest);
        } else if (method.getAnnotation(Find.class) == null) {
            var query = query(method, params);
            var special = DynamicReturn.findSpecialParameters(params, sortParser());
            var pageRequest = special.pageRequest()
                    .orElseThrow(() -> new IllegalArgumentException("Pageable is required in the method signature as parameter at " + method));
            return this.template().selectCursor(query, pageRequest);
        } else {
            var parameters = RepositoryReflectionUtils.INSTANCE.getBy(method, params);
            var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(parameters, getSorts(method, entityMetadata()), entityMetadata());
            var updateQuery = updateQueryDynamically(params, query);
            var special = DynamicReturn.findSpecialParameters(params, sortParser());
            var pageRequest = special.pageRequest()
                    .orElseThrow(() -> new IllegalArgumentException("Pageable is required in the method signature as parameter at " + method));
            return this.template().selectCursor(updateQuery, pageRequest);
        }
    }


    @Override
    protected Object executeDeleteByAll(Object instance, Method method, Object[] params) {
        DeleteQuery deleteQuery = deleteQuery(method, params);
        template().delete(deleteQuery);
        return Void.class;
    }

    @Override
    protected Object executeFindAll(Object instance, Method method, Object[] params) {
        Class<?> type = entityMetadata().type();
        var query = org.eclipse.jnosql.communication.semistructured.SelectQuery.select().from(entityMetadata().name()).build();
        return executeFindByQuery(method, params, type, updateQueryDynamically(params, query));
    }

    @Override
    protected Object executeExistByQuery(Object instance, Method method, Object[] params) {
        return executeExistsByQuery(query(method, params));
    }

    @Override
    protected Object executeCountByQuery(Object instance, Method method, Object[] params) {
        return executeCountByQuery(query(method, params));
    }

    @Override
    protected Object executeFindByQuery(Object instance, Method method, Object[] params) {
        Class<?> type = entityMetadata().type();
        return executeFindByQuery(method, params, type, query(method, params));
    }

    @Override
    protected Object executeParameterBased(Object instance, Method method, Object[] params) {
        Class<?> type = entityMetadata().type();
        Map<String, Object> parameters = RepositoryReflectionUtils.INSTANCE.getBy(method, params);
        var query = SemiStructuredParameterBasedQuery.INSTANCE.toQuery(parameters, getSorts(method, entityMetadata()), entityMetadata());
        return executeFindByQuery(method, params, type, updateQueryDynamically(params, query));
    }

    @Override
    protected Object executeDeleteRestriction(Object instance, Method method, Object[] params) {
        LOGGER.finest("Executing delete restriction on method: " + method);
        Restriction<?> restriction = restriction(params);
        var entity = entityMetadata().name();
        Optional<CriteriaCondition> condition = RestrictionConverter.INSTANCE.parser(restriction, entityMetadata(), converters());
        var deleteQuery = new MappingDeleteQuery(entity, condition.orElse(null));
        this.template().delete(deleteQuery);
        return Void.class;
    }

    private Restriction<?> restriction(Object[] params) {
        if (params.length == 0) {
            throw new IllegalArgumentException("The method must have at least one parameter for restriction");
        }
        if (params[0] instanceof Restriction<?> restriction) {
            return restriction;
        } else {
            throw new IllegalArgumentException("The first parameter must be a Restriction, but was: " + params[0].getClass().getName());
        }
    }


    private static List<Sort<?>> getSorts(Method method, EntityMetadata metadata) {
        return Stream.of(method.getAnnotationsByType(OrderBy.class))
                .map(order -> {
                    String column = metadata.columnField(order.value());
                    if (column == null || column.isEmpty()) {
                        throw new IllegalArgumentException("Invalid field in @OrderBy: " + order.value());
                    }
                    return order.descending() ? Sort.desc(column) : Sort.asc(column);
                })
                .collect(toList());
    }

}
