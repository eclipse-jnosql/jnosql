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
package org.eclipse.jnosql.mapping.core.spi;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.PassivationCapable;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;


/**
 * A template class to all the {@link Bean} at the Eclipse JNoSQL artemis project.
 * It will work as Template method.
 *
 * @param <T> the bean type
 */
public abstract class AbstractBean<T> implements Bean<T>, PassivationCapable {

    public  <T> T getInstance(Class<T> bean) {
        return CDI.current().select(bean).get();
    }

    public  <T> T getInstance(Class<T> bean, Annotation qualifier) {
        return CDI.current().select(bean, qualifier).get();
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    public boolean isNullable() {
        return false;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public void destroy(T instance, CreationalContext<T> context) {

    }

}
