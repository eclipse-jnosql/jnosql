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
package org.eclipse.jnosql.mapping.core.query;

import jakarta.inject.Inject;
import jakarta.nosql.Convert;
import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.core.VetedConverter;
import org.eclipse.jnosql.mapping.core.entities.Person;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.ReflectionClassConverter;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = EntitiesMetadata.class)
@AddPackages(value = VetedConverter.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
@AddPackages(value = ReflectionClassConverter.class)
class AbstractRepositoryTest {

    private Template template;

    @Inject
    private EntitiesMetadata entitiesMetadata;

    private PeopleRepository repository;

    @BeforeEach
    void setUp(){
        this.template = Mockito.mock(Template.class);
        this.repository = new PeopleRepository();
    }

    @Test
    void shouldInsert() {

        Person person = Person.builder().withAge(10).withName("Ada").build();
        this.repository.insert(person);
        Mockito.verify(template).insert(person);
    }

    @Test
    void shouldUpdate() {
        Person person = Person.builder().withAge(10).withName("Ada").build();
        this.repository.update(person);
        Mockito.verify(template).update(person);
    }

    @Test
    void shouldInsertAll() {
        Person person = Person.builder().withAge(10).withName("Ada").build();
        this.repository.insertAll(List.of(person));
        Mockito.verify(template).insert((List.of(person)));
    }

    @Test
    void shouldUpdateAll() {
        Person person = Person.builder().withAge(10).withName("Ada").build();
        this.repository.updateAll(List.of(person));
        Mockito.verify(template).update((List.of(person)));
    }


    @Test
    void shouldDeleteById() {
        this.repository.deleteById(10L);
        Mockito.verify(template).delete(Person.class, 10L);
    }

    @Test
    void shouldDeleteByIdIn() {
        this.repository.deleteByIdIn(List.of(10L));
        Mockito.verify(template).delete(Person.class, 10L);
    }

    @Test
    void shouldFindByID() {
        this.template = Mockito.mock(Template.class);
        this.repository.findById(10L);
        Mockito.verify(template).find(Person.class, 10L);
    }

    @Test
    void shouldFindByIDIn() {
        this.repository.findByIdIn(List.of(10L)).toList();
        Mockito.verify(template).find(Person.class, 10L);
    }

    @Test
    void shouldExistsById() {
        this.repository.existsById(10L);
        Mockito.verify(template).find(Person.class, 10L);
    }

    @Test
    void shouldDelete() {
        Person person = Person.builder().withId(10L).withAge(10).withName("Ada").build();
        this.repository.delete(person);
        Mockito.verify(template).delete(Person.class, 10L);
    }

    @Test
    void shouldDeleteAll() {
        Person person = Person.builder().withId(10L).withAge(10).withName("Ada").build();
        this.repository.deleteAll(List.of(person));
        Mockito.verify(template).delete(Person.class, 10L);
    }

    @Test
    void shouldSaveAsInsert() {
        Person person = Person.builder().withId(10L).withAge(10).withName("Ada").build();
        this.repository.save(person);
        Mockito.verify(template).insert(person);
    }

    @Test
    void shouldSaveAsUpdate() {
        var person = Person.builder().withId(10L).withAge(10).withName("Ada").build();
        Mockito.when(template.find(Person.class, 10L))
                .thenReturn(Optional.of(person));
        this.repository.save(person);
        Mockito.verify(template).update(person);
    }

    @Test
    void shouldSaveAll() {
        Person person = Person.builder().withId(10L).withAge(10).withName("Ada").build();
        this.repository.saveAll(List.of(person));
        Mockito.verify(template).insert(person);
    }

    @Test
    void shouldReturnException(){
        Assertions.assertThrows(UnsupportedOperationException.class, () -> this.repository.findAll());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> this.repository.findAll(null, null));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> this.repository.deleteAll());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> this.repository.countBy());
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
    }


}