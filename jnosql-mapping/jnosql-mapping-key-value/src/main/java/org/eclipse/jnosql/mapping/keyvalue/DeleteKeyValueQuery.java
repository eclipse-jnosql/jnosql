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
package org.eclipse.jnosql.mapping.keyvalue;

import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.data.QueryType;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

final class DeleteKeyValueQuery extends KeyValueQuery {

    DeleteKeyValueQuery(String query,
                        AbstractKeyValueTemplate template,
                        QueryType type,
                        FieldMetadata id,
                        QueryCondition condition,
                        EntityMetadata entityMetadata,
                        KeyValueParameterState parameterState) {
        super(query, template, type, id, condition, entityMetadata, parameterState);
    }

    @Override
    public void executeUpdate() {
        checkParamsLeft();
        parameterState.values().forEach(value ->
                template.deleteByKey(value.get()));
    }

    @Override
    public <T> List<T> result() {
        throw new UnsupportedOperationException("DELETE queries do not produce results: " + query);
    }

    @Override
    public <T> Stream<T> stream() {
        throw new UnsupportedOperationException("DELETE queries do not support stream: " + query);
    }

    @Override
    public <T> Optional<T> singleResult() {
        throw new UnsupportedOperationException("DELETE queries do not support singleResult: " + query);
    }
}