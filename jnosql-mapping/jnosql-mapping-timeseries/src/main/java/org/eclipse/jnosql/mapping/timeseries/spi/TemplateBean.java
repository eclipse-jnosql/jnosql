/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.timeseries.spi;


import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.nosql.Template;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.spi.AbstractBean;
import org.eclipse.jnosql.mapping.timeseries.TimeSeriesTemplate;
import org.eclipse.jnosql.mapping.timeseries.TimeSeriesTemplateProducer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

class TemplateBean extends AbstractBean<TimeSeriesTemplate> {

    private static final Set<Type> TYPES = Set.of(TimeSeriesTemplate.class, Template.class);

    private final String provider;

    private final Set<Annotation> qualifiers;

    TemplateBean( String provider) {
        this.provider = provider;
        this.qualifiers = Collections.singleton(DatabaseQualifier.ofTimeSeries(provider));
    }

    @Override
    public Class<?> getBeanClass() {
        return TimeSeriesTemplate.class;
    }


    @Override
    public TimeSeriesTemplate create(CreationalContext<TimeSeriesTemplate> context) {

        var producer = getInstance(TimeSeriesTemplateProducer.class);
        var manager = getManager();
        return producer.apply(manager);
    }

    private DatabaseManager getManager() {
        return getInstance(DatabaseManager.class, DatabaseQualifier.ofTimeSeries(provider));
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
        return TimeSeriesTemplate.class.getName() + DatabaseType.TIME_SERIES + "-" + provider;
    }

}
