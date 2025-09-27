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

import jakarta.data.repository.DataRepository;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.core.entities.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;

import static org.eclipse.jnosql.mapping.core.query.AnnotationOperation.DELETE;
import static org.eclipse.jnosql.mapping.core.query.AnnotationOperation.INSERT;
import static org.eclipse.jnosql.mapping.core.query.AnnotationOperation.SAVE;
import static org.eclipse.jnosql.mapping.core.query.AnnotationOperation.UPDATE;

@ExtendWith(MockitoExtension.class)
class AnnotationOperationTest {

    @Mock
    private AbstractRepository<Person, Long> repository;


    @Test
    void shouldReturnInvalidParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("invalid", Person.class, Person.class);
        Person person = Person.builder().build();
        Assertions.assertThatThrownBy(() -> INSERT.invoke(new AnnotationOperation.Operation(method, new Object[]{person, person}, repository)))
                .isInstanceOf(UnsupportedOperationException.class);
        Assertions.assertThatThrownBy(() -> UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{person, person}, repository)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldInsertSingleParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("same", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.insert(person)).thenReturn(person);
        Object invoked = INSERT.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).insert(person);
        Assertions.assertThat(person).isEqualTo(invoked);
    }

    @Test
    void shouldInsertIterableParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterable", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.insertAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = INSERT.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).insertAll(List.of(person));
        Assertions.assertThat(List.of(person)).isEqualTo(invoked);
    }

    @Test
    void shouldInsertArrayObjectParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Person person = Person.builder().withName("Ada").withAge(8).build();
        Mockito.when(repository.insertAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = INSERT.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).insertAll(List.of(person));
        Assertions.assertThat(new Person[]{person}).isEqualTo(invoked);
    }


    @Test
    void shouldUpdateSingleParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("same", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.update(person)).thenReturn(person);
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).update(person);
        Assertions.assertThat(person).isEqualTo(invoked);
    }

    @Test
    void shouldUpdateSingleParameterBoolean() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameBoolean", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.update(person)).thenReturn(person);
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).update(person);
        Assertions.assertThat(invoked).isEqualTo(true);
    }

    @Test
    void shouldUpdateSingleParameterVoid() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameVoid", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.update(person)).thenReturn(person);
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).update(person);
        Assertions.assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @Test
    void shouldUpdateSingleParameterInt() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameInt", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.update(person)).thenReturn(person);
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).update(person);
        Assertions.assertThat(invoked).isEqualTo(1);
    }

    @Test
    void shouldUpdateSingleParameterLong() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameLong", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.update(person)).thenReturn(person);
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).update(person);
        Assertions.assertThat(invoked).isEqualTo(1L);
    }

    @Test
    void shouldUpdateIterableParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterable", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).updateAll(List.of(person));
        Assertions.assertThat(List.of(person)).isEqualTo(invoked);
    }

    @Test
    void shouldUpdateIterableParameterVoid() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableVoid", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).updateAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @Test
    void shouldUpdateIterableParameterInt() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableInt", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).updateAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(1);
    }


    @Test
    void shouldUpdateIterableParameterLong() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableLong", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).updateAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(1L);
    }

    @Test
    void shouldUpdateIterableParameterBoolean() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableBoolean", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).updateAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(true);
    }

    @Test
    void shouldUpdateArrayParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).updateAll(List.of(person));
        Assertions.assertThat(new Person[]{person}).isEqualTo(invoked);
    }

    @Test
    void shouldUpdateArrayParameterBoolean() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayBoolean", Person[].class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).updateAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(true);
    }

    @Test
    void shouldUpdateArrayParameterInt() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayInt", Person[].class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).updateAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(1);
    }


    @Test
    void shouldUpdateArrayParameterLong() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayLong", Person[].class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).updateAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(1L);
    }


    @Test
    void shouldUpdateArrayParameterVoid() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayVoid", Person[].class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).updateAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @Test
    void shouldDeleteSingleParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("same", Person.class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).delete(person);
        Assertions.assertThat(invoked).isNull();
    }

    @Test
    void shouldDeleteSingleParameterBoolean() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameBoolean", Person.class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).delete(person);
        Assertions.assertThat(invoked).isEqualTo(true);
    }

    @Test
    void shouldDeleteAllWhenThereIsNoParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("deleteAll");
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{}, repository));
        Mockito.verify(repository).deleteAll();
        Assertions.assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @Test
    void shouldDeleteSingleParameterVoid() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameVoid", Person.class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).delete(person);
        Assertions.assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @Test
    void shouldDeleteSingleParameterInt() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameInt", Person.class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).delete(person);
        Assertions.assertThat(invoked).isEqualTo(1);
    }

    @Test
    void shouldDeleteSingleParameterLong() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameLong", Person.class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).delete(person);
        Assertions.assertThat(invoked).isEqualTo(1L);
    }


    @Test
    void shouldDeleteIterableParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterable", List.class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        Assertions.assertThat(invoked).isNull();
    }

    @Test
    void shouldDeleteIterableParameterVoid() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableVoid", List.class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @Test
    void shouldDeleteIterableParameterInt() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableInt", List.class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(1);
    }

    @Test
    void shouldDeleteIterableParameterLong() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableLong", List.class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(1L);
    }

    @Test
    void shouldDeleteIterableParameterBoolean() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableBoolean", List.class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(true);
    }

    @Test
    void shouldDeleteArrayParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        Assertions.assertThat(invoked).isNull();
    }

    @Test
    void shouldDeleteArrayParameterBoolean() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayBoolean", Person[].class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(true);
    }

    @Test
    void shouldDeleteArrayParameterInt() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayInt", Person[].class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(1);
    }

    @Test
    void shouldDeleteArrayParameterLong() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayLong", Person[].class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(1L);
    }

    @Test
    void shouldDeleteArrayParameterVoid() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayVoid", Person[].class);
        Person person = Person.builder().build();
        Object invoked = DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        Assertions.assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @Test
    void shouldSaveSingleParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("same", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.save(person)).thenReturn(person);
        Object invoked = SAVE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).save(person);
        Assertions.assertThat(person).isEqualTo(invoked);
    }

    @Test
    void shouldSaveIterableParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterable", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.saveAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = SAVE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).saveAll(List.of(person));
        Assertions.assertThat(List.of(person)).isEqualTo(invoked);
    }


    @Test
    void shouldSaveArrayParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Person person = Person.builder().withName("Ada").withAge(12).build();
        Mockito.when(repository.saveAll(List.of(person))).thenReturn(List.of(person));
        Person[] invoked = (Person[]) SAVE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).saveAll(List.of(person));
        Assertions.assertThat(invoked).contains(person);
    }

    @Test
    void shouldEqualsAnnotationOperation() throws NoSuchMethodException {
        Person person = Person.builder().build();
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Object[] params = {new Person[]{person}};
        AnnotationOperation.Operation operation = new AnnotationOperation.Operation(method, params,
                repository);

        AnnotationOperation.Operation operation2 = new AnnotationOperation.Operation(method, params,
                repository);

        Assertions.assertThat(operation).isEqualTo(operation2);
        Assertions.assertThat(operation).isEqualTo(operation);
        Assertions.assertThat(operation).isNotEqualTo(123);
    }

    @Test
    void shouldHashcodeAnnotationOperation() throws NoSuchMethodException {
        Person person = Person.builder().build();
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Object[] params = {new Person[]{person}};
        AnnotationOperation.Operation operation = new AnnotationOperation.Operation(method, params,
                repository);

        AnnotationOperation.Operation operation2 = new AnnotationOperation.Operation(method, params,
                repository);

        Assertions.assertThat(operation.hashCode()).isEqualTo(operation2.hashCode());
    }

    @Test
    void shouldToStringAnnotationOperation() throws NoSuchMethodException {
        Person person = Person.builder().build();
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Object[] params = {new Person[]{person}};
        AnnotationOperation.Operation operation = new AnnotationOperation.Operation(method, params,
                repository);

        Assertions.assertThat(operation.toString()).isNotEmpty();
    }

    interface PersonRepository extends DataRepository<Person, Long>{

        void invalid(Person person, Person person2);
        Person same(Person person);

        boolean sameBoolean(Person person);

        void sameVoid(Person person);

        int sameInt(Person person);

        long sameLong(Person person);

        Person[] array(Person[] people);

        boolean arrayBoolean(Person[] people);

        void arrayVoid(Person[] people);

        int arrayInt(Person[] people);

        long arrayLong(Person[] people);

        List<Person> iterable(List<Person> people);

        void iterableVoid(List<Person> people);

        boolean iterableBoolean(List<Person> people);

        int iterableInt(List<Person> people);

        long iterableLong(List<Person> people);

        void deleteAll();
    }
}