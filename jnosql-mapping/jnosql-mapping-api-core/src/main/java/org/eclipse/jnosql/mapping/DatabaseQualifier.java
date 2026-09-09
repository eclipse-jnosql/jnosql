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
package org.eclipse.jnosql.mapping;


import jakarta.enterprise.util.AnnotationLiteral;
import org.eclipse.jnosql.communication.util.StringUtils;

import java.util.Objects;

import static org.eclipse.jnosql.mapping.DatabaseType.COLUMN;
import static org.eclipse.jnosql.mapping.DatabaseType.DOCUMENT;
import static org.eclipse.jnosql.mapping.DatabaseType.GRAPH;
import static org.eclipse.jnosql.mapping.DatabaseType.KEY_VALUE;
import static org.eclipse.jnosql.mapping.DatabaseType.TIME_SERIES;


/**
 * Utilitarian class to select the {@link Database}
 */
public final class DatabaseQualifier extends AnnotationLiteral<Database> implements Database {

    private static final DatabaseQualifier DEFAULT_DOCUMENT_PROVIDER = new DatabaseQualifier("", DOCUMENT);

    private static final DatabaseQualifier DEFAULT_COLUMN_PROVIDER = new DatabaseQualifier("", COLUMN);

    private static final DatabaseQualifier DEFAULT_KEY_VALUE_PROVIDER = new DatabaseQualifier("", KEY_VALUE);

    private static final DatabaseQualifier DEFAULT_GRAPH_PROVIDER = new DatabaseQualifier("", GRAPH);

    private static final DatabaseQualifier DEFAULT_TIME_SERIES_PROVIDER = new DatabaseQualifier("", TIME_SERIES);

    private final String provider;

    private final DatabaseType type;

    private DatabaseQualifier(String provider, DatabaseType type) {
        this.provider = provider;
        this.type = type;
    }

    @Override
    public DatabaseType value() {
        return type;
    }

    @Override
    public String provider() {
        return provider;
    }


    /**
     * Returns the qualifier filter with document type {@link DatabaseType#DOCUMENT}
     * and the nosql provider default
     *
     * @return the default document provider
     */
    public static DatabaseQualifier ofDocument() {
        return DEFAULT_DOCUMENT_PROVIDER;
    }

    /**
     * Returns the qualifier filter with document type {@link DatabaseType#DOCUMENT} and the
     * nosql provider defined
     *
     * @param provider the provider
     * @return the qualifier filter instance
     * @throws NullPointerException when provider is null
     */
    public static DatabaseQualifier ofDocument(String provider) {
        Objects.requireNonNull(provider, "provider is required");
        if (StringUtils.isBlank(provider)) {
            return DEFAULT_DOCUMENT_PROVIDER;
        }
        return new DatabaseQualifier(provider, DOCUMENT);
    }

    /**
     * Returns the qualifier filter with document type {@link DatabaseType#COLUMN}
     * and the nosql provider default
     *
     * @return the default column provider
     */
    public static DatabaseQualifier ofColumn() {
        return DEFAULT_COLUMN_PROVIDER;
    }

    /**
     * Returns the qualifier filter with document type {@link DatabaseType#COLUMN} and the
     * nosql provider defined
     *
     * @param provider the provider
     * @return the qualifier filter instance
     * @throws NullPointerException when provider is null
     */
    public static DatabaseQualifier ofColumn(String provider) {
        Objects.requireNonNull(provider, "provider is required");
        if (StringUtils.isBlank(provider)) {
            return DEFAULT_COLUMN_PROVIDER;
        }
        return new DatabaseQualifier(provider, COLUMN);
    }
    /**
     * Returns the qualifier filter with document type {@link DatabaseType#KEY_VALUE}
     * and the nosql provider default
     *
     * @return the default key-value provider
     */
    public static DatabaseQualifier ofKeyValue() {
        return DEFAULT_KEY_VALUE_PROVIDER;
    }

    /**
     * Returns the qualifier filter with document type {@link DatabaseType#KEY_VALUE} and the
     * nosql provider defined
     *
     * @param provider the provider
     * @return the qualifier filter instance
     * @throws NullPointerException when provider is null
     */
    public static DatabaseQualifier ofKeyValue(String provider) {
        Objects.requireNonNull(provider, "provider is required");
        if (StringUtils.isBlank(provider)) {
            return DEFAULT_KEY_VALUE_PROVIDER;
        }
        return new DatabaseQualifier(provider, KEY_VALUE);
    }

    /**
     * Returns the qualifier filter with document type {@link DatabaseType#GRAPH}
     * and the nosql provider default
     *
     * @return the default key-value provider
     */
    public static DatabaseQualifier ofGraph() {
        return DEFAULT_GRAPH_PROVIDER;
    }

    /**
     * Returns the qualifier filter with graph type {@link DatabaseType#GRAPH} and the
     * nosql provider defined
     *
     * @param provider the provider
     * @return the qualifier filter instance
     * @throws NullPointerException when provider is null
     */
    public static DatabaseQualifier ofGraph(String provider) {
        Objects.requireNonNull(provider, "provider is required");
        if (StringUtils.isBlank(provider)) {
            return DEFAULT_KEY_VALUE_PROVIDER;
        }
        return new DatabaseQualifier(provider, GRAPH);
    }

    /**
     * Returns the qualifier filter with time-series type {@link DatabaseType#TIME_SERIES}
     * and the NoSQL provider default.
     *
     * @return the default time-series provider
     */
    public static DatabaseQualifier ofTimeSeries() {
        return DEFAULT_TIME_SERIES_PROVIDER;
    }

    /**
     * Returns the qualifier filter with time-series type {@link DatabaseType#TIME_SERIES} and the
     * NoSQL provider defined.
     *
     * @param provider the provider
     * @return the qualifier filter instance
     * @throws NullPointerException when provider is null
     */
    public static DatabaseQualifier ofTimeSeries(String provider) {
        Objects.requireNonNull(provider, "provider is required");
        if (StringUtils.isBlank(provider)) {
            return DEFAULT_TIME_SERIES_PROVIDER;
        }
        return new DatabaseQualifier(provider, TIME_SERIES);
    }
}
