package org.eclipse.jnosql.mapping.semistructured.repository;

import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.query.data.QueryType;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.CommunicationPreparedStatement;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DeleteQueryParser;
import org.eclipse.jnosql.communication.semistructured.QueryParser;
import org.eclipse.jnosql.communication.semistructured.SelectQueryParser;
import org.eclipse.jnosql.communication.semistructured.UpdateQueryParser;

import java.util.Objects;
import java.util.stream.Stream;

final class SqlQueryParser {
    static final SqlQueryParser INSTANCE = new SqlQueryParser();

    private final SelectQueryParser select = new SelectQueryParser();
    private final DeleteQueryParser delete = new DeleteQueryParser();
    private final UpdateQueryParser update = new UpdateQueryParser();

    public Stream<CommunicationEntity> query(String query, String entity, DatabaseManager manager) {
        validation(query, manager);
        var command = QueryType.parse(query);
        return switch (command) {
            case DELETE -> delete.query(query, manager, CommunicationObserverParser.EMPTY);
            case UPDATE -> update.query(query, manager, CommunicationObserverParser.EMPTY);
            default -> select.query(query, entity, manager, CommunicationObserverParser.EMPTY);
        };
    }


    public CommunicationPreparedStatement prepare(String query, String entity, DatabaseManager manager, CommunicationObserverParser observer) {
        validation(query, manager, observer);
        var command = QueryType.parse(query);
        return switch (command) {
            case DELETE -> delete.prepare(query, manager, observer);
            case UPDATE -> update.prepare(query, manager, observer);
            default -> select.prepare(query, entity, manager, observer);
        };
    }

    private void validation(String query, DatabaseManager manager) {
        Objects.requireNonNull(query, "query is required");
        Objects.requireNonNull(manager, "manager is required");
    }
}
