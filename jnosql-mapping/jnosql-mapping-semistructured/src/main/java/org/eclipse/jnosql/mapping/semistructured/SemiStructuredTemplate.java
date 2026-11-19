/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors
 *
 *   Otavio Santana
 *   Maximillian Arruda
 * */
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.nosql.Template;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.UpdateQuery;
import org.eclipse.jnosql.mapping.PreparedStatement;

import java.util.Optional;
import java.util.stream.Stream;



/**
 * Template for semi-structured databases (document, column and graph).
 * It extends {@link Template} with {@link SelectQuery}, {@link DeleteQuery} and {@link UpdateQuery}
 * execution, counting, pagination and text queries through {@link PreparedStatement}.
 * Inject a model-specific template such as {@code DocumentTemplate} or {@code ColumnTemplate},
 * or qualify the injection point with {@link org.eclipse.jnosql.mapping.Database}.
 *
 * <pre>{@code
 * @Inject
 * @Database(DatabaseType.DOCUMENT)
 * SemiStructuredTemplate template;
 *
 * SelectQuery query = SelectQuery.select().from("Person").where("name").eq("Ada").build();
 * Stream<Person> people = template.select(query);
 * }</pre>
 */
public interface SemiStructuredTemplate extends Template {

    /**
     * Returns the number of elements stored under the given entity name.
     *
     * <pre>{@code
     * long people = template.count("Person");
     * }</pre>
     *
     * @param entity the entity (column family, collection) name
     * @return the number of elements
     * @throws NullPointerException          if the entity name is null
     * @throws UnsupportedOperationException if the database does not support this operation
     */
    long count(String entity);

    /**
     * Returns the number of stored entities of the given mapped type.
     *
     * <pre>{@code
     * long people = template.count(Person.class);
     * }</pre>
     *
     * @param <T>  the entity type
     * @param type the mapped entity class
     * @return the number of elements
     * @throws NullPointerException          if the entity type is null
     * @throws UnsupportedOperationException if the database does not support this operation
     */
    <T> long count(Class<T> type);

    /**
     * Creates a {@link PreparedStatement} from a text query. Bind the named parameters, then read the result.
     *
     * <p>The query syntax is specific to each provider and may vary between implementations and NoSQL providers.</p>
     *
     * <pre>{@code
     * Stream<Person> adults = template.prepare("FROM Person WHERE age >= :age")
     *         .bind("age", 18)
     *         .result();
     * }</pre>
     *
     * @param query the query text
     * @return a {@link PreparedStatement} instance
     * @throws NullPointerException          if the query is null
     * @throws UnsupportedOperationException if the provider does not support query by text
     */
    PreparedStatement prepare(String query);

    /**
     * Creates a {@link PreparedStatement} from a text query that does not name its entity, using the given
     * entity name instead.
     *
     * <pre>{@code
     * long count = template.prepare("SELECT COUNT(1) WHERE age >= :age", "Person")
     *         .bind("age", 18)
     *         .count();
     * }</pre>
     *
     * <p>The query syntax is specific to each provider and may vary between implementations and NoSQL providers.</p>
     *
     * @param query  the query text
     * @param entity the name of the entity
     * @return a {@link PreparedStatement} instance
     * @throws NullPointerException          if the query or entity is null
     * @throws UnsupportedOperationException if the provider does not support query by text
     */
    PreparedStatement prepare(String query, String entity);
    /**
     * Deletes the entities that match the query.
     *
     * <pre>{@code
     * template.delete(DeleteQuery.delete().from("Person").where("name").eq("Ada").build());
     * }</pre>
     *
     * @param query the delete query
     * @throws NullPointerException when query is null
     */
    void delete(DeleteQuery query);

    /**
     * Updates the entities that match the query, without loading them.
     *
     * <p>Applications usually build such updates with the fluent {@code update(Class)} API inherited from
     * {@link Template}; this method accepts an already built {@link UpdateQuery}.</p>
     *
     * @param query the update query
     * @throws NullPointerException when query is null
     */
    void update(UpdateQuery query);

    /**
     * Executes the query and converts each result to a mapped entity.
     *
     * <pre>{@code
     * Stream<Person> people = template.select(SelectQuery.select().from("Person").where("name").eq("Ada").build());
     * }</pre>
     *
     * @param query the select query
     * @param <T>   the entity type
     * @return the matching entities; an empty stream when nothing matches
     * @throws NullPointerException when query is null
     */
    <T> Stream<T> select(SelectQuery query);

    /**
     * Returns the number of entities that match the query.
     *
     * <pre>{@code
     * long total = template.count(SelectQuery.select().from("Person").where("age").gte(18).build());
     * }</pre>
     *
     * @param query the select query
     * @return the number of matching entities
     */
    long count(SelectQuery query);

    /**
     * Checks whether at least one entity matches the query.
     *
     * <pre>{@code
     * boolean taken = template.exists(SelectQuery.select().from("Person").where("name").eq("Ada").build());
     * }</pre>
     *
     * @param query the select query
     * @return true if an entity matches the query, false otherwise
     */
    boolean exists(SelectQuery query);

