/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueEntityConverter;
import org.eclipse.jnosql.mapping.keyvalue.MockProducer;
import org.eclipse.jnosql.mapping.keyvalue.spi.KeyValueExtension;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.keyvalue.entities.PersonStatisticRepository;
import org.eclipse.jnosql.mapping.keyvalue.entities.User;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;


@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(PersonStatisticRepository.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
class KeyValueRepositoryProxyTest {


    private KeyValueTemplate template;

    private UserRepository userRepository;

    @Inject
    private EntitiesMetadata entitiesMetadata;
    @BeforeEach
    void setUp() {
        this.template = Mockito.mock(KeyValueTemplate.class);
        KeyValueRepositoryProxy handler = new KeyValueRepositoryProxy(UserRepository.class, entitiesMetadata, template);
        userRepository = (UserRepository) Proxy.newProxyInstance(UserRepository.class.getClassLoader(),
                new Class[]{UserRepository.class},
                handler);
    }

    @Test
    void shouldSave() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        User user = new User("ada", "Ada", 10);
        userRepository.save(user);
        Mockito.verify(template).insert(captor.capture());
        User value = captor.getValue();
        assertEquals(user, value);
    }

    @Test
    void shouldSaveIterable() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        User user = new User("ada", "Ada", 10);
        userRepository.saveAll(Collections.singletonList(user));
        Mockito.verify(template).insert(captor.capture());
        User value = captor.getValue();
        assertEquals(user, value);
    }

    @Test
    void shouldInsert() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        User user = new User("ada", "Ada", 10);
        userRepository.insert(user);
        Mockito.verify(template).insert(captor.capture());
        User value = captor.getValue();
        assertEquals(user, value);
    }


    @Test
    void shouldInsertIterable() {
        ArgumentCaptor<Iterable> captor = ArgumentCaptor.forClass(Iterable.class);

        User user = new User("ada", "Ada", 10);
        userRepository.insertAll(Collections.singletonList(user));
        Mockito.verify(template).insert(captor.capture());
        User value = (User) captor.getValue().iterator().next();
        assertEquals(user, value);
    }

    @Test
    void shouldUpdate() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        User user = new User("ada", "Ada", 10);
        userRepository.update(user);
        Mockito.verify(template).update(captor.capture());
        User value = captor.getValue();
        assertEquals(user, value);
    }


    @Test
    void shouldUpdateIterable() {
        ArgumentCaptor<Iterable> captor = ArgumentCaptor.forClass(Iterable.class);

        User user = new User("ada", "Ada", 10);
        userRepository.updateAll(Collections.singletonList(user));
        Mockito.verify(template).update(captor.capture());
        User value = (User) captor.getValue().iterator().next();
        assertEquals(user, value);
    }


    @Test
    void shouldDelete() {
        userRepository.deleteById("key");
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(template).delete(Mockito.eq(User.class), captor.capture());
        assertEquals("key", captor.getValue());
    }

    @Test
    void shouldDeleteIterable() {
        userRepository.deleteByIdIn(Collections.singletonList("key"));
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(template).delete(Mockito.eq(User.class), captor.capture());
        assertEquals("key", captor.getValue());
    }

    @Test
    void shouldDeleteEntity() {
        User user = new User("ada", "Ada", 10);
        userRepository.delete(user);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(template).delete(Mockito.eq(User.class), captor.capture());
        assertEquals("ada", captor.getValue());
    }

    @Test
    void shouldDeleteEntities() {
        User user = new User("ada", "Ada", 10);
        userRepository.deleteAll(Collections.singletonList(user));
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(template).delete(Mockito.eq(User.class), captor.capture());
        assertEquals("ada", captor.getValue());
    }

    @Test
    void shouldFindById() {
        User user = new User("ada", "Ada", 10);
        when(template.find(User.class, "key")).thenReturn(
                Optional.of(user));

        assertEquals(user, userRepository.findById("key").get());
    }

    @Test
    void shouldExistsById() {
        User user = new User("ada", "Ada", 10);
        when(template.find(User.class, "key")).thenReturn(Optional.of(user));

        assertThat(userRepository.existsById("key")).isTrue();
        assertThat(userRepository.existsById("non-exist")).isFalse();
    }

    @Test
    void shouldFindByIdIterable() {
        User user = new User("ada", "Ada", 10);
        User user2 = new User("ada", "Ada", 10);
        List<String> keys = Arrays.asList("key", "key2");
        when(template.find(User.class, "key")).thenReturn(Optional.of(user));
        when(template.find(User.class, "key2")).thenReturn(Optional.of(user2));

        assertThat(userRepository.findByIdIn(keys)).contains(user, user2);
    }

    @Test
    void shouldReturnErrorWhenExecuteMethodQuery() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userRepository.findByName("name"));
    }

    @Test
    void shouldReturnToString() {
        assertNotNull(userRepository.toString());
    }

    @Test
    void shouldReturnHasCode() {
        assertEquals(userRepository.hashCode(), userRepository.hashCode());
    }

    @Test
    void shouldReturnUnsupportedOperationException() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userRepository.findAll());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userRepository.countBy());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userRepository.findAll(null, null));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userRepository.deleteAll());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userRepository.countByName("name"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userRepository.find("name"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userRepository.deleteByAge(10));
    }


    @Test
    void shouldExecuteCustomRepository(){
        PersonStatisticRepository.PersonStatistic statistics = userRepository
                .statistics("Salvador");
        assertThat(statistics).isNotNull();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(statistics.average()).isEqualTo(26);
            softly.assertThat(statistics.sum()).isEqualTo(26);
            softly.assertThat(statistics.max()).isEqualTo(26);
            softly.assertThat(statistics.min()).isEqualTo(26);
            softly.assertThat(statistics.count()).isEqualTo(1);
            softly.assertThat(statistics.city()).isEqualTo("Salvador");
        });
    }

    @Test
    void shouldInsertUsingAnnotation(){
        User user = new User("12", "Poliana", 30);
        userRepository.insertUser(user);
        Mockito.verify(template).insert(user);
    }

    @Test
    void shouldUpdateUsingAnnotation(){
        User user = new User("12", "Poliana", 30);
        userRepository.updateUser(user);
        Mockito.verify(template).update(user);
    }

    @Test
    void shouldDeleteUsingAnnotation(){
        User user = new User("12", "Poliana", 30);
        userRepository.deleteUser(user);
        Mockito.verify(template).delete(User.class, "12");
    }

    @Test
    void shouldSaveUsingAnnotation(){
        User user = new User("12", "Poliana", 30);
        userRepository.saveUser(user);
        Mockito.verify(template).insert(user);
    }

    @Test
    void shouldReturnNotSupported(){
      Assertions.assertThrows(UnsupportedOperationException.class, () -> userRepository.existByName("Ada"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userRepository.findByAge(10));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userRepository.find("Ada"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> userRepository.deleteByAge(10));
    }

    public interface BaseQuery<T> {

        @Query("get @key")
        List<T> key(@Param("key") String name);

        default List<T> poliana() {
            return this.key("Poliana");
        }
    }

    public interface UserRepository extends NoSQLRepository<User, String>, CrudRepository<User, String>, BaseQuery<User>, PersonStatisticRepository {

        Optional<User> findByName(String name);

        @Query("get \"12\"")
        Optional<User> findByQuery();


        @Query("get @id")
        Optional<User> querybyKey(@Param("id") String key);
        default Optional<User> otavio() {
            return querybyKey("otavio");
        }

        @Query("get @id")
        Optional<User> findByQuery(@Param("id") String id);

        @Insert
        User insertUser(User user);
        @Update
        User updateUser(User user);

        @Save
        User saveUser(User user);

        @Delete
        void deleteUser(User user);

        void existByName(String name);

        User findByAge(Integer age);

        User find(String name);

        void deleteByAge(Integer age);

        int countByName(String name);
    }

}