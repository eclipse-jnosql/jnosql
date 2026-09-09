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
    GRAPH_TRANSACTION_AUTOMATIC("jnosql.graph.transaction.automatic");


    private final String value;

    MappingConfigurations(String value) {
        this.value = value;
    }

    @Override
    public String get() {
        return value;
    }
}
