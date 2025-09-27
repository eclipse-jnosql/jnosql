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
package org.eclipse.jnosql.mapping.keyvalue.query;


import jakarta.data.repository.BasicRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplateProducer;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;

import java.lang.reflect.Proxy;
import java.util.Objects;

@ApplicationScoped
public class KeyValueRepositoryProducer {

    @Inject
    private KeyValueTemplateProducer producer;

    @Inject
    private EntitiesMetadata entities;

    public <T, K, R extends BasicRepository<T, K>> R get(Class<R> repositoryClass, BucketManager manager) {
        Objects.requireNonNull(repositoryClass, "repository class is required");
        Objects.requireNonNull(manager, "manager class is required");
        KeyValueTemplate template = producer.apply(manager);
        return get(repositoryClass, template);
    }

    public <T, K, R extends BasicRepository<T, K>> R get(Class<R> repositoryClass, KeyValueTemplate template) {
        Objects.requireNonNull(repositoryClass, "repository class is required");
        Objects.requireNonNull(template, "template class is required");
        KeyValueRepositoryProxy<T, K> handler = new KeyValueRepositoryProxy<>(repositoryClass, entities, template);
        return (R) Proxy.newProxyInstance(repositoryClass.getClassLoader(),
                new Class[]{repositoryClass},
                handler);
    }
}
