/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;

import org.eclipse.jnosql.communication.QueryException;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * A query parser to column database type, this class will convert a String to an operation in {@link DatabaseManager}.
 */
public final class QueryParser {
    static final QueryParser INSTANCE = new QueryParser();
    private final SelectQueryParser select = new SelectQueryParser();
    private final DeleteQueryParser delete = new DeleteQueryParser();
    private final UpdateQueryParser update = new UpdateQueryParser();

    /**
     * Executes a query and returns the result, when the operations are <b>insert</b>, <b>update</b> and <b>select</b>
     * command it will return the result of the operation when the command is <b>delete</b> it will return an empty collection.
     *
     * @param query    the query as {@link String}
     * @param manager  the manager
     * @param observer the observer
     * @return the result of the operation if delete it will always return an empty list
     * @throws NullPointerException     when there is parameter null
     * @throws IllegalArgumentException when the query has value parameters
     * @throws QueryException           when there is error in the syntax
     */
    public Stream<CommunicationEntity> query(String query, String entity, DatabaseManager manager, CommunicationObserverParser observer) {
        validation(query, manager, observer);
        var command = QueryType.parse(query);
        return switch (command) {
            case DELETE -> delete.query(query, manager, observer);
            case UPDATE -> update.query(query, manager, observer);
            default -> select.query(query, entity, manager, observer);
        };
    }

    /**
     * Executes a query and returns a {@link CommunicationPreparedStatement}, when the operations are <b>insert</b>, <b>update</b> and <b>select</b>
     * command it will return the result of the operation when the command is <b>delete</b> it will return an empty collection.
     *
     * @param query    the query as {@link String}
     * @param manager  the manager
     * @param observer the observer
     * @return a {@link CommunicationPreparedStatement} instance
     * @throws NullPointerException     when there is parameter null
     * @throws IllegalArgumentException when the query has value parameters
     * @throws QueryException           when there is error in the syntax
     */
    public CommunicationPreparedStatement prepare(String query, String entity, DatabaseManager manager, CommunicationObserverParser observer) {
        validation(query, manager, observer);
        var command = QueryType.parse(query);
        return switch (command) {
            case DELETE -> delete.prepare(query, manager, observer);
            case UPDATE -> update.prepare(query, manager, observer);
            default -> select.prepare(query, entity, manager, observer);
        };
    }

    private void validation(String query, DatabaseManager manager, CommunicationObserverParser observer) {
        Objects.requireNonNull(query, "query is required");
        Objects.requireNonNull(manager, "manager is required");
        Objects.requireNonNull(observer, "manager is observer");
    }
}