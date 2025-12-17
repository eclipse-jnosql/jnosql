/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured.repository;

import jakarta.data.Direction;
import jakarta.data.Sort;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.core.repository.SpecialParameters;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.MappingQuery;
import org.eclipse.jnosql.mapping.semistructured.query.RestrictionConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

enum DynamicSelectQueryBuilder {

    INSTANCE;

    SelectQuery updateDynamicQuery(SelectQuery query,
                                   RepositoryInvocationContext context,
                                   CommunicationObserverParser parser,
                                   Converters converters) {

        var specialParameters = SpecialParameters.of(context.parameters(), Function.identity());
        var pagination = resolvePagination(query, context.method(), specialParameters);
        var condition = condition(query, converters, context.entityMetadata(), specialParameters);
        var sorts = sorts(query, parser, context);
        var columns = columns(query, parser, context);
        
        return new MappingQuery(sorts,
                pagination.limit,
                pagination.skip,
                condition,
                query.name(),
                columns);
    }

    static SelectQuery includeInheritance(SelectQuery query, EntityMetadata metadata) {
        var condition = includeInheritance(metadata);
        if (condition == null) {
            return query;
        }
        if (query.condition().isPresent()) {
            CriteriaCondition columnCondition = query.condition().orElseThrow();
            condition = condition.and(columnCondition);
        }
        return new MappingQuery(query.sorts(), query.limit(), query.skip(),
                condition, query.name(), query.columns());
    }

    static CriteriaCondition includeInheritance(EntityMetadata metadata) {
        if (metadata.inheritance().isPresent()) {
            InheritanceMetadata inheritanceMetadata = metadata.inheritance().orElseThrow();
            if (!inheritanceMetadata.parent().equals(metadata.type())) {
                return CriteriaCondition.eq(Element.of(inheritanceMetadata.discriminatorColumn(),
                        inheritanceMetadata.discriminatorValue()));
            }
        }
        return null;
    }

    static SelectQuery applyInheritance(SelectQuery query, RepositoryInvocationContext context) {
        var entityMetadata = context.entityMetadata();
        return includeInheritance(query, entityMetadata);
    }


    private static CriteriaCondition appendCriteriaCondition(CriteriaCondition condition, CriteriaCondition newCondition) {
        if (condition != null) {
            condition = condition.and(newCondition);
        } else {
            condition = newCondition;
        }
        return condition;
    }

    private static List<String> columns(SelectQuery query,
                                        CommunicationObserverParser parser,
                                        RepositoryInvocationContext context) {

        var entityMetadata = context.entityMetadata();
        var method = context.method();

        var columns = new ArrayList<>(query.columns());
        columns.addAll(method.select());
        if (columns.isEmpty()) {
            return columns;
        }
        return columns.stream().map(c -> parser.fireSelectField(entityMetadata.name(), c)).toList();
    }

    private static List<Sort<?>> sorts(SelectQuery query,
                                       CommunicationObserverParser parser,
                                       RepositoryInvocationContext context) {

        var entityMetadata = context.entityMetadata();
        var method = context.method();
        var sorts = new ArrayList<>(query.sorts());
        sorts.addAll(method.sorts());

        if (sorts.isEmpty()) {
            return sorts;
        }
        List<Sort<?>> updateSorts = new ArrayList<>(sorts.size());
        updateSorts.addAll(sorts.stream().map(sort -> {
            String attribute = parser.fireSortProperty(entityMetadata.name(), sort.property());
            return Sort.of(attribute, sort.isAscending() ? Direction.ASC : Direction.DESC, sort.ignoreCase());
        }).toList());
        return updateSorts;
    }

    private Pagination resolvePagination(SelectQuery query,
                                         RepositoryMethod method,
                                         SpecialParameters specialParameters) {

        long limit = query.limit();
        long skip = query.skip();

        if (method.first().isPresent()) {
            return new Pagination(0, method.first().orElseThrow());
        }

        if (specialParameters.limit().isPresent()) {
            var limitParam = specialParameters.limit().orElseThrow();
            return new Pagination(limitParam.maxResults(), limitParam.startAt() - 1);
        }

        if (specialParameters.pageRequest().isPresent()) {
            var pageRequest = specialParameters.pageRequest().orElseThrow();
            return new Pagination(pageRequest.size(), NoSQLPage.skip(pageRequest));
        }

        return new Pagination(limit, skip);
    }

    private static CriteriaCondition condition(SelectQuery query, Converters converters, EntityMetadata entityMetadata, SpecialParameters specialParameters) {
        var condition = query.condition().orElse(null);
        var conditionInheritance = includeInheritance(entityMetadata);
        if (conditionInheritance != null) {
            condition = appendCriteriaCondition(condition, conditionInheritance);
        }

        if (specialParameters.restriction().isPresent()) {
            var restrictionCondition = RestrictionConverter.INSTANCE.parser(specialParameters.restriction().orElseThrow(),
                    entityMetadata,
                    converters);
            if (restrictionCondition.isPresent()) {
                condition = appendCriteriaCondition(condition, restrictionCondition.orElseThrow());
            }
        }
        return condition;
    }

    private record Pagination(long limit, long skip) {
    }
}
