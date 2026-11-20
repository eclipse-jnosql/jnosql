/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.repository;

import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.MethodKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AsyncMethodSupportTest {

    interface Fixture {
        CompletionStage<Long> stage();

        CompletableFuture<Long> future();

        Long synchronous();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static AbstractRepositoryInvocationHandler handlerFor(RepositoryMethodType type) {
        var method = Mockito.mock(RepositoryMethod.class);
        Mockito.when(method.type()).thenReturn(type);
        var metadata = Mockito.mock(RepositoryMetadata.class);
        Mockito.when(metadata.find(Mockito.any(MethodKey.class))).thenReturn(Optional.of(method));
        return new AbstractRepositoryInvocationHandler() {
            @Override
            protected AbstractRepository repository() {
                return null;
            }

            @Override
            protected EntityMetadata entityMetadata() {
                return null;
            }

            @Override
            protected RepositoryMetadata repositoryMetadata() {
                return metadata;
            }

            @Override
            protected InfrastructureOperatorProvider infrastructureOperatorProvider() {
                return null;
            }

            @Override
            protected RepositoryOperationProvider repositoryOperationProvider() {
                return null;
            }

            @Override
            protected Template template() {
                return null;
            }
        };
    }

    private static Method method(String name) throws NoSuchMethodException {
        return Fixture.class.getMethod(name);
    }

    @Nested
    @DisplayName("When checking asynchronous method support")
    class WhenTheMethodIsAsynchronous {

        @Test
        @DisplayName("Should accept asynchronous find methods")
        void shouldAcceptFind() throws NoSuchMethodException {
            var descriptor = handlerFor(RepositoryMethodType.FIND_BY).computeMethodDescriptor(method("stage"));

            assertThat(descriptor.type()).isEqualTo(RepositoryMethodType.FIND_BY);
        }

        @Test
        @DisplayName("Should reject asynchronous return types on unsupported operations")
        void shouldRejectUnsupportedOperation() {
            var handler = handlerFor(RepositoryMethodType.COUNT_BY);

            assertThatThrownBy(() -> handler.computeMethodDescriptor(method("stage")))
                    .isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> handler.computeMethodDescriptor(method("future")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should keep default methods that return a future")
        void shouldAcceptDefaultMethods() throws NoSuchMethodException {
            var descriptor = handlerFor(RepositoryMethodType.DEFAULT_METHOD).computeMethodDescriptor(method("future"));

            assertThat(descriptor.type()).isEqualTo(RepositoryMethodType.DEFAULT_METHOD);
        }

        @Test
        @DisplayName("Should not restrict synchronous return types")
        void shouldAcceptSynchronous() throws NoSuchMethodException {
            var descriptor = handlerFor(RepositoryMethodType.COUNT_BY).computeMethodDescriptor(method("synchronous"));

            assertThat(descriptor.type()).isEqualTo(RepositoryMethodType.COUNT_BY);
        }
    }
}
