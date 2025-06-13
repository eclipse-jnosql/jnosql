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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.data.exceptions.MappingException;
import jakarta.data.repository.By;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.Sort;
import jakarta.data.repository.Save;
import jakarta.data.repository.Update;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.PreparedStatement;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.semistructured.entities.PersonStatisticRepository;
import org.eclipse.jnosql.mapping.semistructured.entities.Vendor;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.communication.Condition.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class RepositoryProxyTest {

    private SemiStructuredTemplate template;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private PersonRepository personRepository;

    private VendorRepository vendorRepository;


    @BeforeEach
    void setUp() {
        this.template = Mockito.mock(SemiStructuredTemplate.class);

        SemiStructuredRepositoryProxy personHandler = new SemiStructuredRepositoryProxy(template,
                entities, PersonRepository.class, converters);

        SemiStructuredRepositoryProxy vendorHandler = new SemiStructuredRepositoryProxy(template,
                entities, VendorRepository.class, converters);

        when(template.insert(any(Person.class))).thenReturn(Person.builder().build());
        when(template.insert(any(Person.class), any(Duration.class))).thenReturn(Person.builder().build());
        when(template.update(any(Person.class))).thenReturn(Person.builder().build());

        personRepository = (PersonRepository) Proxy.newProxyInstance(PersonRepository.class.getClassLoader(),
                new Class[]{PersonRepository.class},
                personHandler);
        vendorRepository = (VendorRepository) Proxy.newProxyInstance(VendorRepository.class.getClassLoader(),
                new Class[]{VendorRepository.class}, vendorHandler);
    }


    @Test
    void shouldSaveUsingInsertWhenDataDoesNotExist() {
        when(template.find(Person.class, 10L)).thenReturn(Optional.empty());

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        Person person = Person.builder().name("Ada")
                .id(10L)
                .phones(singletonList("123123"))
                .build();
        assertNotNull(personRepository.save(person));
        verify(template).insert(captor.capture());
        Person value = captor.getValue();
        assertEquals(person, value);
    }


    @Test
    void shouldSaveUsingUpdateWhenDataExists() {

        when(template.find(Person.class, 10L)).thenReturn(Optional.of(Person.builder().build()));

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        Person person = Person.builder().name("Ada")
                .id(10L)
                .phones(singletonList("123123"))
                .build();
        assertNotNull(personRepository.save(person));
        verify(template).update(captor.capture());
        Person value = captor.getValue();
        assertEquals(person, value);
    }


    @Test
    void shouldSaveIterable() {
        when(personRepository.findById(10L)).thenReturn(Optional.empty());

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        Person person = Person.builder().name("Ada")
                .id(10L)
                .phones(singletonList("123123"))
                .build();

        personRepository.saveAll(singletonList(person));
        verify(template).insert(captor.capture());
        Person personCapture = captor.getValue();
        assertEquals(person, personCapture);
    }


    @Test
    void shouldFindByNameInstance() {

        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        personRepository.findByName("name");

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).singleResult(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals("Person", query.name());
        assertEquals(Condition.EQUALS, condition.condition());
        assertEquals(Element.of("name", "name"), condition.element());

        assertNotNull(personRepository.findByName("name"));
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .empty());

        assertNull(personRepository.findByName("name"));


    }

    @Test
    void shouldFindByNameANDAge() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        List<Person> persons = personRepository.findByNameAndAge("name", 20);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        assertThat(persons).contains(ada);

    }

    @Test
    void shouldFindByAgeANDName() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        Set<Person> persons = personRepository.findByAgeAndName(20, "name");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        assertThat(persons).contains(ada);

    }

    @Test
    void shouldFindByNameANDAgeOrderByName() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        Stream<Person> persons = personRepository.findByNameAndAgeOrderByName("name", 20);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        assertThat(persons.collect(Collectors.toList())).contains(ada);

    }

    @Test
    void shouldFindByNameANDAgeOrderByAge() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        Queue<Person> persons = personRepository.findByNameAndAgeOrderByAge("name", 20);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        assertThat(persons).contains(ada);

    }

    @Test
    void shouldDeleteByName() {
        ArgumentCaptor<DeleteQuery> captor = ArgumentCaptor.forClass(DeleteQuery.class);
        personRepository.deleteByName("Ada");
        verify(template).delete(captor.capture());
        DeleteQuery deleteQuery = captor.getValue();
        CriteriaCondition condition = deleteQuery.condition().get();
        assertEquals("Person", deleteQuery.name());
        assertEquals(Condition.EQUALS, condition.condition());
        assertEquals(Element.of("name", "Ada"), condition.element());

    }

    @Test
    void shouldFindById() {
        personRepository.findById(10L);
        verify(template).find(Person.class, 10L);
    }

    @Test
    void shouldFindByIds() {
        when(template.find(Mockito.eq(Person.class), Mockito.any(Long.class)))
                .thenReturn(Optional.of(Person.builder().build()));

        personRepository.findByIdIn(singletonList(10L)).toList();
        verify(template).find(Person.class, 10L);

        personRepository.findByIdIn(asList(1L, 2L, 3L)).toList();
        verify(template, times(4)).find(Mockito.eq(Person.class), Mockito.any(Long.class));
    }

    @Test
    void shouldDeleteById() {
        ArgumentCaptor<DeleteQuery> captor = ArgumentCaptor.forClass(DeleteQuery.class);
        personRepository.deleteById(10L);
        verify(template).delete(Person.class, 10L);
    }

    @Test
    void shouldDeleteByIds() {
        ArgumentCaptor<DeleteQuery> captor = ArgumentCaptor.forClass(DeleteQuery.class);
        personRepository.deleteByIdIn(singletonList(10L));
        verify(template).delete(Person.class, 10L);
    }


    @Test
    void shouldContainsById() {
        when(template.find(Person.class, 10L)).thenReturn(Optional.of(Person.builder().build()));

        assertTrue(personRepository.existsById(10L));
        Mockito.verify(template).find(Person.class, 10L);

        when(template.find(Person.class, 10L)).thenReturn(Optional.empty());
        assertFalse(personRepository.existsById(10L));

    }

    @Test
    void shouldFindAll() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findAll().toList();
        ArgumentCaptor<Class<?>> captor = ArgumentCaptor.forClass(Class.class);
        verify(template).findAll(captor.capture());
        assertEquals(captor.getValue(), Person.class);

    }

    @Test
    void shouldDeleteAll() {
        personRepository.deleteAll();
        ArgumentCaptor<Class<?>> captor = ArgumentCaptor.forClass(Class.class);
        verify(template).deleteAll(captor.capture());
        assertEquals(captor.getValue(), Person.class);

    }

    @Test
    void shouldDeleteEntity(){
        Person person = Person.builder().id(1L).age(20).name("Ada").build();
        personRepository.delete(person);
        verify(template).delete(Person.class, 1L);
    }

    @Test
    void shouldDeleteEntities(){
        Person person = Person.builder().id(1L).age(20).name("Ada").build();
        personRepository.deleteAll(List.of(person));
        verify(template).delete(Person.class, 1L);
    }

    @Test
    void shouldReturnToString() {
        assertNotNull(personRepository.toString());
    }

    @Test
    void shouldReturnSameHashCode() {
        assertEquals(personRepository.hashCode(), personRepository.hashCode());
    }

    @Test
    void shouldReturnNotNull() {
        assertNotNull(personRepository);
    }

    @Test
    void shouldFindByNameAndAgeGreaterEqualThan() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findByNameAndAgeGreaterThanEqual("Ada", 33);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals("Person", query.name());
        assertEquals(AND, condition.condition());
        List<CriteriaCondition> conditions = condition.element().get(new TypeReference<>() {
        });
        CriteriaCondition columnCondition = conditions.get(0);
        CriteriaCondition columnCondition2 = conditions.get(1);

        assertEquals(Condition.EQUALS, columnCondition.condition());
        assertEquals("Ada", columnCondition.element().get());
        assertEquals("name", columnCondition.element().name());

        assertEquals(Condition.GREATER_EQUALS_THAN, columnCondition2.condition());
        assertEquals(33, columnCondition2.element().get());
        assertEquals("age", columnCondition2.element().name());
    }

    @Test
    void shouldFindByGreaterThan() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findByAgeGreaterThan(33);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals("Person", query.name());
        assertEquals(GREATER_THAN, condition.condition());
        assertEquals(Element.of("age", 33), condition.element());

    }

    @Test
    void shouldFindByAgeLessThanEqual() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findByAgeLessThanEqual(33);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals("Person", query.name());
        assertEquals(LESSER_EQUALS_THAN, condition.condition());
        assertEquals(Element.of("age", 33), condition.element());

    }

    @Test
    void shouldFindByAgeLessEqual() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findByAgeLessThan(33);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals("Person", query.name());
        assertEquals(LESSER_THAN, condition.condition());
        assertEquals(Element.of("age", 33), condition.element());

    }

    @Test
    void shouldFindByAgeBetween() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findByAgeBetween(10, 15);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals("Person", query.name());
        assertEquals(BETWEEN, condition.condition());
        List<Value> values = condition.element().get(new TypeReference<>() {
        });
        assertEquals(Arrays.asList(10, 15), values.stream().map(Value::get).collect(Collectors.toList()));
        assertTrue(condition.element().name().contains("age"));
    }


    @Test
    void shouldFindByNameLike() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findByNameLike("Ada");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals("Person", query.name());
        assertEquals(LIKE, condition.condition());
        assertEquals(Element.of("name", "Ada"), condition.element());

    }

    @Test
    void shouldGotOrderException() {
        Assertions.assertThrows(UnsupportedOperationException.class, () ->
                personRepository.invalid());
    }

    @Test
    void shouldGotOrderException2() {
        Assertions.assertThrows(UnsupportedOperationException.class, () ->
                personRepository.invalid2());
    }


    @Test
    void shouldFindByStringWhenFieldIsSet() {
        Vendor vendor = new Vendor("vendor");
        vendor.setPrefixes(Collections.singleton("prefix"));

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(vendor));

        vendorRepository.findByPrefixes("prefix");

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).singleResult(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals("vendors", query.name());
        assertEquals(EQUALS, condition.condition());
        assertEquals(Element.of("prefixes", "prefix"), condition.element());

    }

    @Test
    void shouldFindByIn() {
        Vendor vendor = new Vendor("vendor");
        vendor.setPrefixes(Collections.singleton("prefix"));

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(vendor));

        vendorRepository.findByPrefixesIn(singletonList("prefix"));

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).singleResult(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals("vendors", query.name());
        assertEquals(IN, condition.condition());

    }


    @Test
    void shouldConvertFieldToTheType() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findByAge("120");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals("Person", query.name());
        assertEquals(EQUALS, condition.condition());
        assertEquals(Element.of("age", 120), condition.element());
    }

    @Test
    void shouldFindByActiveTrue() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findByActiveTrue();
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals("Person", query.name());
        assertEquals(EQUALS, condition.condition());
        assertEquals(Element.of("active", true), condition.element());
    }

    @Test
    void shouldFindByActiveFalse() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findByActiveFalse();
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals("Person", query.name());
        assertEquals(EQUALS, condition.condition());
        assertEquals(Element.of("active", false), condition.element());
    }


    @Test
    void shouldExecuteJNoSQLQuery() {
        PreparedStatement preparedStatement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(preparedStatement);
        personRepository.findByQuery();
        verify(template).prepare("FROM Person", "Person");
    }

    @Test
    void shouldExecuteJNoSQLPrepare() {
        PreparedStatement statement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(template.prepare(Mockito.anyString(),Mockito.anyString() )).thenReturn(statement);
        personRepository.findByQuery("Ada");
        verify(statement).bind("id", "Ada");
    }

    @Test
    void shouldExecuteJNoSQLPrepareAge() {
        PreparedStatement statement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(template.prepare(Mockito.anyString(),Mockito.anyString() )).thenReturn(statement);
        personRepository.findByQueryAge(10);
        verify(statement).bind("?1", 10);
    }

    @Test
    void shouldExecuteJNoSQLPrepareUpdate() {
        PreparedStatement statement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(template.prepare(Mockito.anyString(),Mockito.anyString() )).thenReturn(statement);
        personRepository.update(10, "id");
        verify(statement).bind("?1", 10);
        verify(statement).bind("?2", "id");
    }

    @Test
    void shouldExecuteJNoSQLPrepareUpdate2() {
        PreparedStatement statement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(template.prepare(Mockito.anyString(),Mockito.anyString() )).thenReturn(statement);
        personRepository.update("name", "id");
        verify(statement).bind("name", "name");
        verify(statement).bind("id", "id");
    }

    @Test
    void shouldExecuteJNoSQLPrepareDelete() {
        PreparedStatement statement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(template.prepare(Mockito.anyString(),Mockito.anyString() )).thenReturn(statement);
        personRepository.delete("10");
        verify(statement).bind("?1", "10");
    }

    @Test
    void shouldExecuteJNoSQLPrepareDelete2() {
        PreparedStatement statement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(template.prepare(Mockito.anyString(),Mockito.anyString() )).thenReturn(statement);
        personRepository.delete("name", "id");
        verify(statement).bind("name", "name");
        verify(statement).bind("id", "id");
    }

    @Test
    void shouldFindBySalary_Currency() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findBySalary_Currency("USD");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        final Element element = condition.element();
        assertEquals("Person", query.name());
        assertEquals("salary.currency", element.name());

    }

    @Test
    void shouldFindBySalary_CurrencyAndSalary_Value() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();
        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));
        personRepository.findBySalary_CurrencyAndSalary_Value("USD", BigDecimal.TEN);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        final Element column = condition.element();
        final List<CriteriaCondition> conditions = column.get(new TypeReference<>() {
        });
        final List<String> names = conditions.stream().map(CriteriaCondition::element)
                .map(Element::name).collect(Collectors.toList());
        assertEquals("Person", query.name());
        assertThat(names).contains("salary.currency", "salary.value");

    }

    @Test
    void shouldFindBySalary_CurrencyOrderByCurrency_Name() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findBySalary_CurrencyOrderByCurrency_Name("USD");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        final Sort<?> sort = query.sorts().get(0);
        final Element document = condition.element();
        assertEquals("Person", query.name());
        assertEquals("salary.currency", document.name());
        assertEquals("currency.name", sort.property());
    }

    @Test
    void shouldCountByName() {
        when(template.count(any(SelectQuery.class)))
                .thenReturn(10L);

        var result = personRepository.countByName("Poliana");
        Assertions.assertEquals(10L, result);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).count(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        final Element column = condition.element();
        assertEquals("Person", query.name());
        assertEquals("name", column.name());
        assertEquals("Poliana", column.get());
    }

    @Test
    void shouldExistsByName() {
        when(template.exists(any(SelectQuery.class)))
                .thenReturn(true);

        var result = personRepository.existsByName("Poliana");
        assertTrue(result);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).exists(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        final Element column = condition.element();
        assertEquals("Person", query.name());
        assertEquals("name", column.name());
        assertEquals("Poliana", column.get());
    }

    @Test
    void shouldFindByNameNot() {

        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        personRepository.findByNameNot("name");

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).singleResult(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals(Condition.NOT, condition.condition());
        assertEquals("Person", query.name());
        CriteriaCondition columnCondition = condition.element().get(CriteriaCondition.class);
        assertEquals(Condition.EQUALS, columnCondition.condition());
        assertEquals(Element.of("name", "name"), columnCondition.element());

        assertNotNull(personRepository.findByName("name"));
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .empty());

        assertNull(personRepository.findByName("name"));
    }

    @Test
    void shouldFindByNameNotEquals() {

        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        personRepository.findByNameNotEquals("name");

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).singleResult(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertEquals(Condition.NOT, condition.condition());
        assertEquals("Person", query.name());
        CriteriaCondition columnCondition = condition.element().get(CriteriaCondition.class);
        assertEquals(Condition.EQUALS, columnCondition.condition());
        assertEquals(Element.of("name", "name"), columnCondition.element());

        assertNotNull(personRepository.findByName("name"));
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .empty());

        assertNull(personRepository.findByName("name"));
    }

    @Test
    void shouldExecuteDefaultMethod() {
        personRepository.partcionate("name");

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template, Mockito.times(2)).singleResult(captor.capture());
        List<SelectQuery> values = captor.getAllValues();
        assertThat(values).isNotNull().hasSize(2);
    }

    @Test
    void shouldUseQueriesFromOtherInterface() {
        personRepository.findByNameLessThan("name");

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals("Person", query.name());
        CriteriaCondition condition = query.condition().get();
        assertEquals(LESSER_THAN, condition.condition());
        assertEquals(Element.of("name", "name"), condition.element());
    }

    @Test
    void shouldUseDefaultMethodFromOtherInterface() {
        personRepository.ada();

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertEquals("Person", query.name());
        CriteriaCondition condition = query.condition().get();
        assertEquals(LESSER_THAN, condition.condition());
        assertEquals(Element.of("name", "Ada"), condition.element());
    }

    @Test
    void shouldExecuteCustomRepository(){
        PersonStatisticRepository.PersonStatistic statistics = personRepository.statistics("Salvador");
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
        Person person = Person.builder().name("Ada")
                .id(10L)
                .phones(singletonList("123123"))
                .build();
        personRepository.insertPerson(person);
        Mockito.verify(template).insert(person);
    }

    @Test
    void shouldUpdateUsingAnnotation(){
        Person person = Person.builder().name("Ada")
                .id(10L)
                .phones(singletonList("123123"))
                .build();
        personRepository.updatePerson(person);
        Mockito.verify(template).update(person);
    }

    @Test
    void shouldDeleteUsingAnnotation(){
        Person person = Person.builder().name("Ada")
                .id(10L)
                .phones(singletonList("123123"))
                .build();
        personRepository.deletePerson(person);
        Mockito.verify(template).delete(Person.class, 10L);
    }

    @Test
    void shouldSaveUsingAnnotation(){
        Person person = Person.builder().name("Ada")
                .id(10L)
                .phones(singletonList("123123"))
                .build();
        personRepository.savePerson(person);
        Mockito.verify(template).insert(person);
    }

    @Test
    void shouldExecuteMatchParameter(){
        personRepository.find("Ada");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(query.name()).isEqualTo("Person");
            var condition = query.condition().orElseThrow();
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of("name", "Ada"));
            softly.assertThat(query.sorts()).isEmpty();
        });
    }

    @Test
    void shouldExecuteMatchParameterId(){
        personRepository.find(10L);
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(query.name()).isEqualTo("Person");
            var condition = query.condition().orElseThrow();
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of("_id", 10L));
            softly.assertThat(query.sorts()).hasSize(1).contains(Sort.asc("_id"));
        });
    }

    @Test
    void shouldExecuteMatchParameter2(){
        personRepository.find2("Ada");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(query.name()).isEqualTo("Person");
            var condition = query.condition().orElseThrow();
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of("name", "Ada"));
            softly.assertThat(query.sorts()).hasSize(1).contains(Sort.asc("name"));
        });
    }

    @Test
    void shouldExecuteMatchParameter3(){
        personRepository.find3("Ada");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(query.name()).isEqualTo("Person");
            var condition = query.condition().orElseThrow();
            softly.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            softly.assertThat(condition.element()).isEqualTo(Element.of("name", "Ada"));
            softly.assertThat(query.sorts()).hasSize(2).contains(Sort.asc("name"),
                    Sort.desc("age"));
        });
    }

    public interface BaseQuery<T> {

        List<T> findByNameLessThan(String name);

        default List<T> ada() {
            return this.findByNameLessThan("Ada");
        }
    }

    public interface PersonRepository extends NoSQLRepository<Person, Long>, BaseQuery<Person>, PersonStatisticRepository {

        List<Person> findByActiveTrue();

        List<Person> findByActiveFalse();

        List<Person> findBySalary_Currency(String currency);

        List<Person> findBySalary_CurrencyAndSalary_Value(String currency, BigDecimal value);

        List<Person> findBySalary_CurrencyOrderByCurrency_Name(String currency);

        Person findByName(String name);

        Person findByNameNot(String name);

        Person findByNameNotEquals(String name);

        @Insert
        Person insertPerson(Person person);
        @Update
        Person updatePerson(Person person);

        @Save
        Person savePerson(Person person);

        @Delete
        void deletePerson(Person person);

        default Map<Boolean, List<Person>> partcionate(String name) {
            Objects.requireNonNull(name, "name is required");

            var person = Person.builder()
                    .name("Ada Lovelace")
                    .age(20)
                    .id(1L).build();
            findByName(name);
            findByNameNot(name);
            Map<Boolean, List<Person>> map = new HashMap<>();
            map.put(true, List.of(person));
            map.put(false, List.of(person));
            return map;
        }

        void deleteByName(String name);

        List<Person> findByAge(String age);

        List<Person> findByNameAndAge(String name, Integer age);

        Set<Person> findByAgeAndName(Integer age, String name);

        Stream<Person> findByNameAndAgeOrderByName(String name, Integer age);

        Queue<Person> findByNameAndAgeOrderByAge(String name, Integer age);

        Set<Person> findByNameAndAgeGreaterThanEqual(String name, Integer age);

        Set<Person> findByAgeGreaterThan(Integer age);

        Set<Person> findByAgeLessThanEqual(Integer age);

        Set<Person> findByAgeLessThan(Integer age);

        Set<Person> findByAgeBetween(Integer ageA, Integer ageB);

        Set<Person> findByNameLike(String name);

        @Query("FROM Person")
        Optional<Person> findByQuery();

        @Query(" ")
        Optional<Person> all();

        @Query("FROM Person WHERE id = :id")
        Optional<Person> findByQuery(@Param("id") String id);

        @Query("FROM Person WHERE id = ?1")
        Optional<Person> findByQueryAge(int age);

        @Query("UPDATE Person SET name = ?1 WHERE id = ?2")
        void update(int age, String id);

        @Query("UPDATE Person SET name = :name WHERE id = :id")
        void update(@Param("name") String name, @Param("id") String id);


        @Query("DELETE FROM Person WHERE id = ?1")
        void delete(String id);

        @Query("DELETE FROM Person WHERE name = :name AND id = :id")
        void delete(@Param("name") String name,@Param("id") String id);

        long countByName(String name);

        boolean existsByName(String name);

        @OrderBy("name")
        List<Person> invalid();

        @OrderBy("name")
        @OrderBy("age")
        List<Person> invalid2();

        @OrderBy("id")
        @Find
        List<Person> find(@By("id") Long id);


        @Find
        List<Person> find(@By("name") String name);

        @Find
        @OrderBy(value = "name")
        List<Person> find2(@By("name") String name);

        @Find
        @OrderBy(value = "name")
        @OrderBy(value = "age", descending = true)
        List<Person> find3(@By("name") String name);
    }

    public interface VendorRepository extends BasicRepository<Vendor, String> {

        Vendor findByPrefixes(String prefix);

        Vendor findByPrefixesIn(List<String> prefix);

    }
}
