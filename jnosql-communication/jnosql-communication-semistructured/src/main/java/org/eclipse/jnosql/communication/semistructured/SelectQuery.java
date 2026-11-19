/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 * and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;


import jakarta.data.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Describes a select operation on {@link CommunicationEntity}: the entity name, optional condition, columns,
 * sorts, skip and limit. Build it with the fluent API ({@link #select(String...)}) or the builder
 * ({@link #builder(String...)}), and run it with {@link DatabaseManager#select(SelectQuery)}.
 *
 * <p>Options that a database does not support, such as columns, sorts, skip or limit, may be ignored by
 * its driver.</p>
 *
 * <pre>{@code
 * SelectQuery query = SelectQuery.select("name")
 *         .from("person")
 *         .where("age").gte(18)
 *         .and("city").eq("Lisbon")
 *         .orderBy("name").asc()
 *         .limit(10)
 *         .build();
 * Stream<CommunicationEntity> people = manager.select(query);
 * }</pre>
 *
 * @see DatabaseManager#select(SelectQuery)
 * @see CriteriaCondition
 * @see Sort
 */
public interface SelectQuery {

    /**
     * Retrieves the maximum number of results the select object was set to retrieve.
     * The implementation might ignore this option.
     *
     * @return the maximum number of results
     */
    long limit();

    /**
     * Retrieves the position of the first result the select object was set to retrieve.
     * The implementation might ignore this option.
     *
     * @return the position of the first result
     */
    long skip();

    /**
     * Retrieves the entity name.
     *
     * @return the entity name
     */
    String name();

    /**
     * Retrieves the conditions contained in this {@link SelectQuery}.
     * If empty, {@link Optional#empty()} indicates that the implementation might either return an unsupported exception or return the same elements in the database.
     *
     * @return the conditions
     */
    Optional<CriteriaCondition> condition();

    /**
     * Returns the columns to be returned in the query. If empty, all elements in the query will be returned.
     * The implementation might ignore this option.
     *
     * @return the columns
     */
    List<String> columns();

    /**
     * Retrieves the sorts contained in this {@link SelectQuery}.
     * The implementation might ignore this option.
     *
     * @return the sorts
     */
    List<Sort<?>> sorts();

    /**
     * Returns true if the query is a count query.
     *
     * @return true if the query is a count query
     */
    default boolean isCount(){
        return false;
    }

    /**
     * Starts the first step of {@link SelectElements} creation using a fluent API.
     * This step informs the fields to return to the query, similar to "select field, fieldB from database" in a database query.
     *
     * @param names - The document fields to query, optional.
     * @return a new {@link SelectElements} instance
     * @throws NullPointerException when any element is null
     */
    static SelectElements select(String... names) {
        Stream.of(names).forEach(d -> Objects.requireNonNull(d, "A document in the query is null"));
        return new DefaultFluentSelectQueryBuilderElements(Arrays.asList(names));
    }

    /**
     * Starts the first step of {@link SelectQuery} creation using a fluent API.
     * This step informs the fields to return to the query, similar to "select field, fieldB from database" in a database query.
     * If empty, all elements in the query will be returned, similar to "select * from database".
     *
     * @return a new {@link SelectElements} instance
     */
    static SelectElements select() {
        return new DefaultFluentSelectQueryBuilderElements(Collections.emptyList());
    }

    /**
     * Starts the first step of {@link SelectQuery} creation using a builder pattern.
     * This step informs the fields to return to the query, similar to "select field, fieldB from database" in a database query.
     *
     * @return a {@link QueryBuilder} instance
     */
    static QueryBuilder builder() {
        return new DefaultQueryBuilder();
    }

    /**
     * Starts the first step of {@link SelectQuery} creation using a builder pattern.
     * This step informs the fields to return to the query, similar to "select field, fieldB from database" in a database query.
     * If empty, all elements in the query will be returned, similar to "select * from database".
     *
     * @param names The document fields to query, optional.
     * @return a {@link QueryBuilder} instance
     */
    static QueryBuilder builder(String... names) {
        Stream.of(names).forEach(d -> Objects.requireNonNull(d, "A column in the query is null"));
        QueryBuilder builder = new DefaultQueryBuilder();
        Stream.of(names).forEach(builder::select);
        return builder;
    }


    /**
     * Represents a query builder interface for selecting columns from a NoSQL database.
     */
    interface SelectFrom extends SelectQueryBuild {

        /**
         * Starts a new condition by defining the column name.
         *
         * @param name the column name
         * @return a new {@link SelectNameCondition} representing the condition
         * @throws NullPointerException when name is null
         */
        SelectNameCondition where(String name);

        /**
         * Defines the position of the first result to retrieve.
         *
         * @param skip the position of the first result to retrieve
         * @return a query with the specified first result position
         * @throws IllegalArgumentException if skip is negative
         */
        SelectSkip skip(long skip);


        /**
         * Defines the maximum number of results to retrieve.
         *
         * @param limit the maximum number of results to retrieve
         * @return the {@link QueryBuilder} with the specified limit
         * @throws IllegalArgumentException if limit is negative
         */
        SelectLimit limit(long limit);

        /**
         * Specifies the order in which the results should be returned.
         *
         * @param name the name of the column to be ordered
         * @return a query with the specified sort order
         * @throws NullPointerException when name is null
         */
        SelectOrder orderBy(String name);
    }

    /**
     * Represents an interface for defining the maximum number of results to retrieve.
     */
    interface SelectLimit extends SelectQueryBuild {

        /**
         * Defines the position of the first result to retrieve.
         *
         * @param skip the position of the first result to retrieve
         * @return a query with the specified first result position
         * @throws IllegalArgumentException if skip is negative
         */
        SelectSkip skip(long skip);
    }

    /**
     * Represents an interface for defining the order of columns in a query.
     */
    interface SelectNameOrder extends SelectQueryBuild {

        /**
         * Specifies the order in which the results should be returned.
         *
         * @param name the name of the column to be ordered
         * @return a query with the specified sort order
         * @throws NullPointerException when name is null
         */
        SelectOrder orderBy(String name);

        /**
         * Defines the position of the first result to retrieve.
         *
         * @param skip the position of the first result to retrieve
         * @return a query with the specified first result position
         * @throws IllegalArgumentException if skip is negative
         */
        SelectSkip skip(long skip);

        /**
         * Defines the maximum number of results to retrieve.
         *
         * @param limit the maximum number of results to retrieve
         * @return the {@link QueryBuilder} with the specified limit
         * @throws IllegalArgumentException if limit is negative
         */
        SelectLimit limit(long limit);
    }

    /**
     * Defines the order direction for a query result.
     */
    interface SelectOrder {

        /**
         * Defines the order as ascending.
         *
         * @return the {@link SelectNameOrder} instance
         */
        SelectNameOrder asc();

        /**
         * Defines the order as descending.
         *
         * @return the {@link SelectNameOrder} instance
         */
        SelectNameOrder desc();
    }

    /**
     * Represents the final step in building a {@link SelectQuery}.
     * It can either return a new {@link SelectQuery} instance or execute a query with a {@link DatabaseManager}.
     */
    interface SelectQueryBuild {

        /**
         * Creates a new instance of {@link SelectQuery}.
         *
         * @return a new {@link SelectQuery} instance
         */
        SelectQuery build();

        /**
         * Executes the query and returns the result as a stream of {@link CommunicationEntity}.
         *
         * @param manager the entity manager
         * @return the result of the query as a stream of {@link CommunicationEntity}
         * @throws NullPointerException when manager is null
         */
        Stream<CommunicationEntity> getResult(DatabaseManager manager);

        /**
         * Executes the query and returns a single result wrapped in an optional {@link CommunicationEntity}.
         *
         * @param manager the entity manager
         * @return the single result of the query wrapped in an optional {@link CommunicationEntity}
         * @throws NullPointerException when manager is null
         */
        Optional<CommunicationEntity> getSingleResult(DatabaseManager manager);
    }

    /**
     * Represents the initial element in a column-based query.
     */
    interface SelectElements {

        /**
         * Defines the entity to query.
         *
         * @param entity the entity to query
         * @return a {@link SelectFrom} query
         * @throws NullPointerException when entity is null
         */
        SelectFrom from(String entity);
    }

    /**
     * A provider class for {@link SelectElements}.
     */
    interface SelectProvider extends Function<String[], SelectElements>, Supplier<SelectElements> {
    }

    /**
     * A provider class for {@link QueryBuilder}.
     */
    interface QueryBuilderProvider extends Function<String[], QueryBuilder>, Supplier<QueryBuilder> {
    }


    /**
     * Represents the Column Order which defines the position of the first result to retrieve.
     */
    interface SelectSkip extends SelectQueryBuild {

        /**
         * Defines the maximum number of results to retrieve.
         * It will truncate to be no longer than the specified limit.
         * The default value is zero, and it will replace the current property.
         *
         * @param limit the maximum number of results to retrieve
         * @return the {@link QueryBuilder} instance
         * @throws IllegalArgumentException if the limit is negative
         */
        SelectLimit limit(long limit);

    }

    /**
     * Represents the Column Where which defines the conditions in the query.
     */
    interface SelectWhere extends SelectQueryBuild {

        /**
         * Starts a new condition in the select using {@link CriteriaCondition#and(CriteriaCondition)}.
         *
         * @param name a condition to be added
         * @return the same {@link SelectNameCondition} with the condition appended
         * @throws NullPointerException when the condition is null
         */
        SelectNameCondition and(String name);

        /**
         * Appends a new condition in the select using {@link CriteriaCondition#or(CriteriaCondition)}.
         *
         * @param name a condition to be added
         * @return the same {@link SelectNameCondition} with the condition appended
         * @throws NullPointerException when the condition is null
         */
        SelectNameCondition or(String name);

        /**
         * Defines the position of the first result to retrieve.
         * It will depend on the NoSQL vendor implementation, but it will discard or skip the search result.
         * The default value is zero, and it will replace the current property.
         *
         * @param skip the first result to retrieve
         * @return a query with the first result defined
         * @throws IllegalArgumentException if the limit is negative
         */
        SelectSkip skip(long skip);

        /**
         * Defines the maximum number of results to retrieve.
         * It will truncate to be no longer than the specified limit.
         * The default value is zero, and it will replace the current property.
         *
         * @param limit the maximum number of results to retrieve
         * @return the {@link QueryBuilder} instance
         * @throws IllegalArgumentException if the limit is negative
         */
        SelectLimit limit(long limit);

        /**
         * Adds the order of how the result will be returned.
         *
         * @param name the name to order
         * @return a query with the sort defined
         * @throws NullPointerException when the name is null
         */
        SelectOrder orderBy(String name);

    }

    /**
     * Represents the base for named conditions.
     */
    interface SelectNameCondition {

        /**
         * Creates an equals condition.
         *
         * @param value the value for the condition
         * @param <T>   the type of the value
         * @return the {@link SelectWhere} instance
         * @throws NullPointerException if the value is null
         */
        <T> SelectWhere eq(T value);

        /**
         * Creates a like condition.
         *
         * @param value the value for the condition
         * @return the {@link SelectWhere} instance
         * @throws NullPointerException if the value is null
         */
        SelectWhere like(String value);

        /**
         * Creates a greater than condition.
         *
         * @param <T>   the type of the value
         * @param value the value for the condition
         * @return the {@link SelectWhere} instance
         * @throws NullPointerException if the value is null
         */
        <T> SelectWhere gt(T value);

        /**
         * Creates a greater than or equals condition.
         *
         * @param <T>   the type of the value
         * @param value the value for the condition
         * @return the {@link SelectWhere} instance
         * @throws NullPointerException if the value is null
         */
        <T> SelectWhere gte(T value);

        /**
         * Creates a lesser than condition.
         *
         * @param <T>   the type of the value
         * @param value the value for the condition
         * @return the {@link SelectWhere} instance
         * @throws NullPointerException if the value is null
         */
        <T> SelectWhere lt(T value);

        /**
         * Creates a lesser than or equals condition.
         *
         * @param <T>   the type of the value
         * @param value the value for the condition
         * @return the {@link SelectWhere} instance
         * @throws NullPointerException if the value is null
         */
        <T> SelectWhere lte(T value);

        /**
         * Creates a between condition.
         *
         * @param <T>    the type of the value
         * @param valueA the start value of the range
         * @param valueB the end value of the range
         * @return the {@link SelectWhere} instance
         * @throws NullPointerException if either valueA or valueB is null
         */
        <T> SelectWhere between(T valueA, T valueB);

        /**
         * Creates an in condition.
         *
         * @param values the values for the condition
         * @param <T>    the type of the values
         * @return the {@link SelectWhere} instance
         * @throws NullPointerException if the values are null
         */
        <T> SelectWhere in(Iterable<T> values);

        /**
         * Creates an equals condition.
         *
         * @return {@link SelectNotCondition}
         */
        SelectNotCondition not();

    }

    /**
     * Represents the negation of a condition.
     */
    interface SelectNotCondition extends SelectNameCondition {
    }

    /**
     * A builder interface for constructing queries using a fluent API.
     * This builder supports the creation of {@link SelectQuery} instances using a builder pattern, providing more possibilities for complex queries.
     * The ColumnQueryBuilder is not brighter than a fluent API; it has the same validation in the creation method.
     * It is a mutable and non-thread-safe class.
     */
    interface QueryBuilder {

        /**
         * Append a new column in the search result. The query will return the result by elements declared such as "select column from database".
         * If it remains empty, it will return all the possible fields, similar to "select * from database".
         *
         * @param column a field to return to the search
         * @return the {@link QueryBuilder}
         * @throws NullPointerException when the column is null
         */
        QueryBuilder select(String column);

        /**
         * Append new columns in the search result. The query will return the result by elements declared such as "select column from database".
         * If it remains empty, it will return all the possible fields, similar to "select * from database".
         *
         * @param columns fields to return in the search
         * @return the {@link QueryBuilder}
         * @throws NullPointerException when there is a null element in columns
         */
        QueryBuilder select(String... columns);

        /**
         * Append a new sort in the query. The first one has more precedence than the next one.
         *
         * @param sort the {@link Sort}
         * @return the {@link QueryBuilder}
         * @throws NullPointerException when the sort is null
         */
        QueryBuilder sort(Sort<?> sort);

        /**
         * Append sorts in the query. The first one has more precedence than the next one.
         *
         * @param sorts an array of {@link Sort}
         * @return the {@link QueryBuilder}
         * @throws NullPointerException when there is a null element in sorts
         */
        QueryBuilder sort(Sort<?>... sorts);

        /**
         * Define the entity in the query, this element is mandatory to build the {@link SelectQuery}.
         *
         * @param entity the entity to query
         * @return the {@link QueryBuilder}
         * @throws NullPointerException when the entity is null
         */
        QueryBuilder from(String entity);

        /**
         * Either add or replace the condition in the query. It has a different behavior than the previous method because it won't append it.
         * Therefore, it will create when it is the first time or replace when it was executed once.
         *
         * @param condition the {@link CriteriaCondition} in the query
         * @return the {@link QueryBuilder}
         * @throws NullPointerException when the condition is null
         */
        QueryBuilder where(CriteriaCondition condition);

        /**
         * Defines the position of the first result to retrieve.
         * It will depend on the NoSQL vendor implementation, but it will discard or skip the search result.
         * The default value is zero, and it will replace the current property.
         *
         * @param skip the first result to retrieve
         * @return a query with the first result defined
         * @throws IllegalArgumentException if skip is negative
         */
        QueryBuilder skip(long skip);

        /**
         * Defines the maximum number of results to retrieve.
         * It will truncate to be no longer than limit.
         * The default value is zero, and it will replace the current property.
         *
         * @param limit the limit
         * @return the {@link QueryBuilder}
         * @throws IllegalArgumentException if limit is negative
         */
        QueryBuilder limit(long limit);

        /**
         * Validate and create a {@link SelectQuery} instance.
         *
         * @return {@link SelectQuery}
         * @throws IllegalStateException when an element is not valid or not filled up, such as the {@link QueryBuilder#from(String)} method not being called
         */
        SelectQuery build();

        /**
         * Execute {@link DatabaseManager#select(SelectQuery)}.
         *
         * @param manager the entity manager
         * @return the result of {@link DatabaseManager#select(SelectQuery)}
         * @throws NullPointerException  when manager is null
         * @throws IllegalStateException when an element is not valid or not filled up, such as the {@link QueryBuilder#from(String)} method not being called
         */
        Stream<CommunicationEntity> getResult(DatabaseManager manager);

        /**
         * Execute {@link DatabaseManager#singleResult(SelectQuery)}.
         *
         * @param manager the entity manager
         * @return the result of {@link DatabaseManager#singleResult(SelectQuery)}
         * @throws NullPointerException  when manager is null
         * @throws IllegalStateException when an element is not valid or not filled up, such as the {@link QueryBuilder#from(String)} method not being called
         */
        Optional<CommunicationEntity> getSingleResult(DatabaseManager manager);
    }
}
