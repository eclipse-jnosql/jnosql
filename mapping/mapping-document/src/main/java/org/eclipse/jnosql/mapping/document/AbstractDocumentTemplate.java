/*
 *  Copyright (c) 2017 Otávio Santana and others
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
package org.eclipse.jnosql.mapping.document;


import jakarta.nosql.NonUniqueResultException;
import jakarta.nosql.ServiceLoaderProvider;
import jakarta.nosql.criteria.CriteriaQuery;
import jakarta.nosql.criteria.CriteriaQueryResult;
import jakarta.nosql.criteria.ExecutableQuery;
import jakarta.nosql.document.DocumentCollectionManager;
import jakarta.nosql.document.DocumentDeleteQuery;
import jakarta.nosql.document.DocumentEntity;
import jakarta.nosql.document.DocumentObserverParser;
import jakarta.nosql.document.DocumentQuery;
import jakarta.nosql.document.DocumentQueryParser;
import jakarta.nosql.mapping.Converters;
import jakarta.nosql.mapping.IdNotFoundException;
import jakarta.nosql.mapping.Page;
import jakarta.nosql.mapping.PreparedStatement;
import jakarta.nosql.mapping.document.DocumentEntityConverter;
import jakarta.nosql.mapping.document.DocumentEventPersistManager;
import jakarta.nosql.mapping.document.DocumentQueryPagination;
import jakarta.nosql.mapping.document.DocumentTemplate;
import jakarta.nosql.mapping.document.DocumentWorkflow;
import org.eclipse.jnosql.mapping.reflection.ClassMapping;
import org.eclipse.jnosql.mapping.reflection.ClassMappings;
import org.eclipse.jnosql.mapping.reflection.FieldMapping;
import org.eclipse.jnosql.mapping.util.ConverterUtil;

import java.time.Duration;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import org.eclipse.jnosql.communication.criteria.DefaultCriteriaQuery;

/**
 * This class provides a skeletal implementation of the {@link DocumentTemplate} interface,
 * to minimize the effort required to implement this interface.
 */
public abstract class AbstractDocumentTemplate implements DocumentTemplate {


    private static final DocumentQueryParser PARSER = ServiceLoaderProvider.get(DocumentQueryParser.class);

    protected abstract DocumentEntityConverter getConverter();

    protected abstract DocumentCollectionManager getManager();

    protected abstract DocumentWorkflow getWorkflow();

    protected abstract DocumentEventPersistManager getPersistManager();

    protected abstract ClassMappings getClassMappings();

    protected abstract Converters getConverters();

    private final UnaryOperator<DocumentEntity> insert = e -> getManager().insert(e);

    private final UnaryOperator<DocumentEntity> update = e -> getManager().update(e);

    private DocumentObserverParser columnQueryParser;


    private DocumentObserverParser getObserver() {
        if (Objects.isNull(columnQueryParser)) {
            columnQueryParser = new DocumentMapperObserver(getClassMappings());
        }
        return columnQueryParser;
    }

    @Override
    public <T> T insert(T entity) {
        requireNonNull(entity, "entity is required");
        return getWorkflow().flow(entity, insert);
    }


    @Override
    public <T> T insert(T entity, Duration ttl) {
        requireNonNull(entity, "entity is required");
        requireNonNull(ttl, "ttl is required");
        return getWorkflow().flow(entity, e -> getManager().insert(e, ttl));
    }

    @Override
    public <T> Iterable<T> insert(Iterable<T> entities) {
        requireNonNull(entities, "entity is required");
        return StreamSupport.stream(entities.spliterator(), false)
                .map(this::insert).collect(Collectors.toList());
    }

    @Override
    public <T> Iterable<T> insert(Iterable<T> entities, Duration ttl) {
        requireNonNull(entities, "entities is required");
        requireNonNull(ttl, "ttl is required");
        return StreamSupport.stream(entities.spliterator(), false)
                .map(e -> insert(e, ttl))
                .collect(Collectors.toList());
    }

    @Override
    public <T> T update(T entity) {
        requireNonNull(entity, "entity is required");
        return getWorkflow().flow(entity, update);
    }

    @Override
    public <T> Iterable<T> update(Iterable<T> entities) {
        requireNonNull(entities, "entity is required");
        return StreamSupport.stream(entities.spliterator(), false)
                .map(this::update).collect(Collectors.toList());
    }

