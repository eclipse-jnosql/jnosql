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
package org.eclipse.jnosql.mapping.keyvalue.query;

import jakarta.annotation.Priority;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.InvocationContext;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueEntityConverter;
import org.eclipse.jnosql.mapping.keyvalue.MockProducer;
import org.eclipse.jnosql.mapping.keyvalue.entities.Person;
import org.eclipse.jnosql.mapping.keyvalue.spi.KeyValueExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
@AddBeanClasses({
        KeyValueRepositoryProducerWeldTest.InvocationCounter.class,
        KeyValueRepositoryProducerWeldTest.RepositoryInterceptor.class,
        KeyValueRepositoryProducerWeldTest.MethodInterceptor.class
})
@DisplayName("KeyValueRepositoryProducer with Weld")
public class KeyValueRepositoryProducerWeldTest {

    private static final long ID = 10L;

    @Inject
    private WeldRepository repository;

    @Inject
    private InvocationCounter invocationCounter;

    @BeforeEach
    void setUp() {
        invocationCounter.reset();
    }

    @Nested
    @DisplayName("When invoking an injected repository")
    class WhenTheRepositoryInvocation {

        @Test
        @DisplayName("Should apply the repository interceptor")
        void shouldApplyRepositoryInterceptor() {
            Optional<Person> result = repository.repositoryFindById(ID);

            assertSoftly(softly -> {
                softly.assertThat(result).as("repository result").isPresent();
                softly.assertThat(invocationCounter.repositoryInvocations())
                        .as("repository interceptor invocations")
                        .isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Should apply the method interceptor")
        void shouldApplyMethodInterceptor() {
            Optional<Person> result = repository.interceptedFindById(ID);

            assertSoftly(softly -> {
                softly.assertThat(result).as("repository result").isPresent();
                softly.assertThat(invocationCounter.methodInvocations())
                        .as("method interceptor invocations")
                        .isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Should apply both repository and method interceptors")
        void shouldApplyBothInterceptors() {
            repository.interceptedFindById(ID);

            assertSoftly(softly -> {
                softly.assertThat(invocationCounter.repositoryInvocations())
                        .as("repository interceptor invocations")
                        .isEqualTo(1);
                softly.assertThat(invocationCounter.methodInvocations())
                        .as("method interceptor invocations")
                        .isEqualTo(1);
            });
        }

        @Test
        @DisplayName("Should not apply the method interceptor without its binding")
        void shouldNotApplyMethodInterceptorWithoutBinding() {
            repository.repositoryFindById(ID);

            assertSoftly(softly -> {
                softly.assertThat(invocationCounter.repositoryInvocations())
                        .as("repository interceptor invocations")
                        .isEqualTo(1);
                softly.assertThat(invocationCounter.methodInvocations())
                        .as("method interceptor invocations")
                        .isZero();
            });
        }
    }

    @Repository
    @RepositoryIntercepted
    public interface WeldRepository extends CrudRepository<Person, Long> {

        default Optional<Person> repositoryFindById(Long id) {
            return findById(id);
        }

        @MethodIntercepted
        default Optional<Person> interceptedFindById(Long id) {
            return findById(id);
        }
    }

    @Inherited
    @InterceptorBinding
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @interface RepositoryIntercepted {
    }

    @InterceptorBinding
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    @interface MethodIntercepted {
    }

    @RepositoryIntercepted
    @Interceptor
    @Priority(Interceptor.Priority.APPLICATION)
    static class RepositoryInterceptor {

        @Inject
        private InvocationCounter counter;

        @AroundInvoke
        Object intercept(InvocationContext context) throws Exception {
            counter.incrementRepository();
            return context.proceed();
        }
    }

    @MethodIntercepted
    @Interceptor
    @Priority(Interceptor.Priority.APPLICATION + 1)
    static class MethodInterceptor {

        @Inject
        private InvocationCounter counter;

        @AroundInvoke
        Object intercept(InvocationContext context) throws Exception {
            counter.incrementMethod();
            return context.proceed();
        }
    }

    @ApplicationScoped
    static class InvocationCounter {

        private final AtomicInteger repositoryCounter = new AtomicInteger();
        private final AtomicInteger methodCounter = new AtomicInteger();

        void incrementRepository() {
            repositoryCounter.incrementAndGet();
        }

        void incrementMethod() {
            methodCounter.incrementAndGet();
        }

        int repositoryInvocations() {
            return repositoryCounter.get();
        }

        int methodInvocations() {
            return methodCounter.get();
        }

        void reset() {
            repositoryCounter.set(0);
            methodCounter.set(0);
        }
    }

}
