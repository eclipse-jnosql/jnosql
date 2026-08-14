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
import jakarta.nosql.MappingException;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.keyvalue.entities.ErrorEntity;
import org.eclipse.jnosql.mapping.keyvalue.entities.Person;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
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
class MapperSelectTest {
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
    @DisplayName("When the select mapper builds queries")
    class WhenTheSelectMapperBuildsQueries {

        @Test
        @DisplayName("Should return error when select mapper is null")
        void shouldReturnErrorWhenMapperIsNull() {
            assertThatThrownBy(() -> template.select(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should not supported typed query")
        void shouldNotSupportedTypedQuery(){
            assertThatThrownBy(() -> template.typedQuery(null, null)).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return error when entity has not id")
        void shouldReturnErrorWhenEntityHasNotId() {
            assertThatThrownBy(() -> template.select(ErrorEntity.class)).isInstanceOf(MappingException.class);
        }

        @Test
        @DisplayName("Should return error when the result is empty")
        void shouldReturnWhenTheResultWithoutUsingOrder(){
            assertThatThrownBy(() -> template.select(Person.class).result()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.select(Person.class).singleResult()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.select(Person.class).stream()).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return error when there is order")
        void shouldReturnErrorWhenThereIsOrder(){
            assertThatThrownBy(() -> template.select(Person.class).orderBy("name")).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return error when there is skip")
        void shouldReturnErrorWhenThereIsSkip(){
            assertThatThrownBy(() -> template.select(Person.class).skip(10)).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return error when there is limit")
        void shouldReturnErrorWhenThereIsLimit(){
            assertThatThrownBy(() -> template.select(Person.class).limit(10)).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return error when attribute is not id")
        void shouldReturnErrorWhenAttributeIsNotId() {
            assertThatThrownBy(() -> template.select(Person.class).where("name")).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return error when the operator is not supported")
        void shouldReturnErrorWhenTheOperatorIsNotSupported() {
            assertThatThrownBy(() -> template.select(Person.class).where("id").like("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.select(Person.class).where("id").gt("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.select(Person.class).where("id").gte("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.select(Person.class).where("id").lt("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.select(Person.class).where("id").lte("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.select(Person.class).where("id").between(10, 20)).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.select(Person.class).where("id").not()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.select(Person.class).where("id").contains("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.select(Person.class).where("id").startsWith("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.select(Person.class).where("id").endsWith("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.select(Person.class).count()).isInstanceOf(UnsupportedOperationException.class);

        }

        @Test
        @DisplayName("Should return error when there is and operator")
        void shouldReturnErrorWhenThereIsAndOperator() {
            assertThatThrownBy(() -> template.select(Person.class).where("id").eq(10).and("id")).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return error when there is or operator")
        void shouldReturnErrorWhenThereIsOrOperator() {
            assertThatThrownBy(() -> template.select(Person.class).where("id").eq(10).or("id")).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should execute query equals List")
        void shouldExecuteQueryEqualsList() {
            var person = Person.builder().withId(10L).withName("Otavio").build();
            when(manager.get(10L)).thenReturn(java.util.Optional.of(Value.of(person)));
            var result = template.select(Person.class).where("id").eq(10L).result();

            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(result).hasSize(1);
                soft.assertThat(result).contains(person);
                Mockito.verify(manager).get(10L);
            });
        }

        @Test
        @DisplayName("Should execute query equals Stream")
        void shouldExecuteQueryEqualsStream() {
            var person = Person.builder().withId(10L).withName("Otavio").build();
            when(manager.get(10L)).thenReturn(java.util.Optional.of(Value.of(person)));
            var result = template.select(Person.class).where("id").eq(10L).stream();

            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(result).hasSize(1).contains(person);
                Mockito.verify(manager).get(10L);
            });
        }

        @Test
        @DisplayName("Should execute query equals single result")
        void shouldExecuteQueryEqualsOptionalSingleResult() {
            var person = Person.builder().withId(10L).withName("Otavio").build();
            when(manager.get(10L)).thenReturn(java.util.Optional.of(Value.of(person)));
            var result = template.select(Person.class).where("id").eq(10L).singleResult();

            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(result).isNotEmpty();
                soft.assertThat(result).contains(person);
                Mockito.verify(manager).get(10L);
            });
        }

        @Test
        @DisplayName("Should execute query equals single result empty")
        void shouldExecuteQueryEqualsOptionalSingleResultEmpty() {
            when(manager.get(10L)).thenReturn(java.util.Optional.empty());
            var result = template.select(Person.class).where("id").eq(10L).singleResult();

            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(result).isEmpty();
                Mockito.verify(manager).get(10L);
            });
        }

        @Test
        @DisplayName("Should return error when equals single result there are two")
        void shouldReturnErrorWhenEqualsSingleResultThereAreTwo(){
            var otavio = Person.builder().withId(10L).withName("Otavio").build();
            var ada = Person.builder().withId(11L).withName("Ada").build();
            when(manager.get(10L)).thenReturn(java.util.Optional.of(Value.of(otavio)));
            when(manager.get(11L)).thenReturn(java.util.Optional.of(Value.of(ada)));

          assertThatThrownBy(() -> template.select(Person.class).where("id").in(List.of(10L, 11L)).singleResult()).isInstanceOf(NonUniqueResultException.class);
        }

        @Test
        @DisplayName("Should return list when in")
        void shouldReturnListWhenIn() {
            var otavio = Person.builder().withId(10L).withName("Otavio").build();
            var ada = Person.builder().withId(11L).withName("Ada").build();
            when(manager.get(10L)).thenReturn(java.util.Optional.of(Value.of(otavio)));
            when(manager.get(11L)).thenReturn(java.util.Optional.of(Value.of(ada)));
            List<Person> people = template.select(Person.class).where("id").in(List.of(10L, 11L)).result();

            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(people).hasSize(2);
                soft.assertThat(people).contains(otavio, ada);
                Mockito.verify(manager).get(10L);
                Mockito.verify(manager).get(11L);
            });
        }

        @Test
        @DisplayName("Should return stream when in")
        void shouldReturnStreamWhenIn() {
            var otavio = Person.builder().withId(10L).withName("Otavio").build();
            var ada = Person.builder().withId(11L).withName("Ada").build();
            when(manager.get(10L)).thenReturn(java.util.Optional.of(Value.of(otavio)));
            when(manager.get(11L)).thenReturn(java.util.Optional.of(Value.of(ada)));
            Stream<Person> people = template.select(Person.class).where("id").in(List.of(10L, 11L)).stream();

            SoftAssertions.assertSoftly(soft ->{
                soft.assertThat(people).hasSize(2).contains(otavio, ada);
                Mockito.verify(manager).get(10L);
                Mockito.verify(manager).get(11L);
            });
        }

    }

}