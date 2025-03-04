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
package org.eclipse.jnosql.mapping.document.query;

import jakarta.data.repository.DataRepository;
import jakarta.enterprise.context.spi.CreationalContext;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.core.util.AnnotationLiteralUtil;
import org.eclipse.jnosql.mapping.semistructured.query.SemiStructuredRepositoryProxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * This class serves as a JNoSQL discovery bean for CDI extension, responsible for registering Repository instances.
 * It extends {@link AbstractBean} and is parameterized with type {@code T} representing the repository type.
 * <p>
 * Upon instantiation, it initializes with the provided repository type, provider name, and qualifiers.
 * The provider name specifies the database provider for the repository.
 * </p>
 *
 * @param <T> the type of the repository
 * @see AbstractBean
 */
public class RepositoryDocumentBean<T extends DataRepository<T, ?>> extends AbstractBean<T> {

    private final Class<T> type;

    private final Set<Type> types;

    private final String provider;

    private final Set<Annotation> qualifiers;

    /**
     * Constructor
     *
     * @param type        the tye
     * @param provider    the provider name, that must be a
     */
    @SuppressWarnings("unchecked")
    public RepositoryDocumentBean(Class<?> type, String provider) {
        this.type = (Class<T>) type;
        this.types = Collections.singleton(type);
        this.provider = provider;
        if (provider.isEmpty()) {
            this.qualifiers = new HashSet<>();
            qualifiers.add(DatabaseQualifier.ofDocument());
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

    @Override
    @SuppressWarnings("unchecked")
    public T create(CreationalContext<T> context) {
        EntitiesMetadata entities = getInstance(EntitiesMetadata.class);
        var template = provider.isEmpty() ? getInstance(DocumentTemplate.class) :
                getInstance(DocumentTemplate.class, DatabaseQualifier.ofDocument(provider));

        Converters converters = getInstance(Converters.class);

        var handler = new SemiStructuredRepositoryProxy<>(template,
                entities, type, converters);
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
        return type.getName() + '@' + DatabaseType.DOCUMENT + "-" + provider;
    }

}