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
package org.eclipse.jnosql.mapping.semistructured;


import jakarta.data.exceptions.NonUniqueResultException;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.page.impl.CursoredPageRecord;
import jakarta.nosql.Query;
import jakarta.nosql.QueryMapper;
import jakarta.nosql.TypedQuery;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.QueryParser;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.IdNotFoundException;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.core.config.MicroProfileSettings;
import org.eclipse.jnosql.mapping.core.util.ConverterUtil;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;
import static org.eclipse.jnosql.communication.Configurations.CURSOR_PAGINATION_MULTIPLE_SORTING;

/**
 * An abstract implementation of the {@link SemiStructuredTemplate} interface providing
 * a template method for working with semi-structured NoSQL databases.
 * Concrete subclasses must implement methods to provide necessary dependencies and configuration.
 *
 * @see SemiStructuredTemplate
 */
public abstract class AbstractSemiStructuredTemplate implements SemiStructuredTemplate {

    private static final Logger LOGGER = Logger.getLogger(AbstractSemiStructuredTemplate.class.getName());

    private static final QueryParser PARSER = new QueryParser();

    private final UnaryOperator<CommunicationEntity> insert = e -> manager().insert(e);

    private final UnaryOperator<CommunicationEntity> update = e -> manager().update(e);

    /**
     * Retrieves the converter used to convert between entity objects and communication entities.
     *
     * @return the entity converter
     */
    protected abstract EntityConverter converter();

    /**
     * Retrieves the manager responsible for database operations.
     *
     * @return the database manager
     */
    protected abstract DatabaseManager manager();

    /**
     * Retrieves the manager responsible for persisting column events.
     *
     * @return the event manager
     */
    protected abstract EventPersistManager eventManager();

    /**
     * Retrieves the metadata for entities.
     *
     * @return the entities metadata
     */
    protected abstract EntitiesMetadata entities();

    /**
     * Retrieves the converters for handling entity conversions.
     *
     * @return the converters
     */
    protected abstract Converters converters();

    @Override
    public <T> T insert(T entity) {
        requireNonNull(entity, "entity is required");
        return persist(entity, insert);
    }


    @Override
    public <T> T insert(T entity, Duration ttl) {
        requireNonNull(entity, "entity is required");
        requireNonNull(ttl, "ttl is required");
        return persist(entity, e -> manager().insert(e, ttl));
    }


    @Override
    public <T> T update(T entity) {
        requireNonNull(entity, "entity is required");
        return persist(entity, update);
    }

    @Override
    public <T> Iterable<T> update(Iterable<T> entities) {
        requireNonNull(entities, "entity is required");
        return StreamSupport.stream(entities.spliterator(), false)
                .map(this::update).collect(Collectors.toList());
    }

