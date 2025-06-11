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

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.Sort;
import jakarta.data.restrict.Restriction;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.PreparedStatement;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.entities.Address;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.semistructured.entities.Product;
import org.eclipse.jnosql.mapping.semistructured.entities.Vendor;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
class CrudRepositoryProxyTest {

    private SemiStructuredTemplate template;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private PersonRepository personRepository;

    private VendorRepository vendorRepository;

    private AddressRepository addressRepository;


    @BeforeEach
    public void setUp() {
        this.template = Mockito.mock(SemiStructuredTemplate.class);

        var personHandler = new SemiStructuredRepositoryProxy<>(template,
                entities, PersonRepository.class, converters);

        var vendorHandler = new SemiStructuredRepositoryProxy<>(template,
                entities, VendorRepository.class, converters);

        var addressHandler = new SemiStructuredRepositoryProxy<>(template,
                entities, AddressRepository.class, converters);


        when(template.insert(any(Person.class))).thenReturn(Person.builder().build());
        when(template.insert(any(Person.class), any(Duration.class))).thenReturn(Person.builder().build());
        when(template.update(any(Person.class))).thenReturn(Person.builder().build());

        personRepository = (PersonRepository) Proxy.newProxyInstance(PersonRepository.class.getClassLoader(),
                new Class[]{PersonRepository.class},
                personHandler);
        vendorRepository = (VendorRepository) Proxy.newProxyInstance(VendorRepository.class.getClassLoader(),
                new Class[]{VendorRepository.class}, vendorHandler);

        addressRepository = (AddressRepository) Proxy.newProxyInstance(AddressRepository.class.getClassLoader(),
                new Class[]{AddressRepository.class}, addressHandler);
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
    void shouldInsert() {

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        Person person = Person.builder().name("Ada")
                .id(10L)
                .phones(singletonList("123123"))
                .build();
        assertNotNull(personRepository.insert(person));
        verify(template).insert(captor.capture());
        Person value = captor.getValue();
        assertEquals(person, value);
    }

    @Test
    void shouldUpdate() {

        ArgumentCaptor<Person> captor = ArgumentCaptor.forClass(Person.class);
        Person person = Person.builder().name("Ada")
                .id(10L)
                .phones(singletonList("123123"))
                .build();
        personRepository.update(person);
        verify(template).update(captor.capture());
        Person value = captor.getValue();
        assertEquals(person, value);
    }


    @Test
    void shouldInsertIterable() {

        ArgumentCaptor<List<Person>> captor = ArgumentCaptor.forClass(List.class);
        Person person = Person.builder().name("Ada")
                .id(10L)
                .phones(singletonList("123123"))
                .build();
        assertNotNull(personRepository.insertAll(List.of(person)));
        verify(template).insert(captor.capture());
        List<Person> value = captor.getValue();
        assertThat(value).contains(person);
    }

    @Test
    void shouldUpdateIterable() {

        ArgumentCaptor<List<Person>> captor = ArgumentCaptor.forClass(List.class);
        Person person = Person.builder().name("Ada")
                .id(10L)
                .phones(singletonList("123123"))
                .build();
        personRepository.updateAll(List.of(person));
        verify(template).update(captor.capture());
        List<Person> value = captor.getValue();
        assertThat(value).contains(person);
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
    void shouldFindByFirstAgeInstance() {

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(Person.builder().build()));

        Person[] first10ByAge = personRepository.findFirst10ByAge(10);
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(first10ByAge).isNotNull();
            soft.assertThat(first10ByAge).hasSize(1);
        });
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.condition()).isPresent();
            soft.assertThat(query.limit()).isEqualTo(10);
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(EQUALS);
            soft.assertThat(condition.element().value().get()).isEqualTo( 10);
        });

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
    void shouldReturnToString() {
        assertNotNull(personRepository.toString());
    }

    @Test
    void shouldReturnHasCode() {
        assertEquals(personRepository.hashCode(), personRepository.hashCode());
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
    void shouldExecuteJNoSQLQuery() {
        PreparedStatement statement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(statement);
        personRepository.findByQuery();
        verify(template).prepare("FROM Person", "Person");
    }

    @Test
    void shouldExecuteJNoSQLPrepare() {
        PreparedStatement statement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(statement);
        personRepository.findByQuery("Ada");
        verify(statement).bind("id", "Ada");
    }

    @Test
    void shouldExecuteJNoSQLPrepareIndex() {
        PreparedStatement statement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(statement);
        personRepository.findByQuery(10);
        verify(statement).bind("?1", 10);
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
        final Element column = condition.element();
        assertEquals("Person", query.name());
        assertEquals("salary.currency", column.name());

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
    void shouldFindByNameNotEquals() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findByNameNotEquals("Otavio");

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition negate = query.condition().get();
        assertEquals(Condition.NOT, negate.condition());
        CriteriaCondition condition = negate.element().get(CriteriaCondition.class);
        assertEquals(EQUALS, condition.condition());
        assertEquals(Element.of("name", "Otavio"), condition.element());
    }

    @Test
    void shouldFindByAgeNotGreaterThan() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        personRepository.findByAgeNotGreaterThan(10);

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition negate = query.condition().get();
        assertEquals(Condition.NOT, negate.condition());
        CriteriaCondition condition = negate.element().get(CriteriaCondition.class);
        assertEquals(GREATER_THAN, condition.condition());
        assertEquals(Element.of("age", 10), condition.element());
    }

    @Test
    void shouldCount() {

        PreparedStatement statement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(statement);

        when(statement.isCount()).thenReturn(true);
        when(statement.count()).thenReturn(10L);

        long result = personRepository.count("Ada", 10);

        assertEquals(10L, result);
    }

    @Test
    void shouldConvertMapAddressRepository() {

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        addressRepository.findByZipCodeZip("123456");
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        Assertions.assertThat(query)
                .isNotNull()
                .matches(c -> c.name().equals("Address"))
                .matches(c -> c.columns().isEmpty())
                .matches(c -> c.sorts().isEmpty())
                .extracting(SelectQuery::condition)
                .extracting(Optional::orElseThrow)
                .matches(c -> c.condition().equals(EQUALS))
                .extracting(CriteriaCondition::element)
                .matches(d -> d.value().get().equals("123456"))
                .matches(d -> d.name().equals("zipCode.zip"));

    }

    @Test
    void shouldConvertMapAddressRepositoryOrder() {

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        addressRepository.findByZipCodeZipOrderByZipCodeZip("123456");
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        Assertions.assertThat(query)
                .isNotNull()
                .matches(c -> c.name().equals("Address"))
                .matches(c -> c.columns().isEmpty())
                .matches(c -> !c.sorts().isEmpty())
                .extracting(SelectQuery::condition)
                .extracting(Optional::orElseThrow)
                .matches(c -> c.condition().equals(EQUALS))
                .extracting(CriteriaCondition::element)
                .matches(d -> d.value().get().equals("123456"))
                .matches(d -> d.name().equals("zipCode.zip"));


        Assertions.assertThat(query.sorts()).contains(Sort.asc("zipCode.zip"));

    }

    @Test
    void shouldExecuteSingleQuery() {

        PreparedStatement statement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(statement);

        when(statement.isCount()).thenReturn(true);
        when(statement.count()).thenReturn(10L);

        long result = personRepository.count("Ada", 10);

        assertEquals(10L, result);
    }

    @Test
    void shouldExecuteQueryWithPagination() {
        PreparedStatement statement = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        when(template.prepare(Mockito.anyString(), Mockito.anyString())).thenReturn(statement);
        when(statement.result()).thenReturn(Stream.of(10L));
        var page = personRepository.queryPagination(10, PageRequest.ofPage(10));
        verify(statement).bind("?1", 10);

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(page).isNotNull();
            soft.assertThat(page.content()).contains(10L);
            soft.assertThat(page.content()).hasSize(1);
        });
    }



    interface PersonRepository extends NoSQLRepository<Person, Long> {

        Person[] findFirst10ByAge(int age);

        List<Person> findBySalary_Currency(String currency);

        List<Person> findBySalary_CurrencyAndSalary_Value(String currency, BigDecimal value);

        List<Person> findBySalary_CurrencyOrderByCurrency_Name(String currency);

        Person findByName(String name);

        List<Person> findByNameNotEquals(String name);

        List<Person> findByAgeNotGreaterThan(Integer age);

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

        @Query("FROM Person WHERE id = :id")
        Optional<Person> findByQuery(@Param("id") String id);

        @Query("FROM Person WHERE age = ?1")
        Optional<Person> findByQuery(int age);

        @Query("select count(this) FROM Person WHERE name = ?1 and age > ?2")
        long count(String name, int age);

        @Query("SELECT id WHERE age > ?1")
        List<Long> querySingle(int age);

        @Query("SELECT id WHERE age > ?1")
        Page<Long> queryPagination(int age, PageRequest pageRequest);
    }

    public interface VendorRepository extends CrudRepository<Vendor, String> {

        Vendor findByPrefixes(String prefix);

        Vendor findByPrefixesIn(List<String> prefix);

    }

    public interface AddressRepository extends CrudRepository<Address, String> {

        List<Address> findByZipCodeZip(String zip);

        List<Address> findByZipCodeZipOrderByZipCodeZip(String zip);
    }

}
