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

        @DisplayName("Should Save")
        @Test
        void shouldSave() {
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            User user = new User("ada", "Ada", 10);
            when(template.insert(user)).thenReturn(user);
            userRepository.save(user);
            Mockito.verify(template).insert(captor.capture());
            User value = captor.getValue();
            assertThat(value).isEqualTo(user);
        }

        @DisplayName("Should Save Iterable")
        @Test
        void shouldSaveIterable() {
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            User user = new User("ada", "Ada", 10);
            when(template.insert(user)).thenReturn(user);
            userRepository.saveAll(Collections.singletonList(user));
            Mockito.verify(template).insert(captor.capture());
            User value = captor.getValue();
            assertThat(value).isEqualTo(user);
        }

        @DisplayName("Should Insert")
        @Test
        void shouldInsert() {
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            User user = new User("ada", "Ada", 10);
            when(template.insert(user)).thenReturn(user);
            userRepository.insert(user);
            Mockito.verify(template).insert(captor.capture());
            User value = captor.getValue();
            assertThat(value).isEqualTo(user);
        }

        @DisplayName("Should Insert Iterable")
        @Test
        void shouldInsertIterable() {
            ArgumentCaptor<Iterable> captor = ArgumentCaptor.forClass(Iterable.class);

            User user = new User("ada", "Ada", 10);
            userRepository.insertAll(Collections.singletonList(user));
            Mockito.verify(template).insert(captor.capture());
            User value = (User) captor.getValue().iterator().next();
            assertThat(value).isEqualTo(user);
        }

        @DisplayName("Should Update")
        @Test
        void shouldUpdate() {
            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

            User user = new User("ada", "Ada", 10);
            when(template.update(user)).thenReturn(user);
            userRepository.update(user);
            Mockito.verify(template).update(captor.capture());
            User value = captor.getValue();
            assertThat(value).isEqualTo(user);
        }

        @DisplayName("Should Update Iterable")
        @Test
        void shouldUpdateIterable() {
            ArgumentCaptor<Iterable> captor = ArgumentCaptor.forClass(Iterable.class);

            User user = new User("ada", "Ada", 10);
            userRepository.updateAll(Collections.singletonList(user));
            Mockito.verify(template).update(captor.capture());
            User value = (User) captor.getValue().iterator().next();
            assertThat(value).isEqualTo(user);
        }

        @DisplayName("Should Delete")
        @Test
        void shouldDelete() {
            userRepository.deleteById("key");
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            Mockito.verify(template).delete(Mockito.eq(User.class), captor.capture());
            assertThat(captor.getValue()).isEqualTo("key");
        }

        @DisplayName("Should Delete Iterable")
        @Test
        void shouldDeleteIterable() {
            userRepository.deleteByIdIn(Collections.singletonList("key"));
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            Mockito.verify(template).delete(Mockito.eq(User.class), captor.capture());
            assertThat(captor.getValue()).isEqualTo("key");
        }

        @DisplayName("Should Delete Entity")
        @Test
        void shouldDeleteEntity() {
            User user = new User("ada", "Ada", 10);
            userRepository.delete(user);
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            Mockito.verify(template).delete(Mockito.eq(User.class), captor.capture());
            assertThat(captor.getValue()).isEqualTo("ada");
        }

        @DisplayName("Should Delete Entities")
        @Test
        void shouldDeleteEntities() {
            User user = new User("ada", "Ada", 10);
            userRepository.deleteAll(Collections.singletonList(user));
            ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
            Mockito.verify(template).delete(Mockito.eq(User.class), captor.capture());
            assertThat(captor.getValue()).isEqualTo("ada");
        }

        @DisplayName("Should Find By Id")
        @Test
        void shouldFindById() {
            User user = new User("ada", "Ada", 10);
            when(template.find(User.class, "key")).thenReturn(
                    Optional.of(user));

            assertThat(userRepository.findById("key").get()).isEqualTo(user);
        }

        @DisplayName("Should Exists By Id")
        @Test
        void shouldExistsById() {
            User user = new User("ada", "Ada", 10);
            when(template.find(User.class, "key")).thenReturn(Optional.of(user));

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(userRepository.existsById("key")).isTrue();
                softly.assertThat(userRepository.existsById("non-exist")).isFalse();
            });
        }

        @DisplayName("Should Find By Id Iterable")
        @Test
        void shouldFindByIdIterable() {
            User user = new User("ada", "Ada", 10);
            User user2 = new User("ada", "Ada", 10);
            List<String> keys = Arrays.asList("key", "key2");
            when(template.find(User.class, "key")).thenReturn(Optional.of(user));
            when(template.find(User.class, "key2")).thenReturn(Optional.of(user2));

            assertThat(userRepository.findByIdIn(keys)).contains(user, user2);
        }

        @DisplayName("Should Return Error When Execute Method Query")
        @Test
        void shouldReturnErrorWhenExecuteMethodQuery() {
            assertThatThrownBy(() -> userRepository.findByName("name")).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Return To String")
        @Test
        void shouldReturnToString() {
            assertThat(userRepository.toString()).isNotNull();
        }

        @DisplayName("Should Return Has Code")
        @Test
        void shouldReturnHasCode() {
            assertThat(userRepository.hashCode()).isEqualTo(userRepository.hashCode());
        }

        @DisplayName("Should Return Unsupported Operation Exception")
        @Test
        void shouldReturnUnsupportedOperationException() {
            assertThatThrownBy(() -> userRepository.findAll()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.countBy()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.findAll(null, null)).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.deleteAll()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.countByName("name")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.find("name")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.deleteByAge(10)).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Execute Custom Repository")
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

        @DisplayName("Should Insert Using Annotation")
        @Test
        void shouldInsertUsingAnnotation(){
            User user = new User("12", "Poliana", 30);
            when(template.insert(user)).thenReturn(user);
            userRepository.insertUser(user);
            Mockito.verify(template).insert(user);
        }

        @DisplayName("Should Update Using Annotation")
        @Test
        void shouldUpdateUsingAnnotation(){
            User user = new User("12", "Poliana", 30);
            when(template.update(user)).thenReturn(user);
            userRepository.updateUser(user);
            Mockito.verify(template).update(user);
        }

        @DisplayName("Should Delete Using Annotation")
        @Test
        void shouldDeleteUsingAnnotation(){
            User user = new User("12", "Poliana", 30);
            userRepository.deleteUser(user);
            Mockito.verify(template).delete(user);
        }

        @DisplayName("Should Save Using Annotation")
        @Test
        void shouldSaveUsingAnnotation(){
            User user = new User("12", "Poliana", 30);
            when(template.insert(user)).thenReturn(user);
            userRepository.saveUser(user);
            Mockito.verify(template).insert(user);
        }

        @DisplayName("Should Return Not Supported")
        @Test
        void shouldReturnNotSupported(){
            assertThatThrownBy(() -> userRepository.existsByName("Ada")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.findByAge(10)).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.find("Ada")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> userRepository.deleteByAge(10)).isInstanceOf(UnsupportedOperationException.class);
        }

    }

}