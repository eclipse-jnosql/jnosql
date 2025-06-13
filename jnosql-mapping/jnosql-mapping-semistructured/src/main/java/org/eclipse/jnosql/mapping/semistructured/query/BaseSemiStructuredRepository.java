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


import jakarta.data.Limit;
import jakarta.data.Sort;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.restrict.Restriction;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.query.method.DeleteMethodProvider;
import org.eclipse.jnosql.communication.query.method.SelectMethodProvider;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DeleteQueryParser;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQueryParser;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.core.query.AbstractRepositoryProxy;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.SpecialParameters;
import org.eclipse.jnosql.mapping.core.util.ParamsBinder;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.eclipse.jnosql.mapping.semistructured.MappingQuery;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A base abstract class for implementing column-oriented repositories in a Java NoSQL database.
 * This class provides common functionality for executing CRUD operations using column queries.
 *
 * @param <T> The type of entities managed by the repository.
 *
 */
public abstract class BaseSemiStructuredRepository<T, K> extends AbstractRepositoryProxy<T, K> {

    private static final SelectQueryParser SELECT_PARSER = new SelectQueryParser();

    private static final DeleteQueryParser DELETE_PARSER = new DeleteQueryParser();
    private static final Object[] EMPTY_PARAM = new Object[0];

    private CommunicationObserverParser parser;

    private ParamsBinder paramsBinder;


    /**
     * Retrieves the Converters instance responsible for converting data types.
     *
     * @return The Converters instance.
     */
    protected abstract Converters converters();

    /**
     * Retrieves the metadata information about the entity managed by the repository.
     *
     * @return The EntityMetadata instance.
     */
    @Override
    protected abstract EntityMetadata entityMetadata();

    /**
     * Retrieves the SemistructuredTemplate instance for executing column queries.
     *
     * @return The SemistructuredTemplate instance.
     */
    protected abstract SemiStructuredTemplate template();


    protected org.eclipse.jnosql.communication.semistructured.SelectQuery query(Method method, Object[] args) {
        var provider = SelectMethodProvider.INSTANCE;
        var selectQuery = provider.apply(method, entityMetadata().name());
        var queryParams = SELECT_PARSER.apply(selectQuery, parser());
        var query = queryParams.query();
        var params = queryParams.params();
        paramsBinder().bind(params, args(args), method);
        return updateQueryDynamically(args(args), query);
    }

    private static Object[] args(Object[] args) {
        return args == null ? EMPTY_PARAM : args;
    }

    protected org.eclipse.jnosql.communication.semistructured.DeleteQuery deleteQuery(Method method, Object[] args) {
        var deleteMethodFactory = DeleteMethodProvider.INSTANCE;
        var deleteQuery = deleteMethodFactory.apply(method, entityMetadata().name());
        var queryParams = DELETE_PARSER.apply(deleteQuery, parser());
        var query = queryParams.query();
        Params params = queryParams.params();
        paramsBinder().bind(params, args(args), method);
        return query;
    }

    /**
     * Retrieves the ColumnObserverParser instance for parsing column observations.
     *
     * @return The ColumnObserverParser instance.
     */

    protected CommunicationObserverParser parser() {
        if (parser == null) {
            this.parser = new RepositorySemiStructuredObserverParser(entityMetadata());
        }
        return parser;
    }

    /**
     * Retrieves the ParamsBinder instance for binding parameters to queries.
     *
     * @return The ParamsBinder instance.
     */
    protected ParamsBinder paramsBinder() {
        if (Objects.isNull(paramsBinder)) {
            this.paramsBinder = new ParamsBinder(entityMetadata(), converters());
        }
        return paramsBinder;
    }

    @SuppressWarnings("unchecked")
    protected Object executeFindByQuery(Method method, Object[] args, Class<?> typeClass, org.eclipse.jnosql.communication.semistructured.SelectQuery query) {
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .classSource(typeClass)
                .methodSource(method)
                .result(() -> template().select(query))
                .singleResult(() -> template().singleResult(query))
                .pagination(DynamicReturn.findPageRequest(args))
                .streamPagination(streamPagination(query))
                .singleResultPagination(getSingleResult(query))
                .page(getPage(query))
                .build();
        return dynamicReturn.execute();
    }

    private org.eclipse.jnosql.communication.semistructured.SelectQuery includeInheritance(org.eclipse.jnosql.communication.semistructured.SelectQuery query){
        EntityMetadata metadata = this.entityMetadata();
        if(metadata.inheritance().isPresent()){
            InheritanceMetadata inheritanceMetadata = metadata.inheritance().orElseThrow();
            if(!inheritanceMetadata.parent().equals(metadata.type())){
                CriteriaCondition condition = CriteriaCondition.eq(Element.of(inheritanceMetadata.discriminatorColumn(),
                        inheritanceMetadata.discriminatorValue()));
                if(query.condition().isPresent()){
                    CriteriaCondition columnCondition = query.condition().orElseThrow();
                    condition = condition.and(columnCondition);
                }
                return new MappingQuery(query.sorts(), query.limit(), query.skip(),
                        condition, query.name());
            }
        }
        return query;
    }

