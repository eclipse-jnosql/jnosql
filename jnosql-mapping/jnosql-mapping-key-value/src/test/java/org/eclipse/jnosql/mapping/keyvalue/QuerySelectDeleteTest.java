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

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.nosql.Query;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Test for the Query on KeyValue when the Core Query that delete operations")
public class QuerySelectDeleteTest {

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
    @DisplayName("When the delete query executes")
    class WhenTheDeleteQueryExecutes {

        @DisplayName("Should Error When Delete Is Not Support Key Value")
        @ParameterizedTest
        @ValueSource(strings = {"DELETE FROM User"})
        void shouldErrorWhenDeleteIsNotSupportKeyValue(String text) {
            assertThatThrownBy(() -> template.query(text)).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Error When Attribute Is Not Id")
        @ParameterizedTest
        @ValueSource(strings = {"DELETE FROM User where name = 'Ada'",
                "DELETE FROM User where age > 10",
                "DELETE FROM User where age < 10",
                "DELETE FROM User where age <= 10",
                "DELETE FROM User where name like 'Otavio'"})
        void shouldErrorWhenAttributeIsNotId(String text) {
            assertThatThrownBy(() -> template.query(text)).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Error When Id When Not Condition")
        @ParameterizedTest
        @ValueSource(strings = {
                "DELETE FROM User where nickname > 10",
                "DELETE FROM User where nickname < 10",
                "DELETE FROM User where nickname <= 10",
                "DELETE FROM User where nickname like 'Otavio'"})
        void shouldErrorWhenIdWhenNotCondition(String text){
            assertThatThrownBy(() -> template.query(text)).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Return Error When Select Call Result")
        @ParameterizedTest
        @ValueSource(strings = { "DELETE FROM User WHERE nickname = 'Otavio'"})
        void shouldReturnErrorWhenSelectCallResult(String text) {
            Query query = template.query(text);
            assertThatThrownBy(query::singleResult).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(query::result).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(query::stream).isInstanceOf(UnsupportedOperationException.class);
        }

        @DisplayName("Should Execute Delete Literal")
        @ParameterizedTest
        @ValueSource(strings = { "DELETE FROM User WHERE nickname = 'Otavio'"})
        void shouldExecuteDeleteLiteral(String text) {

            Query query = template.query(text);
            query.executeUpdate();
            Mockito.verify(manager).delete("Otavio");
        }

        @DisplayName("Should Delete In Single Parameter")
        @ParameterizedTest
        @ValueSource(strings = { "DELETE FROM User WHERE nickname IN ('Otavio')"})
        void shouldDeleteInSingleParameter(String text) {
            Query query = template.query(text);
            query.executeUpdate();
            Mockito.verify(manager).delete("Otavio");
        }

        @DisplayName("Should Delete In Parameters")
        @ParameterizedTest
        @ValueSource(strings = { "DELETE FROM User WHERE nickname IN ('Otavio', 'Maria')"})
        void shouldDeleteInParameters(String text) {
            Query query = template.query(text);
            query.executeUpdate();
            Mockito.verify(manager).delete("Otavio");
            Mockito.verify(manager).delete("Maria");
        }

        @DisplayName("Should Error When Parameter Is Missing On Equals")
        @ParameterizedTest
        @ValueSource(strings = { "DELETE FROM User WHERE nickname = :param"})
        void shouldErrorWhenParameterIsMissingOnEquals(String text){
            Query query = template.query(text);

            assertThatThrownBy(query::executeUpdate).isInstanceOf(QueryException.class);
        }

        @DisplayName("Should Error When Parameter Is Missing On In")
        @ParameterizedTest
        @ValueSource(strings = { "DELETE FROM User WHERE nickname in (:param)"})
        void shouldErrorWhenParameterIsMissingOnIn(String text){
            Query query = template.query(text);
            assertThatThrownBy(query::executeUpdate).isInstanceOf(QueryException.class);
        }

        @DisplayName("Should Bind Parameter Equals Single Result")
        @ParameterizedTest
        @ValueSource(strings = { "DELETE FROM User WHERE nickname = :nickname"})
        void shouldBindParameterEqualsSingleResult(String text){


            Query query = template.query(text);
            query.bind("nickname", "Otavio");
            query.executeUpdate();
            Mockito.verify(manager).delete("Otavio");
        }

        @DisplayName("Should Bind Parameter Index Equals Single Result")
        @ParameterizedTest
        @ValueSource(strings = { "DELETE FROM User WHERE nickname = ?1"})
        void shouldBindParameterIndexEqualsSingleResult(String text){
            Mockito.when(manager.get("Otavio"))
                    .thenReturn(Optional.of(Value.of(new User("Otavio", "Otavio", 27))));

            Query query = template.query(text);
            query.bind(1, "Otavio");
            query.executeUpdate();
            Mockito.verify(manager).delete("Otavio");
        }

        @DisplayName("Should Bind Return When Index Is Negative")
        @ParameterizedTest
        @ValueSource(strings = { "DELETE FROM User WHERE nickname = ?1"})
        void shouldBindReturnWhenIndexIsNegative(String text){
            Query query = template.query(text);
            assertThatThrownBy(() -> query.bind(-1, "Otavio")).isInstanceOf(IllegalArgumentException.class);
        }

        @DisplayName("Should Bind Mix Of Delete")
        @ParameterizedTest
        @ValueSource(strings = { "DELETE FROM User WHERE nickname in (?1, :second, 'Maria')"})
        void shouldBindMixOfDelete(String text){

            Query query = template.query(text);
            query.bind("second", "Otavio");
            query.bind(1, "Ada");
            query.executeUpdate();

            Mockito.verify(manager).delete("Ada");
            Mockito.verify(manager).delete("Otavio");
            Mockito.verify(manager).delete("Maria");
        }

    }

}
