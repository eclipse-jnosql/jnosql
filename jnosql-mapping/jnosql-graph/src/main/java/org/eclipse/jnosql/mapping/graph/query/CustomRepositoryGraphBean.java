/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.graph.query;

import jakarta.enterprise.context.spi.CreationalContext;
import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.core.util.AnnotationLiteralUtil;
import org.eclipse.jnosql.mapping.graph.GraphTemplate;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.semistructured.query.CustomRepositoryHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * A CDI bean for dynamically creating repository implementations for graph databases.
 * <p>
 * This class extends {@link AbstractBean} and provides integration with JNoSQL's
 * custom repository handling mechanism. It facilitates the creation of repositories
 * with support for CDI discovery and dependency injection.
 * </p>
 *
 * @param <T> the type of the repository interface
 * @see AbstractBean
 */
public class CustomRepositoryGraphBean<T> extends AbstractBean<T> {

    private final Class<T> type;

    private final Set<Type> types;

    private final String provider;

    private final Set<Annotation> qualifiers;

    /**
     * Constructs a new {@code CustomRepositoryGraphBean} for the specified repository type and provider.
     *
     * @param type     the repository interface type
     * @param provider the database provider name; if empty, the default provider is used
     */
    @SuppressWarnings("unchecked")
    public CustomRepositoryGraphBean(Class<?> type, String provider) {
        this.type = (Class<T>) type;
        this.types = Collections.singleton(type);
        this.provider = provider;
        if (provider.isEmpty()) {
            this.qualifiers = new HashSet<>();
            qualifiers.add(DatabaseQualifier.ofGraph());
            qualifiers.add(AnnotationLiteralUtil.DEFAULT_ANNOTATION);
            qualifiers.add(AnnotationLiteralUtil.ANY_ANNOTATION);
        } else {
            this.qualifiers = Collections.singleton(DatabaseQualifier.ofDocument(provider));
        }
    }

    @Override
    public Class<?> getBeanClass() {
        return type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T create(CreationalContext<T> context) {
        var entities = getInstance(EntitiesMetadata.class);
        var template = provider.isEmpty() ? getInstance(GraphTemplate.class) :
                getInstance(GraphTemplate.class, DatabaseQualifier.ofGraph(provider));

        var converters = getInstance(Converters.class);

        var handler = CustomRepositoryHandler.builder()
                .entitiesMetadata(entities)
                .template(template)
                .customRepositoryType(type)
                .converters(converters)
                .build();

        return (T) Proxy.newProxyInstance(type.getClassLoader(),
                new Class[]{type},
                handler);
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
        return type.getName() + '@' + DatabaseType.GRAPH + "-" + provider;
    }

}