/*
 *  Copyright (c) 2017 Otávio Santana and others
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
package org.eclipse.jnosql.mapping.graph;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Transaction.Status;
import org.eclipse.jnosql.mapping.graph.model.Book;
import org.eclipse.jnosql.mapping.graph.model.BookTemplate;
import jakarta.nosql.tck.test.CDIExtension;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.tinkerpop.gremlin.structure.Transaction.Status.COMMIT;
import static org.apache.tinkerpop.gremlin.structure.Transaction.Status.ROLLBACK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CDIExtension
public class BookTemplateTest {

    @Inject
    private BookTemplate template;

    @Inject
    private Graph graph;

    @Test
    public void shouldSaveWithTransaction() {
        AtomicReference<Status> status = new AtomicReference<>();

        Book book = Book.builder().withName("The Book").build();
        Transaction transaction = graph.tx();
        transaction.addTransactionListener(status::set);
        template.insert(book);
        assertFalse(transaction.isOpen());
        assertEquals(COMMIT, status.get());
    }

    @Test
    public void shouldSaveWithRollback() {
        AtomicReference<Status> status = new AtomicReference<>();

        Book book = Book.builder().withName("The Book").build();
        Transaction transaction = graph.tx();
        transaction.addTransactionListener(status::set);
        try {
            template.insertException(book);
            assert false;
        }catch (Exception ex){

        }

        assertFalse(transaction.isOpen());
        assertEquals(ROLLBACK, status.get());
    }

    @Test
    public void shouldUseAutomaticNormalTransaction() {
        AtomicReference<Status> status = new AtomicReference<>();

        Book book = Book.builder().withName("The Book").build();
        Transaction transaction = graph.tx();
        transaction.addTransactionListener(status::set);
        assertNull(status.get());
        template.normalInsertion(book);
        assertTrue(transaction.isOpen());
        assertEquals(COMMIT, status.get());
    }
}

