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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.query.method.DeleteMethodProvider;
import org.eclipse.jnosql.communication.query.method.SelectMethodProvider;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.DeleteQueryParser;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQueryParser;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.util.ParamsBinder;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.MappingDeleteQuery;
import org.eclipse.jnosql.mapping.semistructured.MappingQuery;
import org.eclipse.jnosql.mapping.semistructured.query.RepositorySemiStructuredObserverParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
class SemistructuredQueryBuilder {

    private static final SelectQueryParser SELECT_PARSER = new SelectQueryParser();

    private static final DeleteQueryParser DELETE_PARSER = new DeleteQueryParser();
    private final Map<Class<?>, CommunicationObserverParser> parsers;
    private final Map<Class<?>, ParamsBinder> paramsBinderMap;

    private final Converters converters;

    @Inject
    SemistructuredQueryBuilder(Converters converters) {
        this.converters = converters;
        this.parsers = new ConcurrentHashMap<>();
        this.paramsBinderMap = new ConcurrentHashMap<>();
    }

    SemistructuredQueryBuilder() {
        this.converters = null;
        this.parsers = new ConcurrentHashMap<>();
        this.paramsBinderMap = new ConcurrentHashMap<>();
    }


    SelectQuery selectQuery(RepositoryInvocationContext context) {
        var method = context.method();
        var entityMetadata = context.entityMetadata();
        var parameters = context.parameters();
        var provider = SelectMethodProvider.INSTANCE;
        var selectQuery = provider.apply(method.name(), entityMetadata.name());
        var observer = this.observer(entityMetadata);
        var queryParams = SELECT_PARSER.apply(selectQuery, observer);
        var query = queryParams.query();
        var params = queryParams.params();
        var paramsBinder = this.paramsBinder(entityMetadata);
        paramsBinder.bind(params, parameters, method.name());
        return includeInheritance(query, entityMetadata);
    }

    DeleteQuery deleteQuery(RepositoryInvocationContext context) {
        var entityMetadata = context.entityMetadata();
        var provider = DeleteMethodProvider.INSTANCE;
        var method = context.method();
        var deleteQuery = provider.apply(method.name(), entityMetadata.name());
        var queryParams = DELETE_PARSER.apply(deleteQuery, observer(entityMetadata));
        var params = queryParams.params();
        var parameters = context.parameters();
        var query = queryParams.query();
        var paramsBinder = this.paramsBinder(entityMetadata);
        paramsBinder.bind(params, parameters, method.name());
        return includeInheritance(query, entityMetadata);
    }

    SelectQuery updateQuery(SelectQuery query, RepositoryInvocationContext context) {
        var entityMetadata = context.entityMetadata();
        return includeInheritance(query, entityMetadata);
    }


    private CommunicationObserverParser observer(EntityMetadata entityMetadata) {
        Class<?> entityType = entityMetadata.type();
        return parsers.computeIfAbsent(entityType,key -> new RepositorySemiStructuredObserverParser(entityMetadata));
    }

    private ParamsBinder paramsBinder(EntityMetadata entityMetadata) {
        Class<?> entityType = entityMetadata.type();
        return paramsBinderMap.computeIfAbsent(entityType,key -> new ParamsBinder(entityMetadata, converters));
    }

    private SelectQuery includeInheritance(SelectQuery query, EntityMetadata metadata) {
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

    private DeleteQuery includeInheritance(DeleteQuery query, EntityMetadata metadata) {
        var condition = includeInheritance(metadata);
        if (condition == null) {
            return query;
        }
        if (query.condition().isPresent()) {
            CriteriaCondition columnCondition = query.condition().orElseThrow();
            condition = condition.and(columnCondition);
        }
        return new MappingDeleteQuery( query.name(), condition);
    }

    private CriteriaCondition includeInheritance(EntityMetadata metadata) {
        if (metadata.inheritance().isPresent()) {
            InheritanceMetadata inheritanceMetadata = metadata.inheritance().orElseThrow();
            if (!inheritanceMetadata.parent().equals(metadata.type())) {
                return CriteriaCondition.eq(Element.of(inheritanceMetadata.discriminatorColumn(),
                        inheritanceMetadata.discriminatorValue()));
            }
        }
        return null;
    }


}
