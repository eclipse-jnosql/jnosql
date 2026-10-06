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
package org.eclipse.jnosql.mapping.vector.spi;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Any;
import jakarta.nosql.Template;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.vector.VectorTemplate;
import org.eclipse.jnosql.mapping.vector.VectorTemplateProducer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Exposes a named vector manager through the template contracts.
 */
class TemplateBean extends AbstractBean<VectorTemplate> {

    private static final Set<Type> TYPES = Set.of(VectorTemplate.class, SemiStructuredTemplate.class,
            Template.class, Object.class);

    private final String provider;
    private final Set<Annotation> qualifiers;

    TemplateBean(String provider) {
        this.provider = provider;
        this.qualifiers = Set.of(DatabaseQualifier.ofVector(provider), Any.Literal.INSTANCE);
    }

    @Override
    public Class<?> getBeanClass() {
        return VectorTemplate.class;
    }

    @Override
    public VectorTemplate create(CreationalContext<VectorTemplate> context) {
        var producer = getInstance(VectorTemplateProducer.class);
        var manager = getInstance(DatabaseManager.class, DatabaseQualifier.ofVector(provider));
        return producer.apply(manager);
    }

    @Override
    public Set<Type> getTypes() {
        return TYPES;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public String getId() {
        return VectorTemplate.class.getName() + DatabaseType.VECTOR + "-" + provider;
    }
}
