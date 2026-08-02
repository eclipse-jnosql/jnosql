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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
@DisplayName("Custom repository lifecycle events")
class CustomRepositoryLifecycleEventTest {

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

    private VinylStore repository;

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
                repositoriesMetadata.get(VinylStore.class)
                        .orElseThrow(),
                infrastructureOperatorProvider,
                coreBaseRepositoryOperationProvider,
                template);

        this.repository = (VinylStore) Proxy.newProxyInstance(
                CustomRepositoryLifecycleEventTest.class.getClassLoader(),
                new Class[]{VinylStore.class},
                repositoryHandler);
    }

    @Nested
    @DisplayName("When inserting entities")
    class WhenInsert {

        @Test
        @DisplayName("Should fire pre-insert and post-insert events when inserting one entity")
        void shouldFireEventsForOneEntity() {
            // given
            VinylRecord entity = firstEntity();

            when(template.insert(entity))
                    .thenReturn(entity);

            // when
            repository.insert(entity);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            event(LifecycleEventType.PRE_INSERT, entity),
                            event(LifecycleEventType.POST_INSERT, entity));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should fire pre-insert and post-insert events for each entity in a list")
        void shouldFireEventsForEntityList() {
            // given
            VinylRecord first = firstEntity();
            VinylRecord second = secondEntity();
            List<VinylRecord> entities = List.of(first, second);

            when(template.insert(any(Iterable.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            repository.insert(entities);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            event(LifecycleEventType.PRE_INSERT, first),
                            event(LifecycleEventType.PRE_INSERT, second),
                            event(LifecycleEventType.POST_INSERT, first),
                            event(LifecycleEventType.POST_INSERT, second));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should fire pre-insert and post-insert events for each entity in an array")
        void shouldFireEventsForEntityArray() {
            // given
            VinylRecord first = firstEntity();
            VinylRecord second = secondEntity();

            when(template.insert(any(Iterable.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            repository.insert(new VinylRecord[]{first, second});

            // then
            assertThat(observer.events())
                    .containsExactly(
                            event(LifecycleEventType.PRE_INSERT, first),
                            event(LifecycleEventType.PRE_INSERT, second),
                            event(LifecycleEventType.POST_INSERT, first),
                            event(LifecycleEventType.POST_INSERT, second));
        }
    }

    @Nested
    @DisplayName("When updating entities")
    class WhenUpdate {

        @Test
        @DisplayName("Should fire pre-update and post-update events when updating one entity")
        void shouldFireEventsForOneEntity() {
            // given
            VinylRecord entity = firstEntity();

            when(template.update(entity))
                    .thenReturn(entity);

            // when
            repository.update(entity);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            event(LifecycleEventType.PRE_UPDATE, entity),
                            event(LifecycleEventType.POST_UPDATE, entity));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should fire pre-update and post-update events for each entity in a list")
        void shouldFireEventsForEntityList() {
            // given
            VinylRecord first = firstEntity();
            VinylRecord second = secondEntity();
            List<VinylRecord> entities = List.of(first, second);

            when(template.update(any(Iterable.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            repository.update(entities);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            event(LifecycleEventType.PRE_UPDATE, first),
                            event(LifecycleEventType.PRE_UPDATE, second),
                            event(LifecycleEventType.POST_UPDATE, first),
                            event(LifecycleEventType.POST_UPDATE, second));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Should fire pre-update and post-update events for each entity in an array")
        void shouldFireEventsForEntityArray() {
            // given
            VinylRecord first = firstEntity();
            VinylRecord second = secondEntity();

            when(template.update(any(Iterable.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // when
            repository.update(new VinylRecord[]{first, second});

            // then
            assertThat(observer.events())
                    .containsExactly(
                            event(LifecycleEventType.PRE_UPDATE, first),
                            event(LifecycleEventType.PRE_UPDATE, second),
                            event(LifecycleEventType.POST_UPDATE, first),
                            event(LifecycleEventType.POST_UPDATE, second));
        }
    }

    @Nested
    @DisplayName("When saving entities")
    class WhenSave {

        @Test
        @DisplayName("Should fire pre-upsert and post-upsert events when saving one new entity")
        void shouldFireEventsForOneNewEntity() {
            // given
            VinylRecord entity = firstEntity();

            when(template.find(
                    VinylRecord.class,
                    entity.catalogNumber()))
                    .thenReturn(Optional.empty());

            when(template.insert(entity))
                    .thenReturn(entity);

            // when
            repository.save(entity);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            event(LifecycleEventType.PRE_UPSERT, entity),
                            event(LifecycleEventType.POST_UPSERT, entity));
        }

        @Test
        @DisplayName("Should fire pre-upsert and post-upsert events when saving one existing entity")
        void shouldFireEventsForOneExistingEntity() {
            // given
            VinylRecord entity = firstEntity();

            when(template.find(
                    VinylRecord.class,
                    entity.catalogNumber()))
                    .thenReturn(Optional.of(entity));

            when(template.update(entity))
                    .thenReturn(entity);

            // when
            repository.save(entity);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            event(LifecycleEventType.PRE_UPSERT, entity),
                            event(LifecycleEventType.POST_UPSERT, entity));
        }

        @Test
        @DisplayName("Should fire pre-upsert and post-upsert events for each entity in a list")
        void shouldFireEventsForEntityList() {
            // given
            VinylRecord first = firstEntity();
            VinylRecord second = secondEntity();

            when(template.find(
                    VinylRecord.class,
                    first.catalogNumber()))
                    .thenReturn(Optional.empty());

            when(template.find(
                    VinylRecord.class,
                    second.catalogNumber()))
                    .thenReturn(Optional.empty());

            when(template.insert(first))
                    .thenReturn(first);

            when(template.insert(second))
                    .thenReturn(second);

            // when
            repository.save(List.of(first, second));

            // then
            assertThat(observer.events())
                    .containsExactly(
                            event(LifecycleEventType.PRE_UPSERT, first),
                            event(LifecycleEventType.POST_UPSERT, first),
                            event(LifecycleEventType.PRE_UPSERT, second),
                            event(LifecycleEventType.POST_UPSERT, second));
        }

        @Test
        @DisplayName("Should fire pre-upsert and post-upsert events for each entity in an array")
        void shouldFireEventsForEntityArray() {
            // given
            VinylRecord first = firstEntity();
            VinylRecord second = secondEntity();

            when(template.find(
                    VinylRecord.class,
                    first.catalogNumber()))
                    .thenReturn(Optional.empty());

            when(template.find(
                    VinylRecord.class,
                    second.catalogNumber()))
                    .thenReturn(Optional.empty());

            when(template.insert(first))
                    .thenReturn(first);

            when(template.insert(second))
                    .thenReturn(second);

            // when
            repository.save(new VinylRecord[]{first, second});

            // then
            assertThat(observer.events())
                    .containsExactly(
                            event(LifecycleEventType.PRE_UPSERT, first),
                            event(LifecycleEventType.POST_UPSERT, first),
                            event(LifecycleEventType.PRE_UPSERT, second),
                            event(LifecycleEventType.POST_UPSERT, second));
        }
    }

    @Nested
    @DisplayName("When deleting entities")
    class WhenDelete {

        @Test
        @DisplayName("Should fire pre-delete and post-delete events when deleting one entity")
        void shouldFireEventsForOneEntity() {
            // given
            VinylRecord entity = firstEntity();

            // when
            repository.delete(entity);

            // then
            assertThat(observer.events())
                    .containsExactly(
                            event(LifecycleEventType.PRE_DELETE, entity),
                            event(LifecycleEventType.POST_DELETE, entity));
        }

        @Test
        @DisplayName("Should fire pre-delete and post-delete events for each entity in a list")
        void shouldFireEventsForEntityList() {
            // given
            VinylRecord first = firstEntity();
            VinylRecord second = secondEntity();

            // when
            repository.delete(List.of(first, second));

            // then
            assertThat(observer.events())
                    .containsExactly(
                            event(LifecycleEventType.PRE_DELETE, first),
                            event(LifecycleEventType.PRE_DELETE, second),
                            event(LifecycleEventType.POST_DELETE, first),
                            event(LifecycleEventType.POST_DELETE, second));
        }

        @Test
        @DisplayName("Should fire pre-delete and post-delete events for each entity in an array")
        void shouldFireEventsForEntityArray() {
            // given
            VinylRecord first = firstEntity();
            VinylRecord second = secondEntity();

            // when
            repository.delete(new VinylRecord[]{first, second});

            // then
            assertThat(observer.events())
                    .containsExactly(
                            event(LifecycleEventType.PRE_DELETE, first),
                            event(LifecycleEventType.PRE_DELETE, second),
                            event(LifecycleEventType.POST_DELETE, first),
                            event(LifecycleEventType.POST_DELETE, second));
        }
    }

    private ObservedEvent event(
            LifecycleEventType type,
            VinylRecord entity) {
        return new ObservedEvent(type, entity);
    }

    private VinylRecord firstEntity() {
        return new VinylRecord(
                "BLUE-1959",
                "Kind of Blue",
                "Miles Davis",
                Year.of(1959));
    }

    private VinylRecord secondEntity() {
        return new VinylRecord(
                "IMPULSE-1965",
                "A Love Supreme",
                "John Coltrane",
                Year.of(1965));
    }
}