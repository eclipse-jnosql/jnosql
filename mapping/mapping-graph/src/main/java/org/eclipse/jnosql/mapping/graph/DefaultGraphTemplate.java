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

import jakarta.nosql.mapping.Converters;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.eclipse.jnosql.mapping.reflection.ClassMappings;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

/**
 * The default {@link GraphTemplate}
 */
class DefaultGraphTemplate extends AbstractGraphTemplate {

    private Instance<Graph> graph;

    private ClassMappings classMappings;

    private GraphConverter converter;

    private GraphWorkflow workflow;

    private Converters converters;

    @Inject
    DefaultGraphTemplate(Instance<Graph> graph, ClassMappings classMappings, GraphConverter converter,
                         GraphWorkflow workflow,
                         Converters converters) {
        this.graph = graph;
        this.classMappings = classMappings;
        this.converter = converter;
        this.workflow = workflow;
        this.converters = converters;
    }

    DefaultGraphTemplate() {
    }

    @Override
    protected Graph getGraph() {
        return graph.get();
    }

    @Override
    protected ClassMappings getClassMappings() {
        return classMappings;
    }

    @Override
    protected GraphConverter getConverter() {
        return converter;
    }

    @Override
    protected GraphWorkflow getFlow() {
        return workflow;
    }

    @Override
    protected Converters getConverters() {
        return converters;
    }
}