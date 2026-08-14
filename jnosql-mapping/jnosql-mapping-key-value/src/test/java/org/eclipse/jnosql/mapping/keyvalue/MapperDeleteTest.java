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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;


@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MapperDeleteTest {

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
    @DisplayName("When the delete mapper builds queries")
    class WhenTheDeleteMapperBuildsQueries {

        @Test
        @DisplayName("Should return error when select mapper is null")
        void shouldReturnErrorWhenMapperIsNull() {
            assertThatThrownBy(() -> template.delete((Class<Object>) null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should return error when entity has not id")
        void shouldReturnErrorWhenEntityHasNotId() {
            assertThatThrownBy(() -> template.delete(ErrorEntity.class)).isInstanceOf(MappingException.class);
        }

        @Test
        @DisplayName("Should return error when the result is empty")
        void shouldReturnWhenTheResultWithoutUsingOrder(){
            assertThatThrownBy(() -> template.delete(Person.class).execute()).isInstanceOf(UnsupportedOperationException.class);

        }

        @Test
        @DisplayName("Should return error when attribute is not id")
        void shouldReturnErrorWhenAttributeIsNotId() {
            assertThatThrownBy(() -> template.delete(Person.class).where("name")).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return error when the operator is not supported")
        void shouldReturnErrorWhenTheOperatorIsNotSupported() {
            assertThatThrownBy(() -> template.delete(Person.class).where("id").like("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.delete(Person.class).where("id").gt("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.delete(Person.class).where("id").gte("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.delete(Person.class).where("id").lt("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.delete(Person.class).where("id").lte("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.delete(Person.class).where("id").between(10, 20)).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.delete(Person.class).where("id").not()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.delete(Person.class).where("name").contains("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.delete(Person.class).where("name").startsWith("Otavio")).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> template.delete(Person.class).where("name").endsWith("Otavio")).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return error when there is and operator")
        void shouldReturnErrorWhenThereIsAndOperator() {
            assertThatThrownBy(() -> template.delete(Person.class).where("id").eq(10).and("id")).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should return error when there is or operator")
        void shouldReturnErrorWhenThereIsOrOperator() {
            assertThatThrownBy(() -> template.delete(Person.class).where("id").eq(10).or("id")).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Should execute query equals")
        void shouldExecuteQueryEqualsSingleResult() {
            var person = Person.builder().withId(10L).withName("Otavio").build();
            when(manager.get(10L)).thenReturn(java.util.Optional.of(Value.of(person)));
            template.delete(Person.class).where("id").eq(10L).execute();

            SoftAssertions.assertSoftly(soft -> Mockito.verify(manager).delete(List.of(10L)));
        }

        @Test
        @DisplayName("Should execute query in")
        void shouldExecuteQueryEqualsList() {
            var person = Person.builder().withId(10L).withName("Otavio").build();
            when(manager.get(10L)).thenReturn(java.util.Optional.of(Value.of(person)));
            template.delete(Person.class).where("id").in(List.of(10L, 11L)).execute();

            SoftAssertions.assertSoftly(soft -> Mockito.verify(manager).delete(List.of(10L, 11L)));
        }

    }

}