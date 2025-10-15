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
import org.eclipse.jnosql.communication.keyvalue.KeyValueEntity;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.keyvalue.entities.ErrorEntity;
import org.eclipse.jnosql.mapping.keyvalue.entities.Person;
import org.eclipse.jnosql.mapping.keyvalue.spi.KeyValueExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.mockito.Mockito.when;


@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MapperSelectTest {

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

    @Test
    @DisplayName("Should return error when select mapper is null")
    void shouldReturnErrorWhenMapperIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> template.select(null));
    }

    @Test
    @DisplayName("Should return error when entity has not id")
    void shouldReturnErrorWhenEntityHasNotId() {
        Assertions.assertThrows(MappingException.class, () -> template.select(ErrorEntity.class));
    }


    @Test
    @DisplayName("Should return error when the result is empty")
    void shouldReturnWhenTheResultWithoutUsingOrder(){
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).result());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).singleResult());
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).stream());
    }

    @Test
    @DisplayName("Should return error when there is order")
    void shouldReturnErrorWhenThereIsOrder(){
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).orderBy("name"));
    }

    @Test
    @DisplayName("Should return error when there is skip")
    void shouldReturnErrorWhenThereIsSkip(){
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).skip(10));
    }

    @Test
    @DisplayName("Should return error when there is limit")
    void shouldReturnErrorWhenThereIsLimit(){
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).limit(10));
    }

    @Test
    @DisplayName("Should return error when attribute is not id")
    void shouldReturnErrorWhenAttributeIsNotId() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).where("name"));
    }

    @Test
    @DisplayName("Should return error when the operator is not supported")
    void shouldReturnErrorWhenTheOperatorIsNotSupported() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).where("id").like("Otavio"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).where("id").gt("Otavio"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).where("id").gte("Otavio"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).where("id").lt("Otavio"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).where("id").lte("Otavio"));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).where("id").between(10, 20));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).where("id").not());
    }

    @Test
    @DisplayName("Should return error when there is and operator")
    void shouldReturnErrorWhenThereIsAndOperator() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).where("id").eq(10).and("id"));
    }

    @Test
    @DisplayName("Should return error when there is or operator")
    void shouldReturnErrorWhenThereIsOrOperator() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.select(Person.class).where("id").eq(10).or("id"));
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
            soft.assertThat(result).hasSize(1);
            soft.assertThat(result).contains(person);
            Mockito.verify(manager).get(10L);
        });
    }

}