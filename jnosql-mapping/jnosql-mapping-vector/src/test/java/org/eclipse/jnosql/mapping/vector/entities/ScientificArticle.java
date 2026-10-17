/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.vector.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import org.eclipse.jnosql.mapping.vector.SparseVector;

@Entity
public class ScientificArticle {

    @Id
    private String id;

    @Column
    private String title;

    @Column
    private SparseVector embedding;

    public ScientificArticle() {
    }

    public ScientificArticle(String id, String title, SparseVector embedding) {
        this.id = id;
        this.title = title;
        this.embedding = embedding;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public SparseVector getEmbedding() {
        return embedding;
    }
}
