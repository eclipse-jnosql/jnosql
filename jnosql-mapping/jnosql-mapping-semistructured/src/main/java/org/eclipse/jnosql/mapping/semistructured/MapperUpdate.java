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
 *  Matheus Oliveira
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.semistructured;

import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;


final class MapperUpdate extends AbstractMapperQuery implements SemiStructuredMapperUpdate {

    private final List<Element> elements = new LinkedList<>();

    MapperUpdate(EntityMetadata mapping, Converters converters, SemiStructuredTemplate template) {
        super(mapping, converters, template);
    }

    @Override
    public SemiStructuredMapperUpdate set(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        return this;
    }

    @Override
    public <T> SemiStructuredMapperUpdate to(T value) {
        requireNonNull(this.name, "name is required");
        this.elements.add(Element.of(mapping.columnField(name), getValue(value)));
        return this;
    }

    @Override
    public SemiStructuredMapperUpdate where(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.nameForCondition = null;
        return this;
    }

    @Override
    public <T> SemiStructuredMapperUpdate eq(T value) {
        eqImpl(value);
        return this;
    }

    @Override
    public SemiStructuredMapperUpdate like(String value) {
        likeImpl(value);
        return this;
    }

    @Override
    public SemiStructuredMapperUpdate contains(String value) {
        containsImpl(value);
        return this;
    }

    @Override
    public SemiStructuredMapperUpdate startsWith(String value) {
        startWithImpl(value);
        return this;
    }

    @Override
    public SemiStructuredMapperUpdate endsWith(String value) {
        endsWithImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperUpdate gt(T value) {
        gtImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperUpdate gte(T value) {
        gteImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperUpdate lt(T value) {
        ltImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperUpdate lte(T value) {
        lteImpl(value);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperUpdate between(T valueA, T valueB) {
        betweenImpl(valueA, valueB);
        return this;
    }

    @Override
    public <T> SemiStructuredMapperUpdate in(Iterable<T> values) {
        inImpl(values);
        return this;
    }

    @Override
    public SemiStructuredMapperUpdate not() {
        this.negate = true;
        return this;
    }

    @Override
    public SemiStructuredMapperUpdate and(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.nameForCondition = null;
        this.and = true;
        return this;
    }

    @Override
    public SemiStructuredMapperUpdate or(String name) {
        requireNonNull(name, "name is required");
        this.name = name;
        this.nameForCondition = null;
        this.and = false;
        return this;
    }

    @Override
    public SemiStructuredMapperUpdate where(Function function) {
        requireNonNull(function, "function is required");
        setFunction(function);
        return this;
    }

    @Override
    public SemiStructuredMapperUpdate and(Function function) {
        requireNonNull(function, "function is required");
        setFunction(function);
        this.and = true;
        return this;
    }

    @Override
    public SemiStructuredMapperUpdate or(Function function) {
        requireNonNull(function, "function is required");
        setFunction(function);
        this.and = false;
        return this;
    }

    @Override
    public void execute() {
        var query = new SemistructureUpdateQuery(entity, elements, condition);
        this.template.update(query);
    }
}