    protected Long executeCountByQuery(org.eclipse.jnosql.communication.semistructured.SelectQuery query) {
        return template().count(query);
    }

    protected boolean executeExistsByQuery(org.eclipse.jnosql.communication.semistructured.SelectQuery query) {
        return template().exists(query);
    }

    protected Function<PageRequest, Page<T>> getPage(org.eclipse.jnosql.communication.semistructured.SelectQuery query) {
        return p -> {
            Stream<T> entities = template().select(query);
            return NoSQLPage.of(entities.toList(), p);
        };
    }

    protected Function<PageRequest, Optional<T>> getSingleResult(org.eclipse.jnosql.communication.semistructured.SelectQuery query) {
        return p -> template().singleResult(query);
    }

    protected Function<PageRequest, Stream<T>> streamPagination(org.eclipse.jnosql.communication.semistructured.SelectQuery query) {
        return p -> template().select(query);
    }


    protected org.eclipse.jnosql.communication.semistructured.SelectQuery updateQueryDynamically(Object[] args,
                                                                                                 org.eclipse.jnosql.communication.semistructured.SelectQuery query) {
        var selectInheritance = includeInheritance(query);
        var special = DynamicReturn.findSpecialParameters(args, sortParser());

        if (special.isEmpty()) {
            return selectInheritance;
        }

        final SelectQuery selectQuery;
        if(special.isRestrictionValid(args)){
            selectQuery = includeRestrictCondition(special, selectInheritance);
        } else{
            selectQuery = selectInheritance;
        }

        Optional<Limit> limit = special.limit();

        if (special.hasOnlySort()) {
            List<Sort<?>> sorts = new ArrayList<>();
            sorts.addAll(selectQuery.sorts());
            sorts.addAll(special.sorts());
            long skip = limit.map(l -> l.startAt() - 1).orElse(selectQuery.skip());
            long max = limit.map(Limit::maxResults).orElse((int) selectQuery.limit());
            return new MappingQuery(sorts, max,
                    skip,
                    selectQuery.condition().orElse(null),
                    selectQuery.name());
        }

        if (limit.isPresent()) {
            long skip = limit.map(l -> l.startAt() - 1).orElse(selectQuery.skip());
            long max = limit.map(Limit::maxResults).orElse((int) selectQuery.limit());
            final List<Sort<?>> sorts = new ArrayList<>();
            sorts.addAll(selectQuery.sorts());
            sorts.addAll(special.sorts());
            return new MappingQuery(sorts, max,
                    skip,
                    selectQuery.condition().orElse(null),
                    selectQuery.name());
        }

        return special.pageRequest().<org.eclipse.jnosql.communication.semistructured.SelectQuery>map(p -> {
            long size = p.size();
            long skip = NoSQLPage.skip(p);
            List<Sort<?>> sorts = selectQuery.sorts();
            if (!special.sorts().isEmpty()) {
                sorts = new ArrayList<>(selectQuery.sorts());
                sorts.addAll(special.sorts());
            }
            return new MappingQuery(sorts, size, skip,
                    selectQuery.condition().orElse(null), selectQuery.name());
        }).orElseGet(() -> {
            if (!special.sorts().isEmpty()) {
                List<Sort<?>> sorts = new ArrayList<>(selectQuery.sorts());
                sorts.addAll(special.sorts());
                return new MappingQuery(sorts, selectQuery.limit(), selectQuery.skip(),
                        selectQuery.condition().orElse(null), selectQuery.name());
            }
           return selectQuery;
        });
    }

    private SelectQuery includeRestrictCondition(SpecialParameters special, SelectQuery selectQuery) {
        Restriction<?> restriction = special.restriction().orElseThrow();

        CriteriaCondition conditionConverted = RestrictionConverter.INSTANCE.parser(restriction,
                entityMetadata(), converters()).orElse(null);
        if (conditionConverted != null) {
            var conditionOptional = selectQuery.condition();
            if (conditionOptional.isPresent()) {
                CriteriaCondition condition = conditionOptional.orElseThrow();
                selectQuery = new MappingQuery(selectQuery.sorts(), selectQuery.limit(),
                        selectQuery.skip(), condition.and(conditionConverted), selectQuery.name());
            } else {
                selectQuery = new MappingQuery(selectQuery.sorts(), selectQuery.limit(),
                        selectQuery.skip(), conditionConverted, selectQuery.name());
            }
        }
        return selectQuery;
    }

    protected Function<String, String> sortParser() {
        return property -> parser().fireSortProperty(entityMetadata().name(), property);
    }


}
