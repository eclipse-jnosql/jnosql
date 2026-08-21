/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured.repository;

import jakarta.annotation.Priority;
import jakarta.data.repository.Repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.InvocationContext;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(Reflections.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
@AddBeanClasses({
        SemistructuredRepositoryProducerWeldTest.RepositoryBeans.class,
        SemistructuredRepositoryProducerWeldTest.InvocationCounter.class,
        SemistructuredRepositoryProducerWeldTest.RepositoryInterceptor.class
})
@DisplayName("SemistructuredRepositoryProducer with Weld")
class SemistructuredRepositoryProducerWeldTest {

    @Inject
    private WeldRepository repository;

    @Inject
    private SemiStructuredTemplate template;

    @Inject
    private InvocationCounter invocationCounter;

    @BeforeEach
    void setUp() {
        invocationCounter.reset();
    }

    @Nested
    @DisplayName("when CDI injects a repository")
    class RepositoryInjection {

        @Test
        @DisplayName("executes repository operations through the CDI interceptor")
        void shouldExecuteRepositoryThroughInterceptor() {
            when(template.count(WeldEntity.class)).thenReturn(1L);

            long result = repository.countAll();

            assertThat(result).isEqualTo(1L);
            assertThat(invocationCounter.value()).isEqualTo(1);
            verify(template).count(WeldEntity.class);
        }
    }

    @Repository
    @RepositoryIntercepted
    interface WeldRepository extends NoSQLRepository<WeldEntity, String> {

        long countAll();
    }

    @Entity
    record WeldEntity(@Id String id, @Column String name) {
    }

    @Inherited
    @InterceptorBinding
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @interface RepositoryIntercepted {
    }

    @RepositoryIntercepted
    @Interceptor
    @Priority(Interceptor.Priority.APPLICATION)
    static class RepositoryInterceptor {

        @Inject
        private InvocationCounter counter;

        @AroundInvoke
        Object intercept(InvocationContext context) throws Exception {
            counter.increment();
            return context.proceed();
        }
    }

    @ApplicationScoped
    static class InvocationCounter {

        private final AtomicInteger counter = new AtomicInteger();

        void increment() {
            counter.incrementAndGet();
        }

        int value() {
            return counter.get();
        }

        void reset() {
            counter.set(0);
        }
    }

    @ApplicationScoped
    static class RepositoryBeans {

        private final SemiStructuredTemplate template = mock(SemiStructuredTemplate.class);

        @Inject
        private SemistructuredRepositoryProducer producer;

        @Produces
        SemiStructuredTemplate template() {
            return template;
        }

        @Produces
        WeldRepository repository() {
            return producer.get(WeldRepository.class, template);
        }
    }
}
