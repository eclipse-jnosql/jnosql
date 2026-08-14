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

import org.junit.jupiter.api.Nested;
import static org.assertj.core.api.Assertions.assertThatCode;

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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AbstractBeanTest {










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

    @Nested
    @DisplayName("When the abstract bean operates")
    class WhenTheAbstractBeanOperates {

        @DisplayName("Should get injection points")
        @Test
        void shouldGetInjectionPoints() {
            AbstractBean<Object> abstractBean = getInstance();

            Set<InjectionPoint> injectionPoints = abstractBean.getInjectionPoints();

            assertThat(injectionPoints).isEmpty();
        }
        @DisplayName("Should return scope")
        @Test
        void shouldReturnScope() {
            AbstractBean<Object> abstractBean = getInstance();

            Class<? extends Annotation> scope = abstractBean.getScope();

            assertThat(scope).isEqualTo(ApplicationScoped.class);
        }
        @DisplayName("Should return name as null")
        @Test
        void shouldReturnNameAsNull() {
            AbstractBean<Object> abstractBean = getInstance();

            String name = abstractBean.getName();

            assertThat(name).isNull();
        }
        @DisplayName("Should return empty stereotypes")
        @Test
        void shouldReturnEmptyStereotypes() {
            AbstractBean<Object> abstractBean = getInstance();

            Set<Class<? extends Annotation>> stereotypes = abstractBean.getStereotypes();

            assertThat(stereotypes).isEmpty();
        }
        @DisplayName("Should return false for alternative and nullable")
        @Test
        void shouldReturnFalseForAlternativeAndNullable() {
            AbstractBean<Object> abstractBean = getInstance();

            assertThat(abstractBean.isAlternative()).isFalse();
            assertThat(abstractBean.isNullable()).isFalse();
        }
        @DisplayName("Should return instance from cdi without qualifier")
        @Test
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
        @DisplayName("Should return instance from cdi with qualifier")
        @Test
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
        @DisplayName("Should execute destroy without exception")
        @Test
        void shouldExecuteDestroyWithoutException() {
            AbstractBean<Object> abstractBean = getInstance();
            CreationalContext<Object> context = mock(CreationalContext.class);

            assertThatCode(() -> abstractBean.destroy(new Object(), context))
                    .doesNotThrowAnyException();
        }
    }
}
