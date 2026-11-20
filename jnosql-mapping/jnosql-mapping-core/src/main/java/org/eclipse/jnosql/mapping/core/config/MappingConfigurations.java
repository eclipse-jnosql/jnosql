/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.config;

import java.util.function.Supplier;

/**
 * The properties around Eclipse JNoSQL mapping for define create manager instances.
 *
 * <p>The keys are read through MicroProfile Config, for example from {@code microprofile-config.properties}.
 * A {@code *_PROVIDER} value is the fully qualified class name of the database configuration, and is only
 * needed when more than one implementation is on the classpath. The {@code *_DATABASE} key names the database
 * and is required when the corresponding manager is created; a missing value fails with
 * {@link jakarta.data.exceptions.MappingException}.</p>
 *
 * <pre>{@code
 * jnosql.document.provider=com.example.MyDocumentConfiguration
 * jnosql.document.database=people
 * }</pre>
 */
public enum MappingConfigurations implements Supplier<String> {
    /**
     * Define the KeyValueConfiguration that creates a BucketManager instance.
     * It is necessary when there is more than one implementation; otherwise,  it will find automatically.
     */
    KEY_VALUE_PROVIDER("jnosql.keyvalue.provider"),
    /**
     *Define the key-value database name.
     */
    KEY_VALUE_DATABASE("jnosql.keyvalue.database"),
    /**
     * Define the DocumentConfiguration that creates a DocumentManager instance.
     * It is necessary when there is more than one implementation; otherwise,  it will find automatically.
     */
    DOCUMENT_PROVIDER("jnosql.document.provider"),
    /**
     *Define the document database name.
     */
    DOCUMENT_DATABASE("jnosql.document.database"),
    /**
     * Define the DatabaseConfiguration that creates a time-series DatabaseManager instance.
     * It is necessary when there is more than one implementation; otherwise, it will find one automatically.
     */
    TIME_SERIES_PROVIDER("jnosql.timeseries.provider"),
    /**
     * Define the time-series database name.
     */
    TIME_SERIES_DATABASE("jnosql.timeseries.database"),
    /**
     * Define the DatabaseConfiguration implementation that creates a DatabaseManager for vector mapping.
     * It is necessary when there is more than one implementation; otherwise,  it will find automatically.
     */
    VECTOR_PROVIDER("jnosql.vector.provider"),
    /**
     * Define the vector database name.
     */
    VECTOR_DATABASE("jnosql.vector.database"),
    /**
     * Define the ColumnConfiguration that creates a ColumnManager instance.
     * It is necessary when there is more than one implementation; otherwise,  it will find automatically.
     */
    COLUMN_PROVIDER("jnosql.column.provider"),
    /**
     *Define the column database name.
     */
    COLUMN_DATABASE("jnosql.column.database"),
    /**
     * Define the GraphConfiguration that creates a GraphConfiguration instance.
     * It is necessary when there is more than one implementation; otherwise,  it will find automatically.
     */
    GRAPH_PROVIDER("jnosql.graph.provider"),
    /**
     * Define the graph database name.
     */
    GRAPH_DATABASE("jnosql.graph.database"),
    /**
     * Activate the automatic transaction at Graph database. By default it is true.
     */
    GRAPH_TRANSACTION_AUTOMATIC("jnosql.graph.transaction.automatic"),
    /**
     * Selects the executor that runs asynchronous repository methods, those declared with
     * {@link java.util.concurrent.CompletionStage} or {@link java.util.concurrent.CompletableFuture}.
     * The default is {@code auto}: a Jakarta Concurrency {@code ManagedExecutorService} is used when available,
     * otherwise a JNoSQL-owned bounded thread pool. Any other value is the {@code @Named} CDI bean name or the
     * JNDI name (for example {@code java:comp/DefaultManagedExecutorService}) of an
     * {@link java.util.concurrent.Executor}; an unresolvable value fails instead of falling back.
     */
    REPOSITORY_ASYNC_EXECUTOR("jnosql.repository.async.executor"),
    /**
     * Defines the thread count of the JNoSQL-owned executor used when no managed executor is selected.
     * It is ignored otherwise. The default is twice the number of available processors.
     */
    REPOSITORY_ASYNC_POOL_SIZE("jnosql.repository.async.pool-size");


    private final String value;

    MappingConfigurations(String value) {
        this.value = value;
    }

    @Override
    public String get() {
        return value;
    }
}
