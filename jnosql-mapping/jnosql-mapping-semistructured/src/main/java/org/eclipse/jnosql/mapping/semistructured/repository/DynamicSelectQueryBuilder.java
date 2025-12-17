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

        var method = context.method();
        var entityMetadata = context.entityMetadata();
        var specialParameters = SpecialParameters.of(context.parameters(), Function.identity());

        var columns = new ArrayList<>(query.columns());
        columns.addAll(method.select());

        var sorts = new ArrayList<>(query.sorts());
        sorts.addAll(method.sorts());

        var limit = query.limit();
        var skip = query.skip();
        if (method.first().isPresent()) {
            limit = 0;
            skip = method.first().orElseThrow();
        }

        if (specialParameters.limit().isPresent()) {
            var limitParam = specialParameters.limit().orElseThrow();
            limit = limitParam.maxResults();
            skip = limitParam.startAt() - 1;
        }

        if(specialParameters.pageRequest().isPresent()) {
            var pageRequest = specialParameters.pageRequest().orElseThrow();
            limit = pageRequest.size();
            skip = NoSQLPage.skip(pageRequest);
        }

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

        return new MappingQuery(sorts(parser, sorts, entityMetadata),
                limit, skip,
                condition, query.name(),
                columns(parser, columns, entityMetadata));
    }

    SelectQuery includeInheritance(SelectQuery query, EntityMetadata metadata) {
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

    CriteriaCondition includeInheritance(EntityMetadata metadata) {
        if (metadata.inheritance().isPresent()) {
            InheritanceMetadata inheritanceMetadata = metadata.inheritance().orElseThrow();
            if (!inheritanceMetadata.parent().equals(metadata.type())) {
                return CriteriaCondition.eq(Element.of(inheritanceMetadata.discriminatorColumn(),
                        inheritanceMetadata.discriminatorValue()));
            }
        }
        return null;
    }

    SelectQuery applyInheritance(SelectQuery query, RepositoryInvocationContext context) {
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

    private static List<String> columns(CommunicationObserverParser parser, ArrayList<String> columns,
                                        EntityMetadata entityMetadata) {
        if(columns.isEmpty()) {
            return columns;
        }
        return columns.stream().map(c -> parser.fireSelectField(entityMetadata.name(), c)).toList();
    }

    private static List<Sort<?>> sorts(CommunicationObserverParser parser,
                                          List<Sort<?>> sorts,
                                          EntityMetadata entityMetadata) {
        if(sorts.isEmpty()) {
            return sorts;
        }
        List<Sort<?>> updateSorts = new ArrayList<>(sorts.size());
        updateSorts.addAll(sorts.stream().map(sort -> {
            String attribute = parser.fireSortProperty(entityMetadata.name(), sort.property());
            return Sort.of(attribute, sort.isAscending() ? Direction.ASC : Direction.DESC, sort.ignoreCase());
        }).toList());
        return updateSorts;
    }
}
