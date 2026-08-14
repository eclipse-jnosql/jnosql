/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import jakarta.data.exceptions.NonUniqueResultException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.nosql.Query;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.mapping.core.Converters;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Test for the Query on KeyValue when the Core Query is Select type")
public class QuerySelectTemplateTest {

    @Inject
    private KeyValueEntityConverter converter;

    @Inject
    private KeyValueEventPersistManager eventManager;

    @Mock
    private BucketManager manager;

    private KeyValueTemplate template;


    @BeforeEach
    void setUp() {
        Instance<BucketManager> instance = Mockito.mock(Instance.class);
        when(instance.get()).thenReturn(manager);
        this.template = new DefaultKeyValueTemplate(converter, instance, eventManager);
    }

    @Nested
    @DisplayName("When the select query executes")
    class WhenTheSelectQueryExecutes {

        @Test
        @DisplayName("Should return error when query is null")
        void shouldReturnErrorOnQueryThatIsNull() {
            assertThatThrownBy(() -> template.query(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should return error when update query")
        void shouldReturnErrorOnUpdateQuery() {
            assertThatThrownBy(() -> template.query("UPDATE User set name = 'Otavio' where id = 123")).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Error When Select Is Not Support Key Value")
        @ParameterizedTest
        @ValueSource(strings = {"SELECT name, age FROM User", "FROM User", "From User skip 10", "From User limit 10", "From User ORDER BY name",
                "From User ORDER BY name DESC", "From User ORDER BY name ASC", "select count(this) FROM User"})
        void shouldErrorWhenSelectIsNotSupportKeyValue(String text) {
            assertThatThrownBy(() -> template.query(text)).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Error When Attribute Is Not Id")
        @ParameterizedTest
        @ValueSource(strings = {"FROM User where name = 'Ada'",
                "FROM User where age > 10",
                "FROM User where age < 10",
                "FROM User where age <= 10",
                "FROM User where name like 'Otavio'"})
        void shouldErrorWhenAttributeIsNotId(String text) {
            assertThatThrownBy(() -> template.query(text)).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Error When Id When Not Condition")
        @ParameterizedTest
        @ValueSource(strings = {
                "FROM User where nickname > 10",
                "FROM User where nickname < 10",
                "FROM User where nickname <= 10",
                "FROM User where nickname like 'Otavio'"})
        void shouldErrorWhenIdWhenNotCondition(String text){
            assertThatThrownBy(() -> template.query(text)).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Return Error When Select Call Update")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname = 'Otavio'"})
        void shouldReturnErrorWhenSelectCallUpdate(String text) {
            Query query = template.query(text);
            assertThatThrownBy(query::executeUpdate).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Return Empty When Select Literal Single Value")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname = 'Otavio'"})
        void shouldReturnEmptyWhenSelectLiteralSingleValue(String text) {
            Mockito.when(manager.get("Otavio"))
                    .thenReturn(Optional.empty());

            Query query = template.query(text);
            Optional<User> user = query.singleResult();
            SoftAssertions.assertSoftly(soft -> soft.assertThat(user).isEmpty());
        }

        @DisplayName("Should Select Literal Single Value")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname = 'Otavio'"})
        void shouldSelectLiteralSingleValue(String text) {

            Mockito.when(manager.get("Otavio"))
                    .thenReturn(Optional.of(Value.of(new User("Otavio", "Otavio", 27))));

            Query query = template.query(text);
            Optional<User> user = query.singleResult();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(user).isPresent();
                soft.assertThat(user.orElseThrow().getNickname()).isEqualTo("Otavio");
                Mockito.verify(manager).get("Otavio");
            });
        }

        @DisplayName("Should Select Literal Single Value List")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname = 'Otavio'"})
        void shouldSelectLiteralSingleValueList(String text) {

            Mockito.when(manager.get("Otavio"))
                    .thenReturn(Optional.of(Value.of(new User("Otavio", "Otavio", 27))));

            Query query = template.query(text);
            List<User> users = query.result();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(users).isNotEmpty();
                soft.assertThat(users.getFirst().getNickname()).isEqualTo("Otavio");
                Mockito.verify(manager).get("Otavio");
            });
        }

