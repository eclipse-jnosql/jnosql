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
package org.eclipse.jnosql.mapping.semistructured.spi;


import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.nosql.column.ColumnTemplate;
import org.eclipse.jnosql.communication.column.ColumnManager;
import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.semistructured.ColumnTemplateProducer;
import org.eclipse.jnosql.mapping.semistructured.JNoSQLColumnTemplate;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

class TemplateBean extends AbstractBean<JNoSQLColumnTemplate> {

    private static final Set<Type> TYPES = Set.of(ColumnTemplate.class, JNoSQLColumnTemplate.class);
    private final String provider;

    private final Set<Annotation> qualifiers;

    /**
     * Constructor
     *
     * @param provider    the provider name, that must be a
     */
    public TemplateBean(String provider) {
        this.provider = provider;
        this.qualifiers = Collections.singleton(DatabaseQualifier.ofColumn(provider));
    }

    @Override
    public Class<?> getBeanClass() {
        return ColumnTemplate.class;
    }


    @Override
    public JNoSQLColumnTemplate create(CreationalContext<JNoSQLColumnTemplate> context) {

        ColumnTemplateProducer producer = getInstance(ColumnTemplateProducer.class);
        ColumnManager manager = getColumnManager();
        return producer.apply(manager);
    }

    private ColumnManager getColumnManager() {
        return getInstance(ColumnManager.class, DatabaseQualifier.ofColumn(provider));
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
        return ColumnTemplate.class.getName() + DatabaseType.COLUMN + "-" + provider;
    }

}