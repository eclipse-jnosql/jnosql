/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.Mockito.*;

class AbstractBeanTest {


    @Test
    @DisplayName("should return empty injection points")
    void shouldGetInjectionPoints() {
        AbstractBean<Object> abstractBean = getInstance();

        Set<InjectionPoint> injectionPoints = abstractBean.getInjectionPoints();

        assertThat(injectionPoints).isEmpty();
    }

    @Test
    @DisplayName("should return ApplicationScoped as scope")
    void shouldReturnScope() {
        AbstractBean<Object> abstractBean = getInstance();

        Class<? extends Annotation> scope = abstractBean.getScope();

        assertThat(scope).isEqualTo(ApplicationScoped.class);
    }

    @Test
    @DisplayName("should return name as null")
    void shouldReturnNameAsNull() {
        AbstractBean<Object> abstractBean = getInstance();

        String name = abstractBean.getName();

        assertThat(name).isNull();
    }

    @Test
    @DisplayName("should return empty stereotypes")
    void shouldReturnEmptyStereotypes() {
        AbstractBean<Object> abstractBean = getInstance();

        Set<Class<? extends Annotation>> stereotypes = abstractBean.getStereotypes();

        assertThat(stereotypes).isEmpty();
    }

    @Test
    @DisplayName("should return false for isAlternative and isNullable")
    void shouldReturnFalseForAlternativeAndNullable() {
        AbstractBean<Object> abstractBean = getInstance();

        assertThat(abstractBean.isAlternative()).isFalse();
        assertThat(abstractBean.isNullable()).isFalse();
    }

    @Test
    @DisplayName("should call CDI.current() and return instance without qualifier")
    void shouldReturnInstanceFromCdiWithoutQualifier() {
        AbstractBean<Object> abstractBean = getInstance();

        try (MockedStatic<CDI> cdiMock = mockStatic(CDI.class)) {
            @SuppressWarnings("unchecked")
            CDI<Object> cdi = mock(CDI.class);
            Instance<String> instance = mock(Instance.class);

            cdiMock.when(CDI::current).thenReturn(cdi);
            when(cdi.select(String.class)).thenReturn(instance);
            when(instance.get()).thenReturn("mockedValue");

            String result = abstractBean.getInstance(String.class);

            assertThat(result).isEqualTo("mockedValue");
            verify(instance).get();
        }
    }

    @Test
    @DisplayName("should call CDI.current() and return instance with qualifier")
    void shouldReturnInstanceFromCdiWithQualifier() {
        AbstractBean<Object> abstractBean = getInstance();

        try (MockedStatic<CDI> cdiMock = mockStatic(CDI.class)) {
            @SuppressWarnings("unchecked")
            CDI<Object> cdi = mock(CDI.class);
            Instance<String> instance = mock(Instance.class);
            Annotation qualifier = mock(Annotation.class);

            cdiMock.when(CDI::current).thenReturn(cdi);
            when(cdi.select(String.class, qualifier)).thenReturn(instance);
            when(instance.get()).thenReturn("qualifiedValue");

            String result = abstractBean.getInstance(String.class, qualifier);

            assertThat(result).isEqualTo("qualifiedValue");
            verify(instance).get();
        }
    }

    @Test
    @DisplayName("should execute destroy without throwing any exception")
    void shouldExecuteDestroyWithoutException() {
        AbstractBean<Object> abstractBean = getInstance();
        CreationalContext<Object> context = mock(CreationalContext.class);

        assertThatCode(() -> abstractBean.destroy(new Object(), context))
                .doesNotThrowAnyException();
    }

    private AbstractBean<Object> getInstance(){
        return new AbstractBean<>() {
            @Override
            public Class<?> getBeanClass() {
                return null;
            }

            @Override
            public Object create(CreationalContext<Object> creationalContext) {
                return null;
            }

            @Override
            public Set<Type> getTypes() {
                return null;
            }

            @Override
            public Set<Annotation> getQualifiers() {
                return null;
            }

            @Override
            public String getId() {
                return null;
            }
        };
    }
}