    @Override
    public void delete(DocumentDeleteQuery query) {
        requireNonNull(query, "query is required");
        getPersistManager().firePreDeleteQuery(query);
        getManager().delete(query);
    }

    @Override
    public <T> Stream<T> select(DocumentQuery query) {
        Objects.requireNonNull(query, "query is required");
        return executeQuery(query);
    }

    @Override
    public <T> Page<T> select(DocumentQueryPagination query) {
        Objects.requireNonNull(query, "query is required");
        Stream<T> entities = executeQuery(query);
        return new DocumentPage<>(this, entities, query);
    }


    @Override
    public <T> Optional<T> singleResult(DocumentQuery query) {
        Objects.requireNonNull(query, "query is required");
        final Stream<T> entities = select(query);
        final Iterator<T> iterator = entities.iterator();
        if(!iterator.hasNext()) {
            return Optional.empty();
        }
        final T entity = iterator.next();
        if(!iterator.hasNext()) {
            return Optional.of(entity);
        }
        throw new NonUniqueResultException("No unique result found to the query: " + query);
    }

    @Override
    public <T, K> Optional<T> find(Class<T> entityClass, K id) {
        requireNonNull(entityClass, "entityClass is required");
        requireNonNull(id, "id is required");
        ClassMapping classMapping = getClassMappings().get(entityClass);
        FieldMapping idField = classMapping.getId()
                .orElseThrow(() -> IdNotFoundException.newInstance(entityClass));

        Object value = ConverterUtil.getValue(id, classMapping, idField.getFieldName(), getConverters());
        DocumentQuery query = DocumentQuery.select().from(classMapping.getName())
                .where(idField.getName()).eq(value).build();

        return singleResult(query);
    }

    @Override
    public <T, K> void delete(Class<T> entityClass, K id) {
        requireNonNull(entityClass, "entityClass is required");
        requireNonNull(id, "id is required");

        ClassMapping classMapping = getClassMappings().get(entityClass);
        FieldMapping idField = classMapping.getId()
                .orElseThrow(() -> IdNotFoundException.newInstance(entityClass));

        Object value = ConverterUtil.getValue(id, classMapping, idField.getFieldName(), getConverters());
        DocumentDeleteQuery query = DocumentDeleteQuery.delete().from(classMapping.getName())
                .where(idField.getName()).eq(value).build();

        delete(query);
    }

    @Override
    public <T> Stream<T> query(String query) {
        requireNonNull(query, "query is required");
        return PARSER.query(query, getManager(), getObserver()).map(c -> (T) getConverter().toEntity(c));
    }

    @Override
    public <T> Optional<T> singleResult(String query) {
        requireNonNull(query, "query is required");
        Stream<T> entities = query(query);
        final Iterator<T> iterator = entities.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        final T entity = iterator.next();
        if (!iterator.hasNext()) {
            return Optional.of(entity);
        }
        throw new NonUniqueResultException("No unique result found to the query: " + query);
    }

    @Override
    public PreparedStatement prepare(String query) {
        return new DocumentPreparedStatement(PARSER.prepare(query, getManager(), getObserver()), getConverter());
    }


    @Override
    public long count(String documentCollection) {
        return getManager().count(documentCollection);
    }

    public <T> long count(Class<T> entityClass) {
        requireNonNull(entityClass, "entityClass is required");
        ClassMapping classMapping = getClassMappings().get(entityClass);
        return getManager().count(classMapping.getName());
    }

    private <T> Stream<T> executeQuery(DocumentQuery query) {
        requireNonNull(query, "query is required");
        getPersistManager().firePreQuery(query);
        Stream<DocumentEntity> entities = getManager().select(query);
        Function<DocumentEntity, T> function = e -> getConverter().toEntity(e);
        return entities.map(function);
    }

    @Override
    public <T extends Object> CriteriaQuery<T> createQuery(Class<T> type) {
        return new DefaultCriteriaQuery<>(type);
    }
    
    @Override
    public <T extends Object, R extends CriteriaQueryResult<T>> R executeQuery(ExecutableQuery<T, R> criteriaQuery) {
        requireNonNull(criteriaQuery, "query is required");
        getManager().executeQuery(criteriaQuery);
        return null;
    }
    

}