        @DisplayName("Should Select Literal Single Value Stream")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname = 'Otavio'"})
        void shouldSelectLiteralSingleValueStream(String text) {

            Mockito.when(manager.get("Otavio"))
                    .thenReturn(Optional.of(Value.of(new User("Otavio", "Otavio", 27))));

            Query query = template.query(text);
            Stream<User> users = query.stream();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(users).isNotEmpty().contains(new User("Otavio", "Otavio", 27));
                Mockito.verify(manager).get("Otavio");
            });
        }

        @DisplayName("Should Select In With Single Value")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname IN ('Otavio')"})
        void shouldSelectInWithSingleValue(String text) {

            Mockito.when(manager.get("Otavio"))
                    .thenReturn(Optional.of(Value.of(new User("Otavio", "Otavio", 27))));

            Query query = template.query(text);
            Optional<User> user = query.singleResult();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(user).isPresent();
                soft.assertThat(user.orElseThrow().getNickname()).isEqualTo("Otavio");
                Mockito.verify(manager).get("Otavio");
            });
        }

        @DisplayName("Should Select In With Single Value Empty")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname IN ('Otavio')"})
        void shouldSelectInWithSingleValueEmpty(String text) {

            Mockito.when(manager.get("Otavio")).thenReturn(Optional.empty());

            Query query = template.query(text);
            Optional<User> user = query.singleResult();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(user).isEmpty();
                Mockito.verify(manager).get("Otavio");
            });
        }

        @DisplayName("Should Select In With Empty")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname IN ('Otavio', 'Maria')"})
        void shouldSelectInWithEmpty(String text) {

            Mockito.when(manager.get("Otavio"))
                    .thenReturn(Optional.of(Value.of(new User("Otavio", "Otavio", 27))));
            Mockito.when(manager.get("Maria"))
                    .thenReturn(Optional.of(Value.of(new User("Maria", "Maria", 59))));

            Query query = template.query(text);
            assertThatThrownBy(query::singleResult).isInstanceOf(NonUniqueResultException.class);
        }

        @DisplayName("Should In List")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname IN ('Otavio', 'Maria')"})
        void shouldInList(String text) {

            Mockito.when(manager.get("Otavio"))
                    .thenReturn(Optional.of(Value.of(new User("Otavio", "Otavio", 27))));
            Mockito.when(manager.get("Maria"))
                    .thenReturn(Optional.of(Value.of(new User("Maria", "Maria", 59))));

            Query query = template.query(text);
            List<User> users = query.result();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(users).isNotEmpty().hasSize(2);
                soft.assertThat(users).map( User::getNickname).contains("Otavio", "Maria");
            });
        }

        @DisplayName("Should In Stream")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname IN ('Otavio', 'Maria')"})
        void shouldInStream(String text) {

            Mockito.when(manager.get("Otavio"))
                    .thenReturn(Optional.of(Value.of(new User("Otavio", "Otavio", 27))));
            Mockito.when(manager.get("Maria"))
                    .thenReturn(Optional.of(Value.of(new User("Maria", "Maria", 59))));

            Query query = template.query(text);
            Stream<User> users = query.stream();
            SoftAssertions.assertSoftly(soft -> soft.assertThat(users).isNotEmpty().hasSize(2).map( User::getNickname).contains("Otavio", "Maria"));
        }

        @DisplayName("Should Error When Parameter Is Missing On Equals")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname = :param"})
        void shouldErrorWhenParameterIsMissingOnEquals(String text){
            Query query = template.query(text);

            assertThatThrownBy(query::singleResult).isInstanceOf(QueryException.class);
            assertThatThrownBy(query::result).isInstanceOf(QueryException.class);
            assertThatThrownBy(query::stream).isInstanceOf(QueryException.class);
        }

        @DisplayName("Should Error When Parameter Is Missing On In")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname in (:param)"})
        void shouldErrorWhenParameterIsMissingOnIn(String text){
            Query query = template.query(text);

            assertThatThrownBy(query::singleResult).isInstanceOf(QueryException.class);
            assertThatThrownBy(query::result).isInstanceOf(QueryException.class);
            assertThatThrownBy(query::stream).isInstanceOf(QueryException.class);
        }

        @DisplayName("Should Bind Parameter Equals Single Result")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname = :nickname"})
        void shouldBindParameterEqualsSingleResult(String text){
            Mockito.when(manager.get("Otavio"))
                    .thenReturn(Optional.of(Value.of(new User("Otavio", "Otavio", 27))));

            Query query = template.query(text);
            query.bind("nickname", "Otavio");
            Optional<User> user = query.singleResult();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(user).isPresent();
                soft.assertThat(user.orElseThrow().getNickname()).isEqualTo("Otavio");
                Mockito.verify(manager).get("Otavio");
            });
        }

        @DisplayName("Should Bind Parameter Index Equals Single Result")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname = ?1"})
        void shouldBindParameterIndexEqualsSingleResult(String text){
            Mockito.when(manager.get("Otavio"))
                    .thenReturn(Optional.of(Value.of(new User("Otavio", "Otavio", 27))));

            Query query = template.query(text);
            query.bind(1, "Otavio");
            Optional<User> user = query.singleResult();
            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(user).isPresent();
                soft.assertThat(user.orElseThrow().getNickname()).isEqualTo("Otavio");
                Mockito.verify(manager).get("Otavio");
            });
        }

        @DisplayName("Should Bind Return When Index Is Negative")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname = ?1"})
        void shouldBindReturnWhenIndexIsNegative(String text){
            Query query = template.query(text);
            assertThatThrownBy(() -> query.bind(-1, "Otavio")).isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("Should Bind Stream")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname in (?1, :second, 'Maria')"})
        void shouldBindStream(String text){
            Mockito.when(manager.get("Otavio"))
                    .thenReturn(Optional.of(Value.of(new User("Otavio", "Otavio", 27))));
            Mockito.when(manager.get("Maria"))
                    .thenReturn(Optional.of(Value.of(new User("Maria", "Maria", 59))));
            Mockito.when(manager.get("Ada"))
                    .thenReturn(Optional.of(Value.of(new User("Ada", "Ada", 30))));

            Query query = template.query(text);
            query.bind("second", "Otavio");
            query.bind(1, "Ada");
            Stream<User> users = query.stream();
            SoftAssertions.assertSoftly(soft -> soft.assertThat(users).isNotEmpty().hasSize(3).map( User::getNickname).contains("Otavio", "Maria", "Ada"));
        }

        @DisplayName("Should Bind List")
        @ParameterizedTest
        @ValueSource(strings = { "FROM User WHERE nickname in (?1, :second, 'Maria')"})
        void shouldBindList(String text){

            Mockito.when(manager.get("Otavio"))
                    .thenReturn(Optional.of(Value.of(new User("Otavio", "Otavio", 27))));
            Mockito.when(manager.get("Maria"))
                    .thenReturn(Optional.of(Value.of(new User("Maria", "Maria", 59))));
            Mockito.when(manager.get("Ada"))
                    .thenReturn(Optional.of(Value.of(new User("Ada", "Ada", 30))));

            Query query = template.query(text);
            query.bind("second", "Otavio");
            query.bind(1, "Ada");
            List<User> users = query.result();
            SoftAssertions.assertSoftly(soft -> soft.assertThat(users).isNotEmpty().hasSize(3).map( User::getNickname).contains("Otavio", "Maria"));
        }
    }

}
