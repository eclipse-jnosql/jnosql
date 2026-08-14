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

import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueEntityConverter;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.keyvalue.MockProducer;
import org.eclipse.jnosql.mapping.keyvalue.entities.PersonStatisticRepository;
import org.eclipse.jnosql.mapping.keyvalue.entities.User;
import org.eclipse.jnosql.mapping.keyvalue.spi.KeyValueExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
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
    private KeyValueRepositoryProducer producer;

    @BeforeEach
    void setUp() {
        this.template = Mockito.mock(KeyValueTemplate.class);
        this.userRepository = producer.get(UserRepository.class, template);
    }




























    public interface BaseQuery<T> {

        @Query("get @key")
        List<T> key(@Param("key") String name);

        default List<T> poliana() {
            return this.key("Poliana");
        }
    }

    @Nested
    @DisplayName("When the proxy invokes the template")
    class WhenTheProxyInvokesTemplate {

        @Test
        @DisplayName("Should save")
        void shouldSave() {
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            User user = new User("ada", "Ada", 10);
            when(template.insert(user)).thenReturn(user);
            userRepository.save(user);
            Mockito.verify(template).insert(captor.capture());
            User value = captor.getValue();
            assertThat(value).isEqualTo(user);
        }

        @Test
        @DisplayName("Should save iterable")
        void shouldSaveIterable() {
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            User user = new User("ada", "Ada", 10);
            when(template.insert(user)).thenReturn(user);
            userRepository.saveAll(Collections.singletonList(user));
            Mockito.verify(template).insert(captor.capture());
            User value = captor.getValue();
            assertThat(value).isEqualTo(user);
        }

        @Test
        @DisplayName("Should insert")
        void shouldInsert() {
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            User user = new User("ada", "Ada", 10);
            when(template.insert(user)).thenReturn(user);
            userRepository.insert(user);
            Mockito.verify(template).insert(captor.capture());
            User value = captor.getValue();
            assertThat(value).isEqualTo(user);
        }

        @Test
        @DisplayName("Should insert iterable")
        void shouldInsertIterable() {
            ArgumentCaptor<Iterable> captor = ArgumentCaptor.forClass(Iterable.class);

            User user = new User("ada", "Ada", 10);
            userRepository.insertAll(Collections.singletonList(user));
            Mockito.verify(template).insert(captor.capture());
            User value = (User) captor.getValue().iterator().next();
            assertThat(value).isEqualTo(user);
        }

        @Test
        @DisplayName("Should update")
        void shouldUpdate() {
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            User user = new User("ada", "Ada", 10);
            when(template.update(user)).thenReturn(user);
            userRepository.update(user);
            Mockito.verify(template).update(captor.capture());
            User value = captor.getValue();
            assertThat(value).isEqualTo(user);
        }

        @Test
        @DisplayName("Should update iterable")
        void shouldUpdateIterable() {
            ArgumentCaptor<Iterable> captor = ArgumentCaptor.forClass(Iterable.class);

            User user = new User("ada", "Ada", 10);
            userRepository.updateAll(Collections.singletonList(user));
            Mockito.verify(template).update(captor.capture());
            User value = (User) captor.getValue().iterator().next();
            assertThat(value).isEqualTo(user);
        }

        @Test
        @DisplayName("Should delete")
        void shouldDelete() {
            userRepository.deleteById("key");
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            Mockito.verify(template).delete(Mockito.eq(User.class), captor.capture());
            assertThat(captor.getValue()).isEqualTo("key");
        }

        @Test
        @DisplayName("Should delete iterable")
        void shouldDeleteIterable() {
            userRepository.deleteByIdIn(Collections.singletonList("key"));
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            Mockito.verify(template).delete(Mockito.eq(User.class), captor.capture());
            assertThat(captor.getValue()).isEqualTo("key");
        }

        @Test
        @DisplayName("Should delete entity")
        void shouldDeleteEntity() {
            User user = new User("ada", "Ada", 10);
            userRepository.delete(user);
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            Mockito.verify(template).delete(Mockito.eq(User.class), captor.capture());
            assertThat(captor.getValue()).isEqualTo("ada");
        }

        @Test
        @DisplayName("Should delete entities")
        void shouldDeleteEntities() {
            User user = new User("ada", "Ada", 10);
            userRepository.deleteAll(Collections.singletonList(user));
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            Mockito.verify(template).delete(Mockito.eq(User.class), captor.capture());
            assertThat(captor.getValue()).isEqualTo("ada");
        }

        @Test
        @DisplayName("Should find by ID")
        void shouldFindById() {
            User user = new User("ada", "Ada", 10);
            when(template.find(User.class, "key")).thenReturn(
                    Optional.of(user));

            assertThat(userRepository.findById("key").get()).isEqualTo(user);
        }

        @Test
        @DisplayName("Should exists by ID")
        void shouldExistsById() {
            User user = new User("ada", "Ada", 10);
            when(template.find(User.class, "key")).thenReturn(Optional.of(user));

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(userRepository.existsById("key")).isTrue();
                softly.assertThat(userRepository.existsById("non-exist")).isFalse();
            });
        }

        @Test
        @DisplayName("Should find by ID iterable")
        void shouldFindByIdIterable() {
            User user = new User("ada", "Ada", 10);
            User user2 = new User("ada", "Ada", 10);
            List<String> keys = Arrays.asList("key", "key2");
            when(template.find(User.class, "key")).thenReturn(Optional.of(user));
            when(template.find(User.class, "key2")).thenReturn(Optional.of(user2));

            assertThat(userRepository.findByIdIn(keys)).contains(user, user2);
        }

        @Test
        @DisplayName("Should return error when execute method query")
        void shouldReturnErrorWhenExecuteMethodQuery() {
            assertThatThrownBy(() -> userRepository.findByName("name")).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return to string")
        void shouldReturnToString() {
            assertThat(userRepository.toString()).isNotNull();
        }

        @Test
        @DisplayName("Should return hash code")
        void shouldReturnHasCode() {
            assertThat(userRepository.hashCode()).isEqualTo(userRepository.hashCode());
        }

        @Test
        @DisplayName("Should return unsupported operation exception")
        void shouldReturnUnsupportedOperationException() {
            assertThatThrownBy(() -> userRepository.findAll()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.countBy()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.findAll(null, null)).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.deleteAll()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.countByName("name")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.find("name")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.deleteByAge(10)).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should execute custom repository")
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
        @DisplayName("Should insert using annotation")
        void shouldInsertUsingAnnotation(){
            User user = new User("12", "Poliana", 30);
            when(template.insert(user)).thenReturn(user);
            userRepository.insertUser(user);
            Mockito.verify(template).insert(user);
        }

        @Test
        @DisplayName("Should update using annotation")
        void shouldUpdateUsingAnnotation(){
            User user = new User("12", "Poliana", 30);
            when(template.update(user)).thenReturn(user);
            userRepository.updateUser(user);
            Mockito.verify(template).update(user);
        }

        @Test
        @DisplayName("Should delete using annotation")
        void shouldDeleteUsingAnnotation(){
            User user = new User("12", "Poliana", 30);
            userRepository.deleteUser(user);
            Mockito.verify(template).delete(user);
        }

        @Test
        @DisplayName("Should save using annotation")
        void shouldSaveUsingAnnotation(){
            User user = new User("12", "Poliana", 30);
            when(template.insert(user)).thenReturn(user);
            userRepository.saveUser(user);
            Mockito.verify(template).insert(user);
        }

        @Test
        @DisplayName("Should return not supported")
        void shouldReturnNotSupported(){
            assertThatThrownBy(() -> userRepository.existsByName("Ada")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.findByAge(10)).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.find("Ada")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.deleteByAge(10)).isInstanceOf(UnsupportedOperationException.class);
        }

    }

}
