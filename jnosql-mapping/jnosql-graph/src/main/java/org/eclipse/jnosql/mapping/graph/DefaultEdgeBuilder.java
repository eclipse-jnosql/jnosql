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

import java.util.Map;
import java.util.function.Supplier;

class DefaultEdgeBuilder<T, S> implements EdgeBuilder, EdgeBuilder.SourceStep<T>, EdgeBuilder.LabelStep<T>, EdgeBuilder.TargetStep<T, S> {

    private Object id;

    private Object source;

    private Object target;

    private Map<String, Object> properties;

    private String label;

    @Override
    public <S> SourceStep<S> source(S source) {
        return null;
    }

    @Override
    public <T1> TargetStep<T, T1> target(T1 target) {
        return null;
    }

    @Override
    public LabelStep<T> label(String label) {
        return null;
    }

    @Override
    public LabelStep<T> label(Supplier<String> label) {
        return null;
    }

    @Override
    public Edge<T, S> build() {
        return null;
    }

    @Override
    public TargetStep<T, S> property(String key, Object value) {
        return null;
    }
}