    /**
     * Executes the query expecting at most one entity.
     *
     * <pre>{@code
     * Optional<Person> ada = template.singleResult(
     *         SelectQuery.select().from("Person").where("name").eq("Ada").build());
     * }</pre>
     *
     * @param query the select query
     * @param <T>   the entity type
     * @return the entity, or {@link Optional#empty()} when nothing matches
     * @throws NullPointerException                             when query is null
     * @throws jakarta.data.exceptions.NonUniqueResultException when more than one entity matches
     */
    <T> Optional<T> singleResult(SelectQuery query);

    /**
     * Returns all stored entities of the given type.
     *
     * <pre>{@code
     * Stream<Person> everyone = template.findAll(Person.class);
     * }</pre>
     *
     * @param type the entity type
     * @param <T>  the entity type
     * @return the entities; an empty stream when there are none
     * @throws NullPointerException when type is null
     */
    <T> Stream<T> findAll(Class<T> type);

    /**
     * Deletes all stored entities of the given type. For a subtype in an
     * {@link jakarta.nosql.Inheritance} hierarchy, only the entities with that subtype's discriminator value
     * are deleted.
     *
     * <pre>{@code
     * template.deleteAll(Person.class);
     * }</pre>
     *
     * @param type the entity type
     * @param <T>  the entity type
     * @throws NullPointerException when type is null
     */
    <T> void deleteAll(Class<T> type);

    /**
     * Select entities using pagination with cursor-based paging.
     *
     * <p>This method retrieves entities based on cursor-based paging, where the cursor acts as a bookmark for the next or previous page of results.
     * The method strictly supports cursor-based pagination and does not handle offset-based pagination. If the provided {@link PageRequest} is
     * in {@link jakarta.data.page.PageRequest.Mode#OFFSET}, this method should not be used; instead, use {@link #selectOffSet} for offset-based
     * pagination.</p>
     *
     * <p>The {@link SelectQuery} parameter will be overwritten based on the {@link PageRequest}, specifically using the cursor information to
     * adjust the query condition accordingly. This method ignores the skip value in {@link PageRequest} since skip is not applicable in cursor-based
     * pagination.</p>
     *
     * <p>For cursor-based pagination, at least one sort field must be specified in the {@link SelectQuery} order clause; otherwise, an
     * {@link IllegalArgumentException} will be thrown.</p>
     *
     * <p>By default, multiple sorting is disabled due to the behavior of NoSQL databases. In NoSQL systems, sorting by multiple fields can result
     * in unpredictable or inconsistent results, particularly when those fields contain duplicate values. Relational databases are more deterministic
     * in their sorting algorithms, but NoSQL systems such as MongoDB may return results in varying order if there is no unique field, such as `_id`,
     * to break ties. This behavior makes it difficult to guarantee stable pagination across requests.</p>
     *
     * <p>To enable multiple sorting, set the property <b>org.eclipse.jnosql.pagination.cursor=true</b>. For more details, refer to
     * {@link org.eclipse.jnosql.communication.Configurations#CURSOR_PAGINATION_MULTIPLE_SORTING}.</p>
     *
     * <pre>{@code
     * SelectQuery query = SelectQuery.select().from("Person").orderBy("name").asc().build();
     * CursoredPage<Person> page = template.selectCursor(query, PageRequest.ofSize(10));
     * if (page.hasNext()) {
     *     CursoredPage<Person> next = template.selectCursor(query, page.nextPageRequest());
     * }
     * }</pre>
     *
     * @param query         the query to retrieve entities
     * @param pageRequest   the page request defining the cursor-based paging
     * @param <T>           the entity type
     * @return a {@link CursoredPage} instance containing the entities within the specified page
     * @throws NullPointerException     if the query or pageRequest is null
     * @throws UnsupportedOperationException if the query has more than one sort and multiple sorting is not enabled
     * @throws IllegalArgumentException if cursor-based pagination is used without any sort field specified or if the cursor size does not match
     *                                  the sort size
     */
    <T> CursoredPage<T> selectCursor(SelectQuery query, PageRequest pageRequest);

    /**
     * Select entities using pagination with offset-based paging.
     *
     * <p>This method retrieves entities using traditional offset-based pagination. The results are determined by the offset and limit values
     * specified in the provided {@link PageRequest}. This method is suitable when you want to paginate through a result set using an explicit
     * starting point (offset) and a defined page size.</p>
     *
     * <p>The {@link SelectQuery} may be modified based on the provided {@link PageRequest}, specifically using the offset and limit to adjust
     * the query accordingly. Unlike cursor-based pagination, the offset value in {@link PageRequest} is utilized to skip the specified number
     * of rows before retrieving the next set of results.</p>
     *
     * <p>It is important to note that offset-based pagination may have performance implications on large datasets because it requires the database
     * to scan and count a potentially large number of rows before returning the requested page.</p>
     *
     * <pre>{@code
     * SelectQuery query = SelectQuery.select().from("Person").orderBy("name").asc().build();
     * Page<Person> page = template.selectOffSet(query, PageRequest.ofPage(2).size(10));
     * List<Person> people = page.content();
     * }</pre>
     *
     * @param query         the query to retrieve entities
     * @param pageRequest   the page request defining the offset-based paging, including the offset and page size
     * @param <T>           the entity type
     * @return a {@link Page} instance containing the entities within the specified page, along with paging information
     * @throws NullPointerException if the query or pageRequest is null
     */
    <T> Page<T> selectOffSet(SelectQuery query, PageRequest pageRequest);
}
