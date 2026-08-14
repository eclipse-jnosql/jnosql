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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;

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


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("removal")
class AnnotationOperationTest {

    @Mock
    private AbstractRepository<Person, Long> repository;


    @DisplayName("Should return invalid parameter")
    @Test
    void shouldReturnInvalidParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("invalid", Person.class, Person.class);
        Person person = Person.builder().build();
        assertThatThrownBy(() -> AnnotationOperation.INSERT.invoke(new AnnotationOperation.Operation(method, new Object[]{person, person}, repository)))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{person, person}, repository)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @DisplayName("Should insert single parameter")
    @Test
    void shouldInsertSingleParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("same", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.insert(person)).thenReturn(person);
        Object invoked = AnnotationOperation.INSERT.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).insert(person);
        assertThat(person).isEqualTo(invoked);
    }

    @DisplayName("Should insert iterable parameter")
    @Test
    void shouldInsertIterableParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterable", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.insertAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = AnnotationOperation.INSERT.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).insertAll(List.of(person));
        assertThat(List.of(person)).isEqualTo(invoked);
    }

    @DisplayName("Should insert array object parameter")
    @Test
    void shouldInsertArrayObjectParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Person person = Person.builder().withName("Ada").withAge(8).build();
        Mockito.when(repository.insertAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = AnnotationOperation.INSERT.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).insertAll(List.of(person));
        assertThat(new Person[]{person}).isEqualTo(invoked);
    }


    @DisplayName("Should update single parameter")
    @Test
    void shouldUpdateSingleParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("same", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.update(person)).thenReturn(person);
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).update(person);
        assertThat(person).isEqualTo(invoked);
    }

    @DisplayName("Should update single parameter boolean")
    @Test
    void shouldUpdateSingleParameterBoolean() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameBoolean", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.update(person)).thenReturn(person);
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).update(person);
        assertThat(invoked).isEqualTo(true);
    }

    @DisplayName("Should update single parameter void")
    @Test
    void shouldUpdateSingleParameterVoid() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameVoid", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.update(person)).thenReturn(person);
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).update(person);
        assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @DisplayName("Should update single parameter int")
    @Test
    void shouldUpdateSingleParameterInt() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameInt", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.update(person)).thenReturn(person);
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).update(person);
        assertThat(invoked).isEqualTo(1);
    }

    @DisplayName("Should update single parameter long")
    @Test
    void shouldUpdateSingleParameterLong() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameLong", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.update(person)).thenReturn(person);
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).update(person);
        assertThat(invoked).isEqualTo(1L);
    }

    @DisplayName("Should update iterable parameter")
    @Test
    void shouldUpdateIterableParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterable", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).updateAll(List.of(person));
        assertThat(List.of(person)).isEqualTo(invoked);
    }

    @DisplayName("Should update iterable parameter void")
    @Test
    void shouldUpdateIterableParameterVoid() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableVoid", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).updateAll(List.of(person));
        assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @DisplayName("Should update iterable parameter int")
    @Test
    void shouldUpdateIterableParameterInt() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableInt", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).updateAll(List.of(person));
        assertThat(invoked).isEqualTo(1);
    }


    @DisplayName("Should update iterable parameter long")
    @Test
    void shouldUpdateIterableParameterLong() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableLong", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).updateAll(List.of(person));
        assertThat(invoked).isEqualTo(1L);
    }

    @DisplayName("Should update iterable parameter boolean")
    @Test
    void shouldUpdateIterableParameterBoolean() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableBoolean", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).updateAll(List.of(person));
        assertThat(invoked).isEqualTo(true);
    }

    @DisplayName("Should update array parameter")
    @Test
    void shouldUpdateArrayParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).updateAll(List.of(person));
        assertThat(new Person[]{person}).isEqualTo(invoked);
    }

    @DisplayName("Should update array parameter boolean")
    @Test
    void shouldUpdateArrayParameterBoolean() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayBoolean", Person[].class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).updateAll(List.of(person));
        assertThat(invoked).isEqualTo(true);
    }

    @DisplayName("Should update array parameter int")
    @Test
    void shouldUpdateArrayParameterInt() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayInt", Person[].class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).updateAll(List.of(person));
        assertThat(invoked).isEqualTo(1);
    }


    @DisplayName("Should update array parameter long")
    @Test
    void shouldUpdateArrayParameterLong() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayLong", Person[].class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).updateAll(List.of(person));
        assertThat(invoked).isEqualTo(1L);
    }


    @DisplayName("Should update array parameter void")
    @Test
    void shouldUpdateArrayParameterVoid() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayVoid", Person[].class);
        Person person = Person.builder().build();
        Mockito.when(repository.updateAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = AnnotationOperation.UPDATE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).updateAll(List.of(person));
        assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @DisplayName("Should delete single parameter")
    @Test
    void shouldDeleteSingleParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("same", Person.class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).delete(person);
        assertThat(invoked).isNull();
    }

    @DisplayName("Should delete single parameter boolean")
    @Test
    void shouldDeleteSingleParameterBoolean() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameBoolean", Person.class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).delete(person);
        assertThat(invoked).isEqualTo(true);
    }

    @DisplayName("Should delete all when there is no parameter")
    @Test
    void shouldDeleteAllWhenThereIsNoParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("deleteAll");
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{}, repository));
        Mockito.verify(repository).deleteAll();
        assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @DisplayName("Should delete single parameter void")
    @Test
    void shouldDeleteSingleParameterVoid() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameVoid", Person.class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).delete(person);
        assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @DisplayName("Should delete single parameter int")
    @Test
    void shouldDeleteSingleParameterInt() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameInt", Person.class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).delete(person);
        assertThat(invoked).isEqualTo(1);
    }

    @DisplayName("Should delete single parameter long")
    @Test
    void shouldDeleteSingleParameterLong() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("sameLong", Person.class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).delete(person);
        assertThat(invoked).isEqualTo(1L);
    }


    @DisplayName("Should delete iterable parameter")
    @Test
    void shouldDeleteIterableParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterable", List.class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        assertThat(invoked).isNull();
    }

    @DisplayName("Should delete iterable parameter void")
    @Test
    void shouldDeleteIterableParameterVoid() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableVoid", List.class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @DisplayName("Should delete iterable parameter int")
    @Test
    void shouldDeleteIterableParameterInt() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableInt", List.class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        assertThat(invoked).isEqualTo(1);
    }

    @DisplayName("Should delete iterable parameter long")
    @Test
    void shouldDeleteIterableParameterLong() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableLong", List.class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        assertThat(invoked).isEqualTo(1L);
    }

    @DisplayName("Should delete iterable parameter boolean")
    @Test
    void shouldDeleteIterableParameterBoolean() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterableBoolean", List.class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        assertThat(invoked).isEqualTo(true);
    }

    @DisplayName("Should delete array parameter")
    @Test
    void shouldDeleteArrayParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        assertThat(invoked).isNull();
    }

    @DisplayName("Should delete array parameter boolean")
    @Test
    void shouldDeleteArrayParameterBoolean() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayBoolean", Person[].class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        assertThat(invoked).isEqualTo(true);
    }

    @DisplayName("Should delete array parameter int")
    @Test
    void shouldDeleteArrayParameterInt() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayInt", Person[].class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        assertThat(invoked).isEqualTo(1);
    }

    @DisplayName("Should delete array parameter long")
    @Test
    void shouldDeleteArrayParameterLong() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayLong", Person[].class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        assertThat(invoked).isEqualTo(1L);
    }

    @DisplayName("Should delete array parameter void")
    @Test
    void shouldDeleteArrayParameterVoid() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("arrayVoid", Person[].class);
        Person person = Person.builder().build();
        Object invoked = AnnotationOperation.DELETE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).deleteAll(List.of(person));
        assertThat(invoked).isEqualTo(Void.TYPE);
    }

    @DisplayName("Should save single parameter")
    @Test
    void shouldSaveSingleParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("same", Person.class);
        Person person = Person.builder().build();
        Mockito.when(repository.save(person)).thenReturn(person);
        Object invoked = AnnotationOperation.SAVE.invoke(new AnnotationOperation.Operation(method, new Object[]{person}, repository));
        Mockito.verify(repository).save(person);
        assertThat(person).isEqualTo(invoked);
    }

    @DisplayName("Should save iterable parameter")
    @Test
    void shouldSaveIterableParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("iterable", List.class);
        Person person = Person.builder().build();
        Mockito.when(repository.saveAll(List.of(person))).thenReturn(List.of(person));
        Object invoked = AnnotationOperation.SAVE.invoke(new AnnotationOperation.Operation(method, new Object[]{List.of(person)}, repository));
        Mockito.verify(repository).saveAll(List.of(person));
        assertThat(List.of(person)).isEqualTo(invoked);
    }


    @DisplayName("Should save array parameter")
    @Test
    void shouldSaveArrayParameter() throws Throwable {
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Person person = Person.builder().withName("Ada").withAge(12).build();
        Mockito.when(repository.saveAll(List.of(person))).thenReturn(List.of(person));
        Person[] invoked = (Person[]) AnnotationOperation.SAVE.invoke(new AnnotationOperation.Operation(method, new Object[]{new Person[]{person}},
                repository));
        Mockito.verify(repository).saveAll(List.of(person));
        assertThat(invoked).contains(person);
    }

    @DisplayName("Should equals annotation operation")
    @Test
    void shouldEqualsAnnotationOperation() throws NoSuchMethodException {
        Person person = Person.builder().build();
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Object[] params = {new Person[]{person}};
        AnnotationOperation.Operation operation = new AnnotationOperation.Operation(method, params,
                repository);

        AnnotationOperation.Operation operation2 = new AnnotationOperation.Operation(method, params,
                repository);

        assertThat(operation).isEqualTo(operation2);
        assertThat(operation).isEqualTo(operation);
        assertThat(operation).isNotEqualTo(123);
    }

    @DisplayName("Should hashcode annotation operation")
    @Test
    void shouldHashcodeAnnotationOperation() throws NoSuchMethodException {
        Person person = Person.builder().build();
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Object[] params = {new Person[]{person}};
        AnnotationOperation.Operation operation = new AnnotationOperation.Operation(method, params,
                repository);

        AnnotationOperation.Operation operation2 = new AnnotationOperation.Operation(method, params,
                repository);

        assertThat(operation.hashCode()).isEqualTo(operation2.hashCode());
    }

    @DisplayName("Should to string annotation operation")
    @Test
    void shouldToStringAnnotationOperation() throws NoSuchMethodException {
        Person person = Person.builder().build();
        Method method = PersonRepository.class.getDeclaredMethod("array", Person[].class);
        Object[] params = {new Person[]{person}};
        AnnotationOperation.Operation operation = new AnnotationOperation.Operation(method, params,
                repository);

        assertThat(operation.toString()).isNotEmpty();
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
