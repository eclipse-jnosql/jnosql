/*
 *
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
 *   Elias Nogueira
 *
 */
package org.eclipse.jnosql.communication.keyvalue;

import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.Value;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DefaultKeyValuePreparedStatementTest {


    @Test
    void shouldBind() {
        BucketManager bucketManager = Mockito.mock(BucketManager.class);
        KeyValuePreparedStatement statement = DefaultKeyValuePreparedStatement.get(List.of(Value.of("key1"), Value.of("key2")),
                 bucketManager, Params.newParams(), "from where id = 12");

        statement.bind("key", "value");
    }

    @Test
    void shouldGet() {
        BucketManager bucketManager = Mockito.mock(BucketManager.class);
        Mockito.when(bucketManager.get("key1")).thenReturn(Optional.of(Value.of("value1")));
        KeyValuePreparedStatement statement = DefaultKeyValuePreparedStatement.get(List.of(Value.of("key1"), Value.of("key2")),
                bucketManager, Params.newParams(), "from where id = 12");

        Stream<Value> result = statement.result();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1).contains(Value.of("value1"));
        });
    }

    @Test
    void shouldGetSingleResult() {
        BucketManager bucketManager = Mockito.mock(BucketManager.class);
        Mockito.when(bucketManager.get("key1")).thenReturn(Optional.of(Value.of("value1")));
        KeyValuePreparedStatement statement = DefaultKeyValuePreparedStatement.get(List.of(Value.of("key1"), Value.of("key2")),
                bucketManager, Params.newParams(), "from where id = 12");

        Optional<Value> result = statement.singleResult();
        SoftAssertions.assertSoftly(softly -> softly.assertThat(result).isPresent().contains(Value.of("value1")));
    }

    @Test
    void shouldGetEmptySingleResult() {
        BucketManager bucketManager = Mockito.mock(BucketManager.class);
        Mockito.when(bucketManager.get("key1")).thenReturn(Optional.empty());
        KeyValuePreparedStatement statement = DefaultKeyValuePreparedStatement.get(List.of(Value.of("key1"), Value.of("key2")),
                bucketManager, Params.newParams(), "from where id = 12");

        Optional<Value> result = statement.singleResult();
        SoftAssertions.assertSoftly(softly -> softly.assertThat(result).isEmpty());
    }
}