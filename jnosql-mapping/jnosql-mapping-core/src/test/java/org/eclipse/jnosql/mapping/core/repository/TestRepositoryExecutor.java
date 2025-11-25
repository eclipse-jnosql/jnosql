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
package org.eclipse.jnosql.mapping.core.repository;

import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.core.entities.ComicBook;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

class TestRepositoryExecutor extends AbstractRepository<ComicBook, Long> {

    private final Template template;

    private final EntitiesMetadata entitiesMetadata;

    public TestRepositoryExecutor(Template template, EntitiesMetadata entitiesMetadata) {
        this.template = template;
        this.entitiesMetadata = entitiesMetadata;
    }
    @Override
    protected Template template() {
        return template;
    }

    @Override
    protected EntityMetadata entityMetadata() {
        return entitiesMetadata.get(ComicBook.class);
    }
}