/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.nosql.Query;
import org.eclipse.jnosql.communication.semistructured.CommunicationPreparedStatement;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

final class SemistructuredQuery implements Query {

    private final String query;

    private final PreparedStatement preparedStatement;

    private SemistructuredQuery(String query, PreparedStatement preparedStatement) {
        this.query = query;
        this.preparedStatement = preparedStatement;
    }

    @Override
    public void executeUpdate() {
        if(isDataRetrievalQuery()) {
            throw executeUpdateDeleteStatementError(" is not either update or delete statement");
        }
        this.preparedStatement.result();
    }

    @Override
    public <T> List<T> result() {
        if (isCount()) {
            Stream<T> count = countStream();
            return count.toList();
        } else if (isDataRetrievalQuery()) {
            Stream<T> entities = this.preparedStatement.result();
            return entities.toList();
        }
        throw executeUpdateDeleteStatementError(" is not a select statement");
    }

    @Override
    public <T> Stream<T> stream() {
        if(isCount()) {
            return countStream();
        } else if(isDataRetrievalQuery()) {
            return this.preparedStatement.result();
        }
        throw executeUpdateDeleteStatementError(" is not a select statement");
    }

    @Override
    public <T> Optional<T> singleResult() {
        if(isCount()) {
            return countSingleResult();
        } else if(isDataRetrievalQuery()) {
            return this.preparedStatement.singleResult();
        }
        throw executeUpdateDeleteStatementError(" is not a select statement");
    }

    @Override
    public Query bind(String name, Object value) {
        this.preparedStatement.bind(name, value);
        return this;
    }

    @Override
    public Query bind(int position, Object value) {
        this.preparedStatement.bind(position, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> countSingleResult() {
        long count = this.preparedStatement.count();
        return (Optional<T>) Optional.of(count);
    }
    @SuppressWarnings("unchecked")
    private <T> Stream<T> countStream() {
        long count = this.preparedStatement.count();
        return (Stream<T>) Stream.of(count);
    }

    private boolean isCount() {
        return CommunicationPreparedStatement.PreparedStatementType.COUNT.equals(this.preparedStatement.type());
    }

    private boolean isDataRetrievalQuery() {
        return CommunicationPreparedStatement.PreparedStatementType.COUNT.equals(this.preparedStatement.type())
                || CommunicationPreparedStatement.PreparedStatementType.SELECT.equals(this.preparedStatement.type());
    }

    private UnsupportedOperationException executeUpdateDeleteStatementError(String x) {
        return new UnsupportedOperationException("The query " + query + x);
    }

    static SemistructuredQuery of(String query, PreparedStatement preparedStatement) {
        return new SemistructuredQuery(query, preparedStatement);
    }

}
