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
package org.eclipse.jnosql.mapping.timeseries.configuration;

import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.DatabaseConfiguration;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DatabaseManagerFactory;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;

import java.time.Duration;
import java.util.stream.Stream;

class TimeSeriesConfigurationMock implements DatabaseConfiguration {


    @Override
    public TimeSeriesManagerFactoryMock apply(Settings settings) {
        return new TimeSeriesManagerFactoryMock(settings);
    }

    public record TimeSeriesManagerFactoryMock(Settings settings) implements DatabaseManagerFactory {

        @Override
            public TimeSeriesManagerMock apply(String database) {
                return new TimeSeriesManagerMock(database);
            }

            @Override
            public void close() {

            }
        }

    public record TimeSeriesManagerMock(String name) implements DatabaseManager {

        @Override
        public CommunicationEntity insert(CommunicationEntity entity) {
            return null;
        }

        @Override
        public CommunicationEntity insert(CommunicationEntity entity, Duration ttl) {
            return null;
        }

        @Override
        public Iterable<CommunicationEntity> insert(Iterable<CommunicationEntity> entities) {
            return null;
        }

        @Override
        public Iterable<CommunicationEntity> insert(Iterable<CommunicationEntity> entities, Duration ttl) {
            return null;
        }

        @Override
        public CommunicationEntity update(CommunicationEntity entity) {
            return null;
        }

        @Override
        public Iterable<CommunicationEntity> update(Iterable<CommunicationEntity> entities) {
            return null;
        }

        @Override
        public void delete(DeleteQuery query) {

        }

        @Override
        public Stream<CommunicationEntity> select(SelectQuery query) {
            return null;
        }

        @Override
        public long count(String entity) {
            return 0;
        }

        @Override
        public void close() {

        }
    }
}
