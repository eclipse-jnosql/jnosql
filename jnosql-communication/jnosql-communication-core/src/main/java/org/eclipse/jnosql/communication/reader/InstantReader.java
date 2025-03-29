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
 *
 */
package org.eclipse.jnosql.communication.reader;

import org.eclipse.jnosql.communication.ValueReader;

import java.time.Instant;

/**
 * Class to reads and converts to {@link Instant} type
 *
 */
public class InstantReader implements ValueReader {

    @Override
    public <T> T read(Class<T> type, Object value) {
        return null;
    }

    @Override
    public boolean test(Class<?> aClass) {
        return Instant.class.isAssignableFrom(aClass);
    }
}
