/*
 *  Copyright (c) 2023-2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.query;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import jakarta.inject.Inject;
import jakarta.nosql.Convert;
import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.core.VetedConverter;
import org.eclipse.jnosql.mapping.core.entities.Person;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.ReflectionClassConverter;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = EntitiesMetadata.class)
@AddPackages(value = VetedConverter.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
@AddPackages(value = ReflectionClassConverter.class)
class AbstractRepositoryTest {
    private Template template;

    private LifecycleEventHandler lifecycleEventHandler;

    @Inject
    private EntitiesMetadata entitiesMetadata;

    private PeopleRepository repository;

    @BeforeEach
    void setUp() {
        this.template = mock(Template.class);
        this.lifecycleEventHandler = mock(LifecycleEventHandler.class);
        this.repository = new PeopleRepository();
    }

    @Nested
    @DisplayName("When inserting entities")
    class WhenInsert {

        @DisplayName("Should insert one entity with lifecycle events")
        @Test
        void shouldInsertOneEntityWithLifecycleEvents() {
            // given
            Person person = person();
            when(template.insert(person)).thenReturn(person);

            // when
            repository.insert(person);

            // then
            var ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preInsert(person);
            ordered.verify(template).insert(person);
            ordered.verify(lifecycleEventHandler).postInsert(person);
        }

        @DisplayName("Should insert all entities with lifecycle events")
        @Test
        void shouldInsertAllEntitiesWithLifecycleEvents() {
            // given
            Person person = person();
            List<Person> people = List.of(person);
            when(template.insert(people)).thenReturn(people);

            // when
            repository.insertAll(people);

            // then
            var ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preInsert(person);
            ordered.verify(template).insert(people);
            ordered.verify(lifecycleEventHandler).postInsert(person);
        }
    }

    @Nested
    @DisplayName("When updating entities")
    class WhenUpdate {

        @DisplayName("Should update one entity with lifecycle events")
        @Test
        void shouldUpdateOneEntityWithLifecycleEvents() {
            // given
            Person person = person();
            when(template.update(person)).thenReturn(person);

            // when
            repository.update(person);

            // then
            var ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpdate(person);
            ordered.verify(template).update(person);
            ordered.verify(lifecycleEventHandler).postUpdate(person);
        }

        @DisplayName("Should update all entities with lifecycle events")
        @Test
        void shouldUpdateAllEntitiesWithLifecycleEvents() {
            // given
            Person person = person();
            List<Person> people = List.of(person);
            when(template.update(people)).thenReturn(people);

            // when
            repository.updateAll(people);

            // then
            var ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpdate(person);
            ordered.verify(template).update(people);
            ordered.verify(lifecycleEventHandler).postUpdate(person);
        }
    }

    @Nested
    @DisplayName("When saving entities")
    class WhenSave {

        @DisplayName("Should save missing entity as insert with lifecycle events")
        @Test
        void shouldSaveMissingEntityAsInsertWithLifecycleEvents() {
            // given
            Person person = personWithId();
            when(template.find(Person.class, 10L)).thenReturn(Optional.empty());
            when(template.insert(person)).thenReturn(person);

            // when
            repository.save(person);

            // then
            var ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpsert(person);
            ordered.verify(template).find(Person.class, 10L);
            ordered.verify(template).insert(person);
            ordered.verify(lifecycleEventHandler).postUpsert(person);
        }

        @DisplayName("Should save existing entity as update with lifecycle events")
        @Test
        void shouldSaveExistingEntityAsUpdateWithLifecycleEvents() {
            // given
            Person person = personWithId();
            when(template.find(Person.class, 10L))
                    .thenReturn(Optional.of(person));
            when(template.update(person)).thenReturn(person);

            // when
            repository.save(person);

            // then
            var ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpsert(person);
            ordered.verify(template).find(Person.class, 10L);
            ordered.verify(template).update(person);
            ordered.verify(lifecycleEventHandler).postUpsert(person);
        }

        @DisplayName("Should save all missing entities with lifecycle events")
        @Test
        void shouldSaveAllMissingEntitiesWithLifecycleEvents() {
            // given
            Person person = personWithId();
            when(template.find(Person.class, 10L)).thenReturn(Optional.empty());
            when(template.insert(person)).thenReturn(person);

            // when
            repository.saveAll(List.of(person));

            // then
            var ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preUpsert(person);
            ordered.verify(template).find(Person.class, 10L);
            ordered.verify(template).insert(person);
            ordered.verify(lifecycleEventHandler).postUpsert(person);
        }
    }

    @Nested
    @DisplayName("When deleting entities")
    class WhenDelete {

        @DisplayName("Should delete one entity with lifecycle events")
        @Test
        void shouldDeleteOneEntityWithLifecycleEvents() {
            // given
            Person person = personWithId();

            // when
            repository.delete(person);

            // then
            var ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preDelete(person);
            ordered.verify(template).delete(Person.class, 10L);
            ordered.verify(lifecycleEventHandler).postDelete(person);
        }

        @DisplayName("Should delete all entities with lifecycle events")
        @Test
        void shouldDeleteAllEntitiesWithLifecycleEvents() {
            // given
            Person person = personWithId();

            // when
            repository.deleteAll(List.of(person));

            // then
            var ordered = inOrder(lifecycleEventHandler, template);
            ordered.verify(lifecycleEventHandler).preDelete(person);
            ordered.verify(template).delete(Person.class, 10L);
            ordered.verify(lifecycleEventHandler).postDelete(person);
        }

        @DisplayName("Should delete by id without lifecycle events")
        @Test
        void shouldDeleteByIdWithoutLifecycleEvents() {
            // when
            repository.deleteById(10L);

            // then
            verify(template).delete(Person.class, 10L);
            verifyNoInteractions(lifecycleEventHandler);
        }

        @DisplayName("Should delete by id in without lifecycle events")
        @Test
        void shouldDeleteByIdInWithoutLifecycleEvents() {
            // when
            repository.deleteByIdIn(List.of(10L));

            // then
            verify(template).delete(Person.class, 10L);
            verifyNoInteractions(lifecycleEventHandler);
        }
    }

    @Nested
    @DisplayName("When finding entities")
    class WhenFind {

        @DisplayName("Should find by id")
        @Test
        void shouldFindById() {
            // when
            repository.findById(10L);

            // then
            verify(template).find(Person.class, 10L);
        }

        @DisplayName("Should find by id in")
        @Test
        void shouldFindByIdIn() {
            // when
            repository.findByIdIn(List.of(10L)).toList();

            // then
            verify(template).find(Person.class, 10L);
        }

        @DisplayName("Should check existence by id")
        @Test
        void shouldCheckExistenceById() {
            // when
            repository.existsById(10L);

            // then
            verify(template).find(Person.class, 10L);
        }
    }

    @Nested
    @DisplayName("When invoking unsupported operations")
    class WhenInvokeUnsupportedOperation {

        @DisplayName("Should reject find all")
        @Test
        void shouldRejectFindAll() {
            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(repository::findAll);
        }

        @DisplayName("Should reject paginated find all")
        @Test
        void shouldRejectPaginatedFindAll() {
            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> repository.findAll(null, null));
        }

        @DisplayName("Should reject delete all")
        @Test
        void shouldRejectDeleteAll() {
            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(repository::deleteAll);
        }

        @DisplayName("Should reject count by")
        @Test
        void shouldRejectCountBy() {
            assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(repository::countBy);
        }
    }

    private Person person() {
        return Person.builder()
                .withAge(10)
                .withName("Ada")
                .build();
    }

    private Person personWithId() {
        return Person.builder()
                .withId(10L)
                .withAge(10)
                .withName("Ada")
                .build();
    }

    class PeopleRepository extends AbstractRepository<Person, Long> {

        @Override
        protected Template template() {
            return template;
        }

        @Override
        protected EntityMetadata entityMetadata() {
            return entitiesMetadata.get(Person.class);
        }

        @Override
        protected LifecycleEventHandler lifeCycle() {
            return lifecycleEventHandler;
        }
    }

}
