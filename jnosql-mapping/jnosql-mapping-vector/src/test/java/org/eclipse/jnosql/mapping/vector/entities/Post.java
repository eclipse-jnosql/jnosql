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
import jakarta.nosql.DiscriminatorColumn;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import jakarta.nosql.Inheritance;
import org.eclipse.jnosql.mapping.vector.DenseVector;

@Entity
@Inheritance
@DiscriminatorColumn("dtype")
public class Post {

    @Id
    private String id;

    @Column
    private String content;

    @Column("representation")
    private DenseVector features;

    @Column
    private float[] measurements;

    public Post() {
    }

    public Post(String id, String content, DenseVector features, float[] measurements) {
        this.id = id;
        this.content = content;
        this.features = features;
        this.measurements = measurements;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public DenseVector getFeatures() {
        return features;
    }

    public float[] getMeasurements() {
        return measurements;
    }
}
