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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.enterprise.context.spi.CreationalContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.core.util.AnnotationLiteralUtil;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

/**
 * Abstract base class for repository beans, handling common logic
 * for both Custom and Standard Repositories.
 *
 * @param <T> The repository type
 */
abstract class BaseRepositoryBean<T> extends AbstractBean<T> {

    private final Class<T> type;
    private final Set<Type> types;
    private final String provider;
    private final Set<Annotation> qualifiers;
    private final DatabaseType databaseType;

    protected BaseRepositoryBean(Class<?> type, String provider, DatabaseType databaseType) {
        this.type = (Class<T>) type;
        this.types = Collections.singleton(type);
        this.provider = provider;
        this.databaseType = databaseType;
        this.qualifiers = initializeQualifiers();
    }

    private Set<Annotation> initializeQualifiers() {
        if (provider.isEmpty()) {
            Set<Annotation> defaultQualifiers = new HashSet<>();
            defaultQualifiers.add(getDatabaseQualifier());
            defaultQualifiers.add(AnnotationLiteralUtil.DEFAULT_ANNOTATION);
            defaultQualifiers.add(AnnotationLiteralUtil.ANY_ANNOTATION);
            return defaultQualifiers;
        }
        return Collections.singleton(getDatabaseQualifier(provider));
    }

    protected abstract Class<? extends SemiStructuredTemplate> getTemplateClass();

    private DatabaseQualifier getDatabaseQualifier() {
        return switch (databaseType) {
            case COLUMN -> DatabaseQualifier.ofColumn();
            case DOCUMENT -> DatabaseQualifier.ofDocument();
            case GRAPH -> DatabaseQualifier.ofGraph();
            default -> throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        };
    }

    private DatabaseQualifier getDatabaseQualifier(String provider) {
        return switch (databaseType) {
            case COLUMN -> DatabaseQualifier.ofColumn(provider);
            case DOCUMENT -> DatabaseQualifier.ofDocument(provider);
            case GRAPH -> DatabaseQualifier.ofGraph(provider);
            default -> throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        };
    }

    /**
     * Subclasses define how the repository handler is created.
     */
    protected abstract InvocationHandler createHandler(EntitiesMetadata entities, SemiStructuredTemplate template, Converters converters);

    @Override
    public Class<?> getBeanClass() {
        return type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T create(CreationalContext<T> context) {
        var entities = getInstance(EntitiesMetadata.class);
        var template = provider.isEmpty()
                ? getInstance(getTemplateClass())
                : getInstance(getTemplateClass(), getDatabaseQualifier(provider));

        var converters = getInstance(Converters.class);
        InvocationHandler handler = createHandler(entities, template, converters);

        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public String getId() {
        return type.getName() + '@' + databaseType + "-" + provider;
    }
}

