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
import org.eclipse.jnosql.communication.query.method.SelectMethodProvider;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQueryParser;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.util.ParamsBinder;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.query.RepositorySemiStructuredObserverParser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
class SemistructuredOperation {

    private static final SelectQueryParser SELECT_PARSER = new SelectQueryParser();
    private final Map<Class<?>, CommunicationObserverParser> parsers;
    private final Map<Class<?>, ParamsBinder> paramsBinderMap;

    @Inject
    private Converters converters;
    public SemistructuredOperation() {
        this.parsers = new ConcurrentHashMap<>();
        this.paramsBinderMap = new ConcurrentHashMap<>();
    }

    CommunicationObserverParser observer(EntityMetadata entityMetadata) {
        Class<?> entityType = entityMetadata.type();
        return parsers.computeIfAbsent(entityType,key -> new RepositorySemiStructuredObserverParser(entityMetadata));
    }

    ParamsBinder paramsBinder(EntityMetadata entityMetadata) {
        Class<?> entityType = entityMetadata.type();
        return paramsBinderMap.computeIfAbsent(entityType,key -> new ParamsBinder(entityMetadata, converters));
    }

    SelectQueryParser selectParser() {
        return SELECT_PARSER;
    }

    public SelectQuery selectQuery(RepositoryInvocationContext context) {
        var method = context.method();
        var entityMetadata = context.entityMetadata();
        var parameters = context.parameters();
        var provider = SelectMethodProvider.INSTANCE;
        var selectQuery = provider.apply(method.name(), entityMetadata.name());
        var observer = this.observer(entityMetadata);
        var queryParams = this.selectParser().apply(selectQuery, observer);
        var query = queryParams.query();
        var params = queryParams.params();
        var paramsBinder = this.paramsBinder(entityMetadata);
        paramsBinder.bind(params, parameters, method.name());
        return query;
    }

}
