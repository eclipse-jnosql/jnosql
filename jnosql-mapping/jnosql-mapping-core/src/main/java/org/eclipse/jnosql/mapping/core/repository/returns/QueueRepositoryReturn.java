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
package org.eclipse.jnosql.mapping.core.repository.returns;

import org.eclipse.jnosql.mapping.core.repository.DynamicReturn;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Converts repository results to {@link java.util.Queue} return values.
 */
public class QueueRepositoryReturn extends AbstractRepositoryReturn {

    /**
     * Creates a queue repository return handler.
     */
    public QueueRepositoryReturn() {
        super(Deque.class);
    }

    @Override
    public boolean isCompatible(Class<?> entity, Class<?> returnType) {
        return Deque.class.equals(returnType)
                || Queue.class.equals(returnType);
    }

    @Override
    public <T> Object convert(DynamicReturn<T> dynamicReturn) {
        return dynamicReturn.result().collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public <T> Object convertPageRequest(DynamicReturn<T> dynamicReturn) {
        return dynamicReturn.streamPagination().collect(Collectors.toCollection(LinkedList::new));
    }
}
