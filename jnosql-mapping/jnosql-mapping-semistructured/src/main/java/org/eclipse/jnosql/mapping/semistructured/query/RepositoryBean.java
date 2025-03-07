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
import jakarta.data.repository.DataRepository;
import jakarta.enterprise.context.spi.CreationalContext;
import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.core.util.AnnotationLiteralUtil;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class for repository beans that dynamically create repository instances.
 */
public abstract class RepositoryBean<T extends DataRepository<T, ?>> extends AbstractBean<T> {

    private final Class<T> type;
    private final Set<Type> types;
    private final String provider;
    private final Set<Annotation> qualifiers;
    private final DatabaseType databaseType;

    protected RepositoryBean(Class<?> type, String provider, DatabaseType databaseType) {
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

    protected abstract DatabaseQualifier getDatabaseQualifier();

    protected abstract DatabaseQualifier getDatabaseQualifier(String provider);

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

        var handler = new SemiStructuredRepositoryProxy<>(template, entities, type, converters);
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
