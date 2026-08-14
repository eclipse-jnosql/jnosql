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
package org.eclipse.jnosql.mapping.core.repository.events;

import jakarta.inject.Inject;
import jakarta.nosql.Convert;
import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.core.VetedConverter;
import org.eclipse.jnosql.mapping.core.repository.CoreRepositoryInvocationHandler;
import org.eclipse.jnosql.mapping.core.repository.InfrastructureOperatorProvider;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreBaseRepositoryOperationProvider;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.reflection.ReflectionClassConverter;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.Year;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(Convert.class)
@AddPackages(EntitiesMetadata.class)
@AddPackages(VetedConverter.class)
@AddPackages(InfrastructureOperatorProvider.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
@AddPackages(ReflectionClassConverter.class)
@AddBeanClasses(VinylRecordLifecycleObserver.class)
@DisplayName("Built-in repository lifecycle events")
class BuiltInRepositoryLifecycleEventTest {

    @Inject
    private CoreBaseRepositoryOperationProvider coreBaseRepositoryOperationProvider;

    @Inject
    private InfrastructureOperatorProvider infrastructureOperatorProvider;

    @Inject
    private LifecycleEventHandler lifecycleEventHandler;

    @Inject
    private EntitiesMetadata entitiesMetadata;

    @Inject
    private RepositoriesMetadata repositoriesMetadata;

    @Inject
    private VinylRecordLifecycleObserver observer;

    private Template template;

    private VinylRecordRepository repository;

    @BeforeEach
    void setUp() {
        this.template = mock(Template.class);
        this.observer.reset();

        var executor = new VinylRepositoryExecutor(
                template,
                entitiesMetadata,
                lifecycleEventHandler);

        var repositoryHandler = CoreRepositoryInvocationHandler.of(
                executor,
                entitiesMetadata.get(VinylRecord.class),
                repositoriesMetadata.get(VinylRecordRepository.class)
                        .orElseThrow(),
                infrastructureOperatorProvider,
                coreBaseRepositoryOperationProvider,
                template);

        this.repository =
                (VinylRecordRepository) Proxy.newProxyInstance(
                        BuiltInRepositoryLifecycleEventTest.class.getClassLoader(),
                        new Class[]{VinylRecordRepository.class},
                        repositoryHandler);
    }

    @Nested
    @DisplayName("When inserting an entity")
    class WhenInsert {

        @DisplayName("Should fire insert events")
        @Test
        void shouldFireInsertEvents() {
            // given
            VinylRecord entity = entity();

            when(template.insert(entity))
                    .thenReturn(entity);

            // when
            VinylRecord result = repository.insert(entity);

            // then
            assertThat(result).isSameAs(entity);

            assertThat(observer.events())
                    .containsExactly(
                            new ObservedEvent(
                                    LifecycleEventType.PRE_INSERT,
                                    entity),
                            new ObservedEvent(
                                    LifecycleEventType.POST_INSERT,
                                    result));
        }
    }

    @Nested
    @DisplayName("When updating an entity")
    class WhenUpdate {

        @DisplayName("Should fire update events")
        @Test
        void shouldFireUpdateEvents() {
            // given
            VinylRecord entity = entity();

            when(template.update(entity))
                    .thenReturn(entity);

            // when
            VinylRecord result = repository.update(entity);

            // then
            assertThat(result).isSameAs(entity);

            assertThat(observer.events())
                    .containsExactly(
                            new ObservedEvent(
                                    LifecycleEventType.PRE_UPDATE,
                                    entity),
                            new ObservedEvent(
                                    LifecycleEventType.POST_UPDATE,
                                    result));
        }
    }

    @Nested
    @DisplayName("When saving a new entity")
    class WhenSaveNewEntity {

        @DisplayName("Should fire upsert events when inserting")
        @Test
        void shouldFireUpsertEventsWhenInserting() {
            // given
            VinylRecord entity = entity();

            when(template.find(
                    VinylRecord.class,
                    entity.catalogNumber()))
                    .thenReturn(Optional.empty());

            when(template.insert(entity))
                    .thenReturn(entity);

            // when
            VinylRecord result = repository.save(entity);

            // then
            assertThat(result).isSameAs(entity);

            assertThat(observer.events())
                    .containsExactly(
                            new ObservedEvent(
                                    LifecycleEventType.PRE_UPSERT,
                                    entity),
                            new ObservedEvent(
                                    LifecycleEventType.POST_UPSERT,
                                    result));
        }
    }

    @Nested
    @DisplayName("When saving an existing entity")
    class WhenSaveExistingEntity {

        @DisplayName("Should fire upsert events when updating")
        @Test
        void shouldFireUpsertEventsWhenUpdating() {
            // given
            VinylRecord entity = entity();

            when(template.find(VinylRecord.class, entity.catalogNumber())).thenReturn(Optional.of(entity));

            when(template.update(entity)).thenReturn(entity);

            // when
            VinylRecord result = repository.save(entity);

            // then
            assertThat(result).isSameAs(entity);

            assertThat(observer.events())
                    .containsExactly(
                            new ObservedEvent(
                                    LifecycleEventType.PRE_UPSERT,
                                    entity),
                            new ObservedEvent(
                                    LifecycleEventType.POST_UPSERT,
                                    result));
        }
    }

    @Nested
    @DisplayName("When deleting an entity")
    class WhenDelete {

        @DisplayName("Should fire delete events")
        @Test
        void shouldFireDeleteEvents() {
            // given
            VinylRecord entity = entity();

            // when
            repository.delete(entity);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            new ObservedEvent(
                                    LifecycleEventType.PRE_DELETE,
                                    entity),
                            new ObservedEvent(
                                    LifecycleEventType.POST_DELETE,
                                    entity));
        }
    }

    private VinylRecord entity() {
        return new VinylRecord(
                "BLUE-1959",
                "Kind of Blue",
                "Miles Davis",
                Year.of(1959));
    }

}