    @Override
    public <T> Iterable<T> insert(Iterable<T> entities) {
        requireNonNull(entities, "entities is required");
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
    public <T> void delete(T entity) {
        requireNonNull(entity, "entity is required");
        EntityMetadata metadata = entities().get(entity.getClass());
        FieldMetadata idField = metadata.id()
                .orElseThrow(() -> IdNotFoundException.newInstance(metadata.type()));

        var idValue = idField.read(entity);
        LOGGER.fine("Deleting entity: " + entity.getClass() + " with id: " + idValue);
        DeleteQuery query = DeleteQuery.delete().from(metadata.name())
                .where(converter().idFieldNameSupplier().defaultIdFieldName().orElseGet(idField::name))
                .eq(idValue).build();
        manager().delete(query);
    }

    @Override
    public <T> void delete(Iterable<? extends T> iterable) {
        requireNonNull(iterable, "iterable is required");
        StreamSupport.stream(iterable.spliterator(), false)
                .forEach(this::delete);
    }

    @Override
    public void delete(DeleteQuery query) {
        requireNonNull(query, "query is required");
        manager().delete(query);
    }

    @Override
    public long count(SelectQuery query) {
        return manager().count(query);
    }

    @Override
    public boolean exists(SelectQuery query) {
        return manager().exists(query);
    }

    @Override
    public <T> Optional<T> singleResult(SelectQuery query) {
        requireNonNull(query, "query is required");
        final Stream<T> select = select(query);

        final Iterator<T> iterator = select.iterator();

        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        final T entity = iterator.next();

        if (!iterator.hasNext()) {
            return Optional.of(entity);
        }
        throw new NonUniqueResultException("No Unique result found to the query: " + query);
    }

    @Override
    public <T, K> Optional<T> find(Class<T> type, K id) {
        requireNonNull(type, "type is required");
        requireNonNull(id, "id is required");
        EntityMetadata entityMetadata = entities().get(type);
        FieldMetadata idField = entityMetadata.id()
                .orElseThrow(() -> IdNotFoundException.newInstance(type));

        Object value = ConverterUtil.getValue(id, entityMetadata, idField.fieldName(), converters());
        return this.select(type)
                .where(converter().idFieldNameSupplier().defaultIdFieldName().orElseGet(idField::name))
                .eq(value).singleResult();
    }

    @Override
    public <T, K> void delete(Class<T> type, K id) {
        requireNonNull(type, "type is required");
        requireNonNull(id, "id is required");

        EntityMetadata entityMetadata = entities().get(type);
        FieldMetadata idField = entityMetadata.id()
                .orElseThrow(() -> IdNotFoundException.newInstance(type));
        Object value = ConverterUtil.getValue(id, entityMetadata, idField.fieldName(), converters());

        this.delete(type)
                .where(converter().idFieldNameSupplier().defaultIdFieldName().orElseGet(idField::name))
                .eq(value)
                .execute();
    }

    @Override
    public org.eclipse.jnosql.mapping.PreparedStatement prepare(String query) {
        var observer = observer();
        return new PreparedStatement(PARSER.prepare(query, null, manager(), observer), converter(), observer, entities());
    }

    @Override
    public org.eclipse.jnosql.mapping.PreparedStatement prepare(String query, String entity) {
        var observer = observer();
        return new PreparedStatement(PARSER.prepare(query, entity, manager(), observer), converter(), observer, entities());
    }

    @Override
    public <T> Stream<T> select(SelectQuery query) {
        requireNonNull(query, "query is required");
        return executeQuery(query);
    }

    @Override
    public long count(String entity) {
        return manager().count(entity);
    }


    @Override
    public <T> long count(Class<T> type) {
        requireNonNull(type, "entity class is required");
        return manager().count(findAllQuery(type));
    }

    private <T> Stream<T> executeQuery(SelectQuery query) {
        requireNonNull(query, "query is required");
        Stream<CommunicationEntity> entities = manager().select(query);
        Function<CommunicationEntity, T> function = e -> converter().toEntity(e);
        return entities.map(function);
    }

    @Override
    public <T> QueryMapper.MapperFrom select(Class<T> type) {
        requireNonNull(type, "type is required");
        EntityMetadata metadata = entities().get(type);
        return new MapperSelect(metadata, converters(), this);
    }

    @Override
    public <T> QueryMapper.MapperDeleteFrom delete(Class<T> type) {
        requireNonNull(type, "type is required");
        EntityMetadata metadata = entities().get(type);
        return new MapperDelete(metadata, converters(), this);
    }

    @Override
    public <T> Stream<T> findAll(Class<T> type) {
        requireNonNull(type, "type is required");
        return select(findAllQuery(type));
    }

    @Override
    public <T> void deleteAll(Class<T> type) {
        requireNonNull(type, "type is required");
        EntityMetadata metadata = entities().get(type);
        if(metadata.inheritance().isPresent()){
            InheritanceMetadata inheritanceMetadata = metadata.inheritance().orElseThrow();
            if(!inheritanceMetadata.parent().equals(metadata.type())){
                manager().delete(DeleteQuery.delete().from(metadata.name())
                        .where(inheritanceMetadata.discriminatorColumn())
                        .eq(inheritanceMetadata.discriminatorValue()).build());
                return;
            }
        }
        manager().delete(DeleteQuery.delete().from(metadata.name()).build());
    }

    @Override
    public <T> CursoredPage<T> selectCursor(SelectQuery query, PageRequest pageRequest){
        requireNonNull(query, "query is required");
        requireNonNull(pageRequest, "pageRequest is required");
        LOGGER.finest(() -> "Executing query: " + query);
        var enableMultipleSorting = MicroProfileSettings.INSTANCE.get(CURSOR_PAGINATION_MULTIPLE_SORTING, Boolean.class)
                .orElse(false);
        LOGGER.finest(() -> "Cursor pagination with multiple sorting is enabled: " + enableMultipleSorting);

        if (!enableMultipleSorting && query.sorts().size() > 1) {
            throw new UnsupportedOperationException("Cursor pagination with multiple sorting is not supported, " +
                    "enable it by setting the property " + CURSOR_PAGINATION_MULTIPLE_SORTING.get() + " to true");
        }
        CursoredPage<CommunicationEntity> cursoredPage = this.manager().selectCursor(query, pageRequest);
        List<T> entities = cursoredPage.stream().<T>map(c -> converter().toEntity(c)).toList();
        PageRequest nextPageRequest = cursoredPage.hasNext()? cursoredPage.nextPageRequest() : null;
        PageRequest beforePageRequest = cursoredPage.hasPrevious()? cursoredPage.previousPageRequest() : null;
        List<PageRequest.Cursor> cursors = ((CursoredPageRecord<CommunicationEntity>) cursoredPage).cursors();
        return new CursoredPageRecord<>(entities, cursors, -1, pageRequest, nextPageRequest, beforePageRequest);
    }

    @Override
    public <T> Page<T> selectOffSet(SelectQuery query, PageRequest pageRequest) {
        requireNonNull(query, "query is required");
        requireNonNull(pageRequest, "pageRequest is required");
        var queryPage = new MappingQuery(query.sorts(), pageRequest.size(), NoSQLPage.skip(pageRequest),
                query.condition().orElse(null), query.name(), query.columns());
        Stream<T> result = select(queryPage);
        return NoSQLPage.of(result.toList(), pageRequest);
    }

    @Override
    public Query query(String query) {
        requireNonNull(query, "query is required");
        PreparedStatement preparedStatement = (PreparedStatement) this.prepare(query);
       return SemistructuredQuery.of(query, preparedStatement);
    }

    @Override
    public <T> TypedQuery<T> typedQuery(String query, Class<T> type) {
        requireNonNull(query, "query is required");
        requireNonNull(type, "type is required");
        var entityData = entities().findByClassName(type.getName());
        var projector = entities().projection(type);
        var entityName = entityName(type, projector, entityData);
        var preparedStatement = (PreparedStatement) this.prepare(query, entityName);
        return SemistructuredTypedQuery.of(query, preparedStatement, this, projector.orElse(null));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private <T> String entityName(Class<T> type, Optional<ProjectionMetadata> projection, Optional<EntityMetadata> entityData) {
        if(projection.isPresent()){
            return projection.map(ProjectionMetadata::from)
                    .map(e -> entities().findByClassName(e.getName()))
                    .flatMap(Function.identity()).map(EntityMetadata::name).orElse(null);

        } else{
            return entityData.map(EntityMetadata::name).orElseThrow(() -> new IllegalArgumentException("There " +
                    "is no entity " + type.getName()));
        }
    }

    protected <T> T persist(T entity, UnaryOperator<CommunicationEntity> persistAction) {
        return Stream.of(entity)
                .map(toUnary(eventManager()::firePreEntity))
                .map(converter()::toCommunication)
                .map(persistAction)
                .map(t -> converter().toEntity(entity, t))
                .map(toUnary(eventManager()::firePostEntity))
                .findFirst()
                .orElseThrow();
    }

    private <T> UnaryOperator<T> toUnary(Consumer<T> consumer) {
        return t -> {
            consumer.accept(t);
            return t;
        };
    }

    private <T> SelectQuery findAllQuery(Class<T> type){
        EntityMetadata metadata = entities().get(type);

        if(metadata.inheritance().isPresent()){
            InheritanceMetadata inheritanceMetadata = metadata.inheritance().orElseThrow();
            if(!inheritanceMetadata.parent().equals(metadata.type())){
                return SelectQuery.select().from(metadata.name())
                        .where(inheritanceMetadata.discriminatorColumn()).eq(inheritanceMetadata.discriminatorValue()).build();
            }
        }
        return SelectQuery.select().from(metadata.name()).build();
    }

    private MapperObserver observer() {
        return new MapperObserver(entities());
    }


}
