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
package org.eclipse.jnosql.mapping;


import java.util.Objects;

/**
 * The DatabaseMetadata class represents metadata for NoSQL databases used as qualifiers in CDI (Contexts and Dependency Injection).
 * It provides information about the type of NoSQL database and its provider.
 * CDI qualifiers allow for distinguishing between different instances of the same type.
 *
 * <p>Supported types of NoSQL databases are {@link DatabaseType#KEY_VALUE}, {@link DatabaseType#COLUMN},
 * {@link DatabaseType#DOCUMENT}, and {@link DatabaseType#GRAPH}.
 *
 * @see DatabaseType
 */
public final class DatabaseMetadata {

    /**
     * A default DatabaseMetadata instance for key-value databases without a specific provider.
     */
    public static final DatabaseMetadata DEFAULT_KEY_VALUE = new DatabaseMetadata(DatabaseType.KEY_VALUE, "");

    /**
     * A default DatabaseMetadata instance for column databases without a specific provider.
     */
    public static final DatabaseMetadata DEFAULT_COLUMN = new DatabaseMetadata(DatabaseType.COLUMN, "");

    /**
     * A default DatabaseMetadata instance for document databases without a specific provider.
     */
    public static final DatabaseMetadata DEFAULT_DOCUMENT = new DatabaseMetadata(DatabaseType.DOCUMENT, "");

    /**
     * A default DatabaseMetadata instance for graph databases without a specific provider.
     */
    public static final DatabaseMetadata DEFAULT_GRAPH = new DatabaseMetadata(DatabaseType.GRAPH, "");


    private final DatabaseType type;
    private final String provider;

    private DatabaseMetadata(DatabaseType type, String provider) {
        this.type = type;
        this.provider = provider;
    }

    /**
     * Gets the type of the NoSQL database.
     *
     * @return The type of the NoSQL database.
     */
    public DatabaseType getType() {
        return type;
    }

    /**
     * Gets the provider of the NoSQL database.
     *
     * @return The provider of the NoSQL database.
     */
    public String getProvider() {
        return provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DatabaseMetadata that)) {
            return false;
        }
        return type == that.type &&
                Objects.equals(provider, that.provider);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, provider);
    }

    @Override
    public String toString() {
        return type + (provider == null ? "" : '@' + provider);
    }


    /**
     * creates an instance
     *
     * @param database the database annotation
     * @return a  instance
     * @throws NullPointerException when database is null
     */
    public static DatabaseMetadata of(Database database) {
        Objects.requireNonNull(database, "database is required");
        return new DatabaseMetadata(database.value(), database.provider());
    }
}
