/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.repository.operations;

import jakarta.data.event.PreInsertEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.jnosql.mapping.core.entities.Book;

import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
class BookObserver {

    private AtomicReference<Book> preInsert = new AtomicReference<>();

    public void onPreInsert(@Observes PreInsertEvent<Book> event) {
        this.preInsert.set(event.entity());
    }

    public AtomicReference<Book> preInsert() {
        return preInsert;
    }
}
