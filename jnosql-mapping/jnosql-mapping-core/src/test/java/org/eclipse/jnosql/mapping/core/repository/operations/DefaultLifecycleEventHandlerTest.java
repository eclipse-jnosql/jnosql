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

import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.core.entities.Book;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
@AddPackages(value = LifecycleEventHandler.class)
@AddPackages(value = BookObserver.class)

class DefaultLifecycleEventHandlerTest {

    @Inject
    private LifecycleEventHandler lifecycleEventHandler;

    @Inject
    private BookObserver bookObserver;

    @Test
    @DisplayName("Should fire pre insert event")
    void shouldFirePreInsertEvent() {
        var book = Book.builder().build();
        lifecycleEventHandler.preInsert(book);
        Assertions.assertThat(bookObserver.preInsert().get()).isEqualTo(book);
    }


}