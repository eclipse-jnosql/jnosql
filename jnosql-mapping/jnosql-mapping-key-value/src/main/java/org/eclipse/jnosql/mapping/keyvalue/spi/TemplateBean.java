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
package org.eclipse.jnosql.mapping.keyvalue.spi;


import jakarta.enterprise.context.spi.CreationalContext;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplateProducer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

class TemplateBean extends AbstractBean<KeyValueTemplate> {

    private static final Set<Type> TYPES = Collections.singleton(KeyValueTemplate.class);

    private final String provider;

    private final Set<Annotation> qualifiers;

    /**
     * Constructor
     * @param provider    the provider name, that must be a
     */
    public TemplateBean(String provider) {
        this.provider = provider;
        this.qualifiers = Collections.singleton(DatabaseQualifier.ofKeyValue(provider));
    }

    @Override
    public Class<?> getBeanClass() {
        return KeyValueTemplate.class;
    }

    @Override
    public KeyValueTemplate create(CreationalContext<KeyValueTemplate> context) {

        KeyValueTemplateProducer producer = getInstance(KeyValueTemplateProducer.class);
        BucketManager manager = getManager();
        return producer.apply(manager);
    }

    private BucketManager getManager() {
        return getInstance(BucketManager.class, DatabaseQualifier.ofKeyValue(provider));
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
        return KeyValueTemplate.class.getName() + DatabaseType.KEY_VALUE + "-" + provider;
    }

}
