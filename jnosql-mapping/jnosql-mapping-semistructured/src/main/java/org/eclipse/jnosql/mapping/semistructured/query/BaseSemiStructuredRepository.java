/*
 *  Copyright (c) 2022,2025 Contributors to the Eclipse Foundation
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
import jakarta.data.repository.First;
import jakarta.data.repository.Select;
import jakarta.data.restrict.Restriction;
import org.eclipse.jnosql.communication.Params;
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
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.core.query.AbstractRepositoryProxy;
import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;
import org.eclipse.jnosql.mapping.core.repository.SpecialParameters;
import org.eclipse.jnosql.mapping.core.util.ParamsBinder;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;
import org.eclipse.jnosql.mapping.semistructured.MappingQuery;
import org.eclipse.jnosql.mapping.semistructured.ProjectorConverter;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A base abstract class for implementing column-oriented repositories in a Java NoSQL database.
 * This class provides common functionality for executing CRUD operations using column queries.
 *
 * @param <T> The type of entities managed by the repository.
 */
public abstract class BaseSemiStructuredRepository<T, K> extends AbstractRepositoryProxy<T, K> {

    private static final SelectQueryParser SELECT_PARSER = new SelectQueryParser();

    private static final DeleteQueryParser DELETE_PARSER = new DeleteQueryParser();
    private static final Object[] EMPTY_PARAM = new Object[0];

    private CommunicationObserverParser parser;

    private ParamsBinder paramsBinder;

    private ProjectorConverter projectorConverter;


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
     * Retrieves the EntitiesMetadata instance containing metadata for all entities.
     *
     * @return The EntitiesMetadata instance.
     */
    protected abstract EntitiesMetadata entitiesMetadata();

    /**
     * Retrieves the ProjectorConverter instance for converting entities to projections.
     *
     * @return The ProjectorConverter instance.
     */
    protected ProjectorConverter projectorConverter() {
        if (Objects.isNull(projectorConverter)) {
            this.projectorConverter = new ProjectorConverter(entitiesMetadata());
        }
        return projectorConverter;
    }

    /**
     * Retrieves the SemistructuredTemplate instance for executing column queries.
     *
     * @return The SemistructuredTemplate instance.
     */
    protected abstract SemiStructuredTemplate template();

    protected SelectQuery query(Method method, Object[] args) {
        var provider = SelectMethodProvider.INSTANCE;
        var selectQuery = provider.apply(method, entityMetadata().name());
        var queryParams = SELECT_PARSER.apply(selectQuery, parser());
        var query = queryParams.query();
        var params = queryParams.params();
        var first = method.getAnnotation(First.class);
        paramsBinder().bind(params, args(args), method);
        return updateQueryDynamically(args(args), query, first);
    }

    private static Object[] args(Object[] args) {
        return args == null ? EMPTY_PARAM : args;
    }

