/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 * and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;

import java.util.function.Function;

/**
 * Creates {@link DatabaseManager} instances from a database name. It is created by a
 * {@link DatabaseConfiguration} and should throw a {@link NullPointerException} when the database name is null.
 *
 * <p>The factory may hold resources, such as connections, so call {@link #close()} when the application no longer
 * needs it.</p>
 *
 * <pre>{@code
 * try (DatabaseManagerFactory factory = configuration.apply(settings);
 *      DatabaseManager manager = factory.apply("school")) {
 *     manager.count("person");
 * }
 * }</pre>
 *
 * @see DatabaseConfiguration
 * @see DatabaseManager
 */
public interface DatabaseManagerFactory extends Function<String, DatabaseManager>, AutoCloseable {


    /**
     * Closes any resources held by the factory.
     *
     * <p>Note: Some databases may not perform any specific actions upon closing.</p>
     */
    @Override
    void close();
}
