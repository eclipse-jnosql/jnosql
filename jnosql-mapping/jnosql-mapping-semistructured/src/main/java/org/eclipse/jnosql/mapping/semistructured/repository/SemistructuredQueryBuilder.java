/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
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
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQueryParser;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.util.ParamsBinder;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.MappingDeleteQuery;
import org.eclipse.jnosql.mapping.semistructured.query.RepositorySemiStructuredObserverParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SemistructuredQueryBuilder {

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

    protected SemistructuredQueryBuilder() {
        this.converters = null;
        this.parsers = new ConcurrentHashMap<>();
        this.paramsBinderMap = new ConcurrentHashMap<>();
    }


    public SelectQuery selectQuery(RepositoryInvocationContext context) {
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
        return DynamicSelectQueryBuilder.includeInheritance(query, entityMetadata);
    }

    public DeleteQuery deleteQuery(RepositoryInvocationContext context) {
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

    public SelectQuery applyInheritance(SelectQuery query, RepositoryInvocationContext context) {
        return DynamicSelectQueryBuilder.applyInheritance(query, context);
    }

    public SelectQuery updateDynamicQuery(SelectQuery query, RepositoryInvocationContext context) {
        return DynamicSelectQueryBuilder.INSTANCE.updateDynamicQuery(query, context,
                observer(context.entityMetadata()),
                converters);
    }


    public CommunicationObserverParser observer(EntityMetadata entityMetadata) {
        Class<?> entityType = entityMetadata.type();
        return parsers.computeIfAbsent(entityType,key -> new RepositorySemiStructuredObserverParser(entityMetadata));
    }

    public ParamsBinder paramsBinder(EntityMetadata entityMetadata) {
        Class<?> entityType = entityMetadata.type();
        return paramsBinderMap.computeIfAbsent(entityType,key -> new ParamsBinder(entityMetadata, converters));
    }

    public DeleteQuery includeInheritance(DeleteQuery query, EntityMetadata metadata) {
        var condition = DynamicSelectQueryBuilder.includeInheritance(metadata);
        if (condition == null) {
            return query;
        }
        if (query.condition().isPresent()) {
            CriteriaCondition columnCondition = query.condition().orElseThrow();
            condition = condition.and(columnCondition);
        }
        return new MappingDeleteQuery(query.name(), condition);
    }

}