    protected DeleteQuery deleteQuery(Method method, Object[] args) {
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
    protected Object executeFindByQuery(Method method, Object[] args, Class<?> typeClass, SelectQuery query) {
        DynamicReturn<?> dynamicReturn = DynamicReturn.builder()
                .classSource(typeClass)
                .methodSource(method)
                .result(() -> {
                    Stream<Object> select = template().select(query);
                    return select.map(mapper(method));
                })
                .singleResult(() -> {
                    Optional<Object> object = template().singleResult(query);
                    return object.map(mapper(method));
                })
                .pagination(DynamicReturn.findPageRequest(args))
                .streamPagination(streamPagination(query, method))
                .singleResultPagination(getSingleResult(query, method))
                .page(getPage(query, method))
                .build();
        return dynamicReturn.execute();
    }

    @SuppressWarnings("unchecked")
    protected <E> Function<Object, E> mapper(Method method) {
        return value -> {
            var returnType = returnType(method);
            Optional<ProjectionMetadata> projection = this.entitiesMetadata().projection(returnType);
            if (projection.isPresent()) {
                ProjectionMetadata projectionMetadata = projection.orElseThrow();
                return projectorConverter().map(value, projectionMetadata);
            }
            Select[] annotations = method.getAnnotationsByType(Select.class);
            if (annotations.length == 1) {
                String fieldReturn = annotations[0].value();
                Optional<EntityMetadata> valueEntityMetadata = entitiesMetadata().findByClassName(value.getClass().getName());
                return (E) valueEntityMetadata
                        .map(entityMetadata -> value(entityMetadata, fieldReturn, value))
                        .orElse(value);
            }
            return (E) value;
        };
    }

    private Class<?> returnType(Method method) {
        Class<?> typeClass = method.getReturnType();
        if (typeClass.isArray()) {
            return typeClass.getComponentType();
        } else if (Iterable.class.isAssignableFrom(typeClass) || Stream.class.isAssignableFrom(typeClass) || Optional.class.isAssignableFrom(typeClass)) {
            return (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
        }
        return typeClass;
    }

    private SelectQuery includeInheritance(SelectQuery query, First first) {
        EntityMetadata metadata = this.entityMetadata();
        if (metadata.inheritance().isPresent()) {
            InheritanceMetadata inheritanceMetadata = metadata.inheritance().orElseThrow();
            if (!inheritanceMetadata.parent().equals(metadata.type())) {
                CriteriaCondition condition = CriteriaCondition.eq(Element.of(inheritanceMetadata.discriminatorColumn(),
                        inheritanceMetadata.discriminatorValue()));
                if (query.condition().isPresent()) {
                    CriteriaCondition columnCondition = query.condition().orElseThrow();
                    condition = condition.and(columnCondition);
                }
                return new MappingQuery(query.sorts(), Optional.ofNullable(first)
                        .map(First::value)
                        .map(v -> (long) v)
                        .orElse(query.limit()), query.skip(),
                        condition, query.name(), query.columns());
            }
        }
        if(first != null){
            return new MappingQuery(query.sorts(), Optional.of(first.value()).map(v -> (long) v).orElse(query.limit()), query.skip(),
                    query.condition().orElse(null), query.name(), query.columns());
        }
        return query;
    }

    protected Long executeCountByQuery(SelectQuery query) {
        return template().count(query);
    }

    protected boolean executeExistsByQuery(SelectQuery query) {
        return template().exists(query);
    }

    protected Function<PageRequest, Page<T>> getPage(SelectQuery query, Method method) {
        return p -> {
            Stream<T> entities = template().select(query).map(mapper(method));
            return NoSQLPage.of(entities.toList(), p);
        };
    }

    protected Function<PageRequest, Optional<T>> getSingleResult(SelectQuery query, Method method) {
        return p -> template().singleResult(query).map(mapper(method));
    }

    protected Function<PageRequest, Stream<T>> streamPagination(SelectQuery query, Method method) {
        return p -> template().select(query).map(mapper(method));
    }


    protected SelectQuery updateQueryDynamically(Object[] args, SelectQuery query, First first) {

        var selectInheritance = includeInheritance(query, first);
        var special = DynamicReturn.findSpecialParameters(args, sortParser());

        if (special.isEmpty()) {
            return selectInheritance;
        }

        final SelectQuery selectQuery;
        if (special.restriction().isPresent()) {
            selectQuery = includeRestrictCondition(special, selectInheritance, first);
        } else {
            selectQuery = selectInheritance;
        }

        Optional<Limit> limit = special.limit();

        if (special.hasOnlySort()) {
            List<Sort<?>> sorts = new ArrayList<>();
            sorts.addAll(selectQuery.sorts());
            sorts.addAll(special.sorts());
            long skip = limit.map(l -> l.startAt() - 1).orElse(selectQuery.skip());
            long max;
            if (first != null) {
                max = first.value();
            } else {
                max = limit.map(Limit::maxResults).orElse((int) selectQuery.limit());
            }
            return new MappingQuery(sorts, max,
                    skip,
                    selectQuery.condition().orElse(null),
                    selectQuery.name(),
                    selectQuery.columns());
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
                    selectQuery.name(), selectQuery.columns());
        }

        return special.pageRequest().<SelectQuery>map(p -> {
            long size = p.size();
            long skip = NoSQLPage.skip(p);
            List<Sort<?>> sorts = selectQuery.sorts();
            if (!special.sorts().isEmpty()) {
                sorts = new ArrayList<>(selectQuery.sorts());
                sorts.addAll(special.sorts());
            }
            return new MappingQuery(sorts, size, skip,
                    selectQuery.condition().orElse(null), selectQuery.name(), selectQuery.columns());
        }).orElseGet(() -> {
            if (!special.sorts().isEmpty()) {
                List<Sort<?>> sorts = new ArrayList<>(selectQuery.sorts());
                sorts.addAll(special.sorts());
                return new MappingQuery(sorts, Optional.ofNullable(first).map(First::value)
                        .map(v -> (long)v).orElse(selectQuery.limit()), selectQuery.skip(),
                        selectQuery.condition().orElse(null), selectQuery.name(), selectQuery.columns());
            }
            return selectQuery;
        });
    }

    private SelectQuery includeRestrictCondition(SpecialParameters special, SelectQuery selectQuery, First first) {
        Restriction<?> restriction = special.restriction().orElseThrow();

        CriteriaCondition conditionConverted = RestrictionConverter.INSTANCE.parser(restriction,
                entityMetadata(), converters()).orElse(null);
        SelectQuery updateQuery = selectQuery;
        var limit = Optional.ofNullable(first).map(First::value).map(v -> (long) v).orElse(selectQuery.limit());
        if (conditionConverted != null) {
            var conditionOptional = selectQuery.condition();

            if (conditionOptional.isPresent()) {
                CriteriaCondition condition = conditionOptional.orElseThrow();
                updateQuery = new MappingQuery(selectQuery.sorts(), limit,
                        selectQuery.skip(), condition.and(conditionConverted), selectQuery.name(), selectQuery.columns());
            } else {
                updateQuery = new MappingQuery(selectQuery.sorts(), limit,
                        selectQuery.skip(), conditionConverted, selectQuery.name(), selectQuery.columns());
            }
        }
        return updateQuery;
    }

    protected Function<String, String> sortParser() {
        return property -> parser().fireSortProperty(entityMetadata().name(), property);
    }

    private Object value(EntityMetadata entityMetadata, String returnName, Object value) {
        var names = returnName.split("\\.");
        Optional<FieldMetadata> fieldMetadata = entityMetadata.fieldMapping(names[0]);
        if (fieldMetadata.isPresent()) {
            var field = fieldMetadata.orElseThrow();
            var convertedField = field.read(value);
            if (convertedField != null && names.length > 1) {
                var subField = Stream.of(names).skip(1).collect(Collectors.joining("."));
                var subEntityMetadata = entitiesMetadata().findByClassName(convertedField.getClass().getName())
                        .orElseThrow(() -> new IllegalArgumentException("Entity metadata not found for " + convertedField.getClass()));
                return value(subEntityMetadata, subField, convertedField);

            }
            return convertedField == null ? value : convertedField;
        }
        return value;
    }

}
