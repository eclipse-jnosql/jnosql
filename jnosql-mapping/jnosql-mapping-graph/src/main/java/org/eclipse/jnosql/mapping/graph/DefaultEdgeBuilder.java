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
package org.eclipse.jnosql.mapping.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
class DefaultEdgeBuilder<T, S> implements EdgeBuilder, EdgeBuilder.SourceStep<T>, EdgeBuilder.LabelStep<T>, EdgeBuilder.TargetStep<T, S> {

    private Object id;

    private Object source;

    private Object target;

    private Map<String, Object> properties = new HashMap<>();

    private String label;

    @Override
    public <I> SourceStep<I> source(I source) {
        Objects.requireNonNull(source, "source is required");
        this.source = source;
        return (SourceStep<I>) this;
    }

    @Override
    public <F> TargetStep<T, F> target(F target) {
        Objects.requireNonNull(target, "target is required");
        this.target = target;
        return (TargetStep<T, F>) this;
    }

    @Override
    public LabelStep<T> label(String label) {
        Objects.requireNonNull(label, "label is required");
        this.label = label;
        return this;
    }

    @Override
    public LabelStep<T> label(Supplier<String> label) {
        Objects.requireNonNull(label, "label is required");
        return label(label.get());
    }

    @Override
    public Edge<T, S> build() {
        return null;
    }

    @Override
    public TargetStep<T, S> property(String key, Object value) {
        Objects.requireNonNull(key, "key is required");
        Objects.requireNonNull(value, "value is required");
        properties.put(key, value);
        return this;
    }
}
