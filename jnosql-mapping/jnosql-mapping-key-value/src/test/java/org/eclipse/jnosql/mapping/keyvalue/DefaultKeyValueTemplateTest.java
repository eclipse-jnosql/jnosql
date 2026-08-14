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
package org.eclipse.jnosql.mapping.keyvalue;

import jakarta.enterprise.inject.Instance;
import org.assertj.core.api.SoftAssertions;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.communication.keyvalue.KeyValueEntity;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.keyvalue.entities.Person;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static java.util.Collections.singletonList;
import static java.util.stream.StreamSupport.stream;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DefaultKeyValueTemplateTest {

    private static final String KEY = "otaviojava";
    @Inject
    private KeyValueEntityConverter converter;

    @Inject
    private KeyValueEventPersistManager eventManager;

    @Mock
    private BucketManager manager;

    @Captor
    private ArgumentCaptor<KeyValueEntity> captor;

    private KeyValueTemplate template;


    @BeforeEach
    void setUp() {
        Instance<BucketManager> instance = Mockito.mock(Instance.class);
        when(instance.get()).thenReturn(manager);
        this.template = new DefaultKeyValueTemplate(converter, instance, eventManager);
    }

    @Nested
    @DisplayName("When the template handles entities")
    class WhenTheTemplateHandlesEntities {

        @DisplayName("Should Put")
        @Test
        void shouldPut() {
            User user = new User(KEY, "otavio", 27);
            template.put(user);
            Mockito.verify(manager).put(captor.capture());
            KeyValueEntity entity = captor.getValue();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(entity.key()).isEqualTo(KEY);
                softly.assertThat(entity.value()).isEqualTo(user);
            });
        }

        @DisplayName("Should Merge On Put")
        @Test
        void shouldMergeOnPut() {
            User user = new User(KEY, "otavio", 27);
            User result = template.put(user);
            assertThat(result).isSameAs(user);
        }

        @DisplayName("Should Put Iterable")
        @Test
        void shouldPutIterable() {
            User user = new User(KEY, "otavio", 27);
            template.put(singletonList(user));
            Mockito.verify(manager).put(captor.capture());
            KeyValueEntity entity = captor.getValue();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(entity.key()).isEqualTo(KEY);
                softly.assertThat(entity.value()).isEqualTo(user);
            });
        }

        @DisplayName("Should Put TTL")
        @Test
        void shouldPutTTL() {

            Duration duration = Duration.ofSeconds(2L);
            User user = new User(KEY, "otavio", 27);
            template.put(user, duration);

            Mockito.verify(manager).put(captor.capture(), Mockito.eq(duration));
            KeyValueEntity entity = captor.getValue();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(entity.key()).isEqualTo(KEY);
                softly.assertThat(entity.value()).isEqualTo(user);
            });
        }

        @DisplayName("Should Put TTLIterable")
        @Test
        void shouldPutTTLIterable() {

            Duration duration = Duration.ofSeconds(2L);
            User user = new User(KEY, "otavio", 27);
            template.put(singletonList(user), duration);

            Mockito.verify(manager).put(captor.capture(), Mockito.eq(duration));
            KeyValueEntity entity = captor.getValue();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(entity.key()).isEqualTo(KEY);
                softly.assertThat(entity.value()).isEqualTo(user);
            });
        }

        @DisplayName("Should Insert")
        @Test
        void shouldInsert() {
            User user = new User(KEY, "otavio", 27);
            template.insert(user);
            Mockito.verify(manager).put(captor.capture());
            KeyValueEntity entity = captor.getValue();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(entity.key()).isEqualTo(KEY);
                softly.assertThat(entity.value()).isEqualTo(user);
            });
        }

        @DisplayName("Should Insert Iterable")
        @Test
        void shouldInsertIterable() {
            User user = new User(KEY, "otavio", 27);
            template.insert(singletonList(user));
            Mockito.verify(manager).put(captor.capture());
            KeyValueEntity entity = captor.getValue();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(entity.key()).isEqualTo(KEY);
                softly.assertThat(entity.value()).isEqualTo(user);
            });
        }

        @DisplayName("Should Insert TTL")
        @Test
        void shouldInsertTTL() {

            Duration duration = Duration.ofSeconds(2L);
            User user = new User(KEY, "otavio", 27);
            template.insert(user, duration);

            Mockito.verify(manager).put(captor.capture(), Mockito.eq(duration));
            KeyValueEntity entity = captor.getValue();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(entity.key()).isEqualTo(KEY);
                softly.assertThat(entity.value()).isEqualTo(user);
            });
        }

        @DisplayName("Should Insert TTLIterable")
        @Test
        void shouldInsertTTLIterable() {

            Duration duration = Duration.ofSeconds(2L);
            User user = new User(KEY, "otavio", 27);
            template.insert(singletonList(user), duration);

            Mockito.verify(manager).put(captor.capture(), Mockito.eq(duration));
            KeyValueEntity entity = captor.getValue();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(entity.key()).isEqualTo(KEY);
                softly.assertThat(entity.value()).isEqualTo(user);
            });
        }

        @DisplayName("Should Update")
        @Test
        void shouldUpdate() {
            User user = new User(KEY, "otavio", 27);
            template.update(user);
            Mockito.verify(manager).put(captor.capture());
            KeyValueEntity entity = captor.getValue();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(entity.key()).isEqualTo(KEY);
                softly.assertThat(entity.value()).isEqualTo(user);
            });
        }

        @DisplayName("Should Update Iterable")
        @Test
        void shouldUpdateIterable() {
            User user = new User(KEY, "otavio", 27);
            template.update(singletonList(user));
            Mockito.verify(manager).put(captor.capture());
            KeyValueEntity entity = captor.getValue();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(entity.key()).isEqualTo(KEY);
                softly.assertThat(entity.value()).isEqualTo(user);
            });
        }

        @DisplayName("Should Get")
        @Test
        void shouldGet() {
            User user = new User(KEY, "otavio", 27);

            when(manager.get(KEY)).thenReturn(Optional.of(Value.of(user)));
            Optional<User> userOptional = template.get(KEY, User.class);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(userOptional.isPresent()).isTrue();
                softly.assertThat(userOptional.get()).isEqualTo(user);
            });
        }

        @DisplayName("Should Find By Id")
        @Test
        void shouldFindById() {
            User user = new User(KEY, "otavio", 27);
            when(manager.get(KEY)).thenReturn(Optional.of(Value.of(user)));
            Optional<User> userOptional = template.find(User.class, KEY);

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(userOptional.isPresent()).isTrue();
                softly.assertThat(userOptional.get()).isEqualTo(user);
            });
        }

        @DisplayName("Should Get Iterable")
        @Test
        void shouldGetIterable() {
            User user = new User(KEY, "otavio", 27);

            when(manager.get(KEY)).thenReturn(Optional.of(Value.of(user)));
            List<User> userOptional = stream(template.get(singletonList(KEY), User.class).spliterator(), false)
                    .toList();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(userOptional.isEmpty()).isFalse();
                softly.assertThat(userOptional.getFirst()).isEqualTo(user);
            });
        }

        @DisplayName("Should Return Empty Iterable")
        @Test
        void shouldReturnEmptyIterable() {
            User user = new User(KEY, "otavio", 27);

            when(manager.get(KEY)).thenReturn(Optional.empty());
            List<User> userOptional = stream(template.get(singletonList(KEY), User.class).spliterator(), false)
                    .toList();

            assertThat(userOptional.isEmpty()).isTrue();
        }

        @DisplayName("Should Remove")
        @Test
        void shouldRemove() {
            template.deleteByKey(KEY);
            Mockito.verify(manager).delete(KEY);
        }

        @DisplayName("Should Remove Using Entity")
        @Test
        void shouldRemoveUsingEntity() {
            User user = new User(KEY, "otavio", 27);
            template.delete(user);
            Mockito.verify(manager).delete(KEY);
        }

        @DisplayName("Should Return Error When Entity Is Null")
        @Test
        void shouldReturnErrorWhenEntityIsNull(){
            assertThatThrownBy(() -> template.delete((User)null)).isInstanceOf(NullPointerException.class);
        }

        @DisplayName("Should Remove By Id")
        @Test
        void shouldRemoveById() {
            template.delete(User.class, KEY);
            Mockito.verify(manager).delete(KEY);
        }

        @DisplayName("Should Remove Iterable")
        @Test
        void shouldRemoveIterable() {
            template.deleteByKeys(singletonList(KEY));
            Mockito.verify(manager).delete(singletonList(KEY));
        }

        @DisplayName("Should Return Unsupported Exception On Update")
        @Test
        void shouldReturnUnsupportedExceptionOnUpdate() {
            assertThatThrownBy(() -> template.update(Person.class)).isInstanceOf(UnsupportedOperationException.class);
        }

    }

}
