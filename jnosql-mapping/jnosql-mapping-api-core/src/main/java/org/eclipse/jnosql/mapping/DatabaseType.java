/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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


/**
 * Defines the database type supported on Jakarta NoSQL Mapping.
 */
public enum DatabaseType {
    /**
     * A document database is a type of non-relational database that is designed to store semi-structured data as documents.
     * Document databases are intuitive for developers to use because the data in the application tier is typically represented as a JSON document.
     */
    DOCUMENT,
    /**
     * A wide column store is a type of NoSQL database. It uses tables, rows, and columns, but unlike a relational database,
     * the names and format of the columns can vary from row to row in the same table.
     */
    COLUMN,
    /**
     * A key-value database, or key-value store, is a data storage paradigm designed for storing, retrieving,
     * and managing associative arrays, a data structure more commonly known
     * today as a dictionary or hash table. Dictionaries contain a collection of objects,
     * or records, which in turn have many different fields within them, each containing data.
     * These records are stored and retrieved using a key that uniquely identifies the record,
     * and is used to quickly find the data within the database.
     */
    KEY_VALUE,
    /**
     * In computing, a graph database (GDB[1]) is a database that uses graph structures for semantic queries with nodes,
     * edges and properties to represent and store data. A key concept of the system is
     * the graph (or edge or relationship), which directly relates
     * data items in the store a collection of nodes of data and edges representing
     * the relationships between the nodes. The relationships allow data in the store
     * to be linked together directly, and in many cases retrieved with one operation.
     * Graph databases hold the relationships between data as a priority. Querying relationships within a graph database
     * is fast because they are perpetually stored within the database itself.
     * Relationships can be intuitively visualized using graph databases, making it useful for heavily interconnected data.
     */
    GRAPH,
    /**
     * A time-series database is optimized for storing and querying data points indexed in time order.
     * It is commonly used for metrics, events, telemetry, and other values that change over time.
     */
    TIME_SERIES,
    /**
     * That is not a NoSQL type; it defines resources shared among the NoSQL types.
     */
    SHARED
}
