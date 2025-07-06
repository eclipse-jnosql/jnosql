/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 */
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.nosql.Template;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.PreparedStatement;

import java.util.Optional;
import java.util.stream.Stream;



/**
 * Interface representing a template for accessing a semi-structured database.
 * It extends the {@link Template} interface.
 * This interface provides methods for executing queries, counting elements, and preparing statements.
 */
public interface SemiStructuredTemplate extends Template {

    /**
     * Returns the number of elements in a specified column family.
     *
     * @param entity the name of the entity (column family)
     * @return the number of elements
     * @throws NullPointerException          if the column family name is null
     * @throws UnsupportedOperationException if the database does not support this operation
     */
    long count(String entity);

    /**
     * Returns the number of elements of a specified entity type.
     *
     * @param <T>  the entity type
     * @param type the class representing the entity type (column family)
     * @return the number of elements
     * @throws NullPointerException          if the entity type is null
     * @throws UnsupportedOperationException if the database does not support this operation
     */
    <T> long count(Class<T> type);

    /**
     * Executes a native query on the database and returns the result as a {@link Stream}.
     *
     * <p>
     * The query syntax is specific to each provider and may vary between implementations and NoSQL providers.
     * </p>
     *
     * @param query the native query
     * @param <T>   the type of the entities in the result stream
     * @return the result as a {@link Stream}
     * @throws NullPointerException          if the query is null
     * @throws UnsupportedOperationException if the provider does not support query by text
     */
    <T> Stream<T> query(String query);

    /**
     * Executes a native query on the database and returns the result as a {@link Stream}.
     *
     * <p>
     * The query syntax is specific to each provider and may vary between implementations and NoSQL providers.
     * </p>
     *
     * @param query  the native query
     * @param entity the name of the entity (column family)
     * @param <T>    the type of the entities in the result stream
     * @return the result as a {@link Stream}
     * @throws NullPointerException          if the query or entity is null
     * @throws UnsupportedOperationException if the provider does not support query by text
     */
    <T> Stream<T> query(String query, String entity);

    /**
     * Executes a query on the database and returns the result as a single unique result wrapped in an {@link Optional}.
     *
     * <p>
     * The query syntax is specific to each provider and may vary between implementations and NoSQL providers.
     * </p>
     *
     * @param query the query
     * @param <T>   the type of the entity in the result
     * @return the result as an {@link Optional}
     * @throws NullPointerException          if the query is null
     * @throws UnsupportedOperationException if the provider does not support query by text
     */
    <T> Optional<T> singleResult(String query);

    /**
     * Executes a query on the database and returns the result as a single unique result wrapped in an {@link Optional}.
     *
     * <p>
     * The query syntax is specific to each provider and may vary between implementations and NoSQL providers.
     * </p>
     *
     * @param query  the query
     * @param entity the name of the entity
     * @param <T>    the type of the entity in the result
     * @return the result as an {@link Optional}
     * @throws NullPointerException          if the query or entity is null
     * @throws UnsupportedOperationException if the provider does not support query by text
     */
    <T> Optional<T> singleResult(String query, String entity);

    /**
     * Creates a {@link PreparedStatement} from the specified query.
     *
     * <p>
     * The query syntax is specific to each provider and may vary between implementations and NoSQL providers.
     * </p>
     *
     * @param query the query
     * @return a {@link PreparedStatement} instance
     * @throws NullPointerException          if the query is null
     * @throws UnsupportedOperationException if the provider does not support query by text
     */
    PreparedStatement prepare(String query);

    /**
     * Creates a {@link PreparedStatement} from the specified query.
     *
     * <p>
     * The query syntax is specific to each provider and may vary between implementations and NoSQL providers.
     * </p>
     *
     * @param query  the query
     * @param entity the name of the entity
     * @return a {@link PreparedStatement} instance
     * @throws NullPointerException          if the query or entity is null
     * @throws UnsupportedOperationException if the provider does not support query by text
     */
    PreparedStatement prepare(String query, String entity);
    /**
     * Deletes an entity
     *
     * @param query query to delete an entity
     * @throws NullPointerException when query is null
     */
    void delete(DeleteQuery query);

    /**
     * Finds entities from query
     *
     * @param query - query to figure out entities
     * @param <T>   the instance type
     * @return entities found by query
     * @throws NullPointerException when query is null
     */
    <T> Stream<T> select(SelectQuery query);

    /**
     * Returns the number of items in the column family that match a specified query.
     * @param query the query
     * @return the number of documents from query
     * @throws NullPointerException when query is null
     */
    long count(SelectQuery query);

    /**
     * Returns whether an entity that match a specified query.
     * @param query the query
     * @return true if an entity with the given query exists, false otherwise.
     * @throws NullPointerException when query it null
     */
    boolean exists(SelectQuery query);

    /**
     * Returns a single entity from query
     *
     * @param query - query to figure out entities
     * @param <T>   the instance type
     * @return an entity on {@link Optional} or {@link Optional#empty()} when the result is not found.
     * @throws NullPointerException     when query is null
     */
    <T> Optional<T> singleResult(SelectQuery query);

    /**
     * Returns all entities on the database
     * @param type the entity type filter
     * @return the {@link Stream}
     * @param <T> the entity type
     * @throws NullPointerException when type is null
     */
    <T> Stream<T> findAll(Class<T> type);

    /**
     * delete all entities from the database
     * @param type the entity type filter
     * @param <T> the entity type
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
     * @param query         the query to retrieve entities
     * @param pageRequest   the page request defining the cursor-based paging
     * @param <T>           the entity type
     * @return a {@link CursoredPage} instance containing the entities within the specified page
     * @throws NullPointerException     if the query or pageRequest is null
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
     * @param query         the query to retrieve entities
     * @param pageRequest   the page request defining the offset-based paging, including the offset and page size
     * @param <T>           the entity type
     * @return a {@link Page} instance containing the entities within the specified page, along with paging information
     * @throws NullPointerException if the query or pageRequest is null
     */
    <T> Page<T> selectOffSet(SelectQuery query, PageRequest pageRequest);
}
