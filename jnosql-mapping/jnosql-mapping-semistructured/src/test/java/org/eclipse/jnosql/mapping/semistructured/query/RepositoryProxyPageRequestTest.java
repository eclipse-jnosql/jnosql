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

import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.exceptions.EmptyResultException;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.By;
import jakarta.data.repository.Find;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.inject.Inject;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.repository.LifecycleEventHandler;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.semistructured.entities.Vendor;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.jnosql.communication.Condition.AND;
import static org.eclipse.jnosql.communication.Condition.BETWEEN;
import static org.eclipse.jnosql.communication.Condition.EQUALS;
import static org.eclipse.jnosql.communication.Condition.GREATER_THAN;
import static org.eclipse.jnosql.communication.Condition.IN;
import static org.eclipse.jnosql.communication.Condition.LESSER_EQUALS_THAN;
import static org.eclipse.jnosql.communication.Condition.LESSER_THAN;
import static org.eclipse.jnosql.communication.Condition.LIKE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
public class RepositoryProxyPageRequestTest {

    private SemiStructuredTemplate template;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    @Inject
    private LifecycleEventHandler lifecycleEventHandler;

    private PersonRepository personRepository;

    private VendorRepository vendorRepository;


    @BeforeEach
    public void setUp() {
        this.template = Mockito.mock(SemiStructuredTemplate.class);

        SemiStructuredRepositoryProxy personHandler = new SemiStructuredRepositoryProxy(template,
                entities, PersonRepository.class, converters, lifecycleEventHandler);

        SemiStructuredRepositoryProxy vendorHandler = new SemiStructuredRepositoryProxy(template,
                entities, VendorRepository.class, converters, lifecycleEventHandler);

        when(template.insert(any(Person.class))).thenReturn(Person.builder().build());
        when(template.insert(any(Person.class), any(Duration.class))).thenReturn(Person.builder().build());
        when(template.update(any(Person.class))).thenReturn(Person.builder().build());

        personRepository = (PersonRepository) Proxy.newProxyInstance(PersonRepository.class.getClassLoader(),
                new Class[]{PersonRepository.class},
                personHandler);
        vendorRepository = (VendorRepository) Proxy.newProxyInstance(VendorRepository.class.getClassLoader(),
                new Class[]{VendorRepository.class}, vendorHandler);
    }


    @DisplayName("Should find by name instance")
    @Test
    public void shouldFindByNameInstance() {

        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        PageRequest pageRequest = getPageRequest();
        personRepository.findByName("name", pageRequest, Order.by());

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).singleResult(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
        assertThat(query.skip()).isEqualTo(pageRequest.size());
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));

        assertElement(condition.element(), Element.of("name", "name"));

        assertThat(personRepository.findByName("name", pageRequest, Order.by())).isNotNull();
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> personRepository.findByName("name", pageRequest, Order.by()))
                .isInstanceOf(EmptyResultException.class);
    }

    @DisplayName("Should find by name and age")
    @Test
    public void shouldFindByNameANDAge() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        PageRequest pageRequest = getPageRequest();
        List<Person> persons = personRepository.findByNameAndAge("name", 20, pageRequest);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        assertThat(persons).contains(ada);

        SelectQuery query = captor.getValue();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());

    }

    @DisplayName("Should find by age and name")
    @Test
    public void shouldFindByAgeANDName() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        PageRequest pageRequest = getPageRequest();
        Set<Person> persons = personRepository.findByAgeAndName(20, "name", pageRequest);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        assertThat(persons).contains(ada);
        SelectQuery query = captor.getValue();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());

    }

    @DisplayName("Should find by name and age order by name")
    @Test
    public void shouldFindByNameANDAgeOrderByName() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        PageRequest pageRequest = getPageRequest();

        Stream<Person> persons = personRepository.findByNameAndAgeOrderByName("name", 20, pageRequest);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        assertThat(persons.collect(Collectors.toList())).contains(ada);
        SelectQuery query = captor.getValue();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());

    }

    @DisplayName("Should find by name and age order by age")
    @Test
    public void shouldFindByNameANDAgeOrderByAge() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        PageRequest pageRequest = getPageRequest();
        Queue<Person> persons = personRepository.findByNameAndAgeOrderByAge("name", 20, pageRequest);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        assertThat(persons).contains(ada);
        SelectQuery query = captor.getValue();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());


    }


    @DisplayName("Should find all")
    @Test
    public void shouldFindAll() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        var cursor = Mockito.mock(CursoredPage.class);
        when(cursor.content()).thenReturn(List.of(ada));
        when(template.selectCursor(any(SelectQuery.class), any(PageRequest.class)))
                .thenReturn(cursor);

        PageRequest pageRequest = getPageRequest();

        List<Person> persons = personRepository.findAll(pageRequest, Order.by(Sort.asc("asd"))).content();
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).selectCursor(captor.capture(), any(PageRequest.class));
        SelectQuery query = captor.getValue();
        assertThat(query.condition().isPresent()).isFalse();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());
    }


    @DisplayName("Should find by name and age greater equal than")
    @Test
    public void shouldFindByNameAndAgeGreaterEqualThan() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        PageRequest pageRequest = getPageRequest();
        personRepository.findByNameAndAgeGreaterThanEqual("Ada", 33, pageRequest);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(condition.condition()).isEqualTo(AND);
        List<CriteriaCondition> conditions = condition.element().get(new TypeReference<>() {
        });
        CriteriaCondition columnCondition = conditions.get(0);
        CriteriaCondition columnCondition2 = conditions.get(1);

        assertThat(columnCondition.condition()).isEqualTo(Condition.EQUALS);
        assertThat(columnCondition.element().get()).isEqualTo("Ada");
        assertThat(columnCondition.element().name()).isEqualTo("name");

        assertThat(columnCondition2.condition()).isEqualTo(Condition.GREATER_EQUALS_THAN);
        assertThat(columnCondition2.element().get()).isEqualTo(33);
        assertThat(columnCondition2.element().name()).isEqualTo("age");
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());
    }

    @DisplayName("Should find by greater than")
    @Test
    public void shouldFindByGreaterThan() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        PageRequest pageRequest = getPageRequest();
        personRepository.findByAgeGreaterThan(33, pageRequest);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(condition.condition()).isEqualTo(GREATER_THAN);
        assertElement(condition.element(), Element.of("age", 33));
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());

    }

    @DisplayName("Should find by age less than equal")
    @Test
    public void shouldFindByAgeLessThanEqual() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        PageRequest pageRequest = getPageRequest();
        personRepository.findByAgeLessThanEqual(33, pageRequest);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(condition.condition()).isEqualTo(LESSER_EQUALS_THAN);
        assertElement(condition.element(), Element.of("age", 33));
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());

    }

    @DisplayName("Should find by age less equal")
    @Test
    public void shouldFindByAgeLessEqual() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        PageRequest pageRequest = getPageRequest();
        personRepository.findByAgeLessThan(33, pageRequest);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(condition.condition()).isEqualTo(LESSER_THAN);
        assertElement(condition.element(), Element.of("age", 33));
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());

    }

    @DisplayName("Should find by age between")
    @Test
    public void shouldFindByAgeBetween() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        PageRequest pageRequest = getPageRequest();
        personRepository.findByAgeBetween(10, 15, pageRequest);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(condition.condition()).isEqualTo(BETWEEN);
        List<Value> values = condition.element().get(new TypeReference<>() {
        });
        assertThat(values.stream().map(Value::get).collect(Collectors.toList())).isEqualTo(Arrays.asList(10, 15));
        assertThat(condition.element().name().contains("age")).isTrue();
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());
    }


    @DisplayName("Should find by name like")
    @Test
    public void shouldFindByNameLike() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        PageRequest pageRequest = getPageRequest();
        personRepository.findByNameLike("Ada", pageRequest);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(condition.condition()).isEqualTo(LIKE);
        assertElement(condition.element(), Element.of("name", "Ada"));
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());

    }


    @DisplayName("Should find by string when field is set")
    @Test
    public void shouldFindByStringWhenFieldIsSet() {
        Vendor vendor = new Vendor("vendor");
        vendor.setPrefixes(Collections.singleton("prefix"));

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(vendor));

        PageRequest pageRequest = getPageRequest();
        vendorRepository.findByPrefixes("prefix", pageRequest);

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).singleResult(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("vendors");
        assertThat(condition.condition()).isEqualTo(EQUALS);
        assertElement(condition.element(), Element.of("prefixes", "prefix"));
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());

    }

    @DisplayName("Should find by in")
    @Test
    public void shouldFindByIn() {
        Vendor vendor = new Vendor("vendor");
        vendor.setPrefixes(Collections.singleton("prefix"));

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(vendor));

        PageRequest pageRequest = getPageRequest();
        vendorRepository.findByPrefixesIn(singletonList("prefix"), pageRequest);

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).singleResult(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("vendors");
        assertThat(condition.condition()).isEqualTo(IN);
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());

    }

    @DisplayName("Should convert field to the type")
    @Test
    public void shouldConvertFieldToTheType() {
        Person ada = Person.builder()
                .age(20).name("Ada").build();

        when(template.select(any(SelectQuery.class)))
                .thenReturn(Stream.of(ada));

        PageRequest pageRequest = getPageRequest();
        Page<Person> slice = personRepository.findByAge("120", pageRequest);
        assertThat(slice).isNotNull();
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(condition.condition()).isEqualTo(EQUALS);
        assertElement(condition.element(), Element.of("age", 120));
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());
    }

    @DisplayName("Should find by name order name")
    @Test
    public void shouldFindByNameOrderName() {

        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        PageRequest pageRequest = getPageRequest();
        Sort<Person> name = Sort.asc("name");
        Order<Person> order = Order.by(name);
        personRepository.findByName("name", pageRequest, order);

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).singleResult(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(condition.condition()).isEqualTo(EQUALS);
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());
        assertThat(query.sorts()).hasSize(1)
                .contains(name);

        assertElement(condition.element(), Element.of("name", "name"));

        assertThat(personRepository.findByName("name", pageRequest, order)).isNotNull();
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> personRepository.findByName("name", pageRequest, order))
                .isInstanceOf(EmptyResultException.class);
    }

    @DisplayName("Should find by name order name 2")
    @Test
    public void shouldFindByNameOrderName2() {

        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        Sort<Person> name = Sort.asc("name");
        Order<Person> nameOrder = Order.by(name);
        PageRequest pageRequest = getPageRequest();
        Page<Person> page = personRepository.findByNameOrderByAge("name", pageRequest, nameOrder);

        assertThat(page).isNotNull();

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(condition.condition()).isEqualTo(EQUALS);
        assertThat(query.limit()).isEqualTo(NoSQLPage.skip(pageRequest));
        assertThat(query.limit()).isEqualTo(pageRequest.size());
        assertThat(query.sorts()).hasSize(2)
                .containsExactly(Sort.asc("age"), name);

        assertElement(condition.element(), Element.of("name", "name"));


        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> personRepository.findByName("name", pageRequest, nameOrder))
                .isInstanceOf(EmptyResultException.class);
    }

    @DisplayName("Should find by name sort")
    @Test
    public void shouldFindByNameSort() {
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        PageRequest pageRequest = getPageRequest();
        personRepository.findByName("name", Sort.asc("name"), pageRequest);

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(condition.condition()).isEqualTo(EQUALS);
        assertThat(query.sorts()).hasSize(1)
                .containsExactly(Sort.asc("name"));
        assertElement(condition.element(), Element.of("name", "name"));
    }

    @DisplayName("Should find by name sort pagination")
    @Test
    public void shouldFindByNameSortPagination() {
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        personRepository.findByName("name", Sort.asc("name"));

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(condition.condition()).isEqualTo(EQUALS);
        assertThat(query.sorts()).hasSize(1)
                .containsExactly(Sort.asc("name"));
        assertElement(condition.element(), Element.of("name", "name"));
    }

    @DisplayName("Should find by name limit")
    @Test
    public void shouldFindByNameLimit() {
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        personRepository.findByName("name", Limit.of(3), Sort.asc("name"));
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(query.skip()).isEqualTo(0);
        assertThat(query.limit()).isEqualTo(3);
        assertThat(condition.condition()).isEqualTo(EQUALS);
        assertThat(query.sorts()).hasSize(1)
                .containsExactly(Sort.asc("name"));
        assertElement(condition.element(), Element.of("name", "name"));
    }

    @DisplayName("Should find by name limit 2")
    @Test
    public void shouldFindByNameLimit2() {
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        personRepository.findByName("name", Limit.range(1, 3), Sort.asc("name"));
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(query.skip()).isEqualTo(0);
        assertThat(query.limit()).isEqualTo(3);
        assertThat(condition.condition()).isEqualTo(EQUALS);
        assertThat(query.sorts()).hasSize(1)
                .containsExactly(Sort.asc("name"));
        assertElement(condition.element(), Element.of("name", "name"));
    }

    @DisplayName("Should find by name limit 3")
    @Test
    public void shouldFindByNameLimit3() {
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        personRepository.findByName("name", Limit.range(2, 3), Sort.asc("name"));
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        CriteriaCondition condition = query.condition().get();
        assertThat(query.name()).isEqualTo("Person");
        assertThat(query.skip()).isEqualTo(1);
        assertThat(query.limit()).isEqualTo(2);
        assertThat(condition.condition()).isEqualTo(EQUALS);
        assertThat(query.sorts()).hasSize(1)
                .containsExactly(Sort.asc("name"));
        assertElement(condition.element(), Element.of("name", "name"));
    }

    @DisplayName("Should find by name order by name")
    @Test
    public void shouldFindByNameOrderByName() {
        CursoredPage<Person> mock = Mockito.mock(CursoredPage.class);

        when(template.<Person>selectCursor(any(SelectQuery.class),
                any(PageRequest.class))).thenReturn(mock);

        CursoredPage<Person> page = personRepository.findByNameOrderByName("name",
                PageRequest.afterCursor(PageRequest.Cursor.forKey("Ada"), 1, 10, false));

        SoftAssertions.assertSoftly(s -> s.assertThat(page).isEqualTo(mock));
    }

    @DisplayName("Should mach parameter")
    @Test
    public void shouldMachParameter() {
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        personRepository.parameter("name", 10);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.skip()).isEqualTo(0);
            soft.assertThat(query.limit()).isEqualTo(0);
            soft.assertThat(query.condition().isPresent()).isTrue();
            soft.assertThat(query.sorts()).hasSize(0);
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(AND);
            List<CriteriaCondition> conditions = condition.element().get(new TypeReference<>() {
            });
            soft.assertThat(conditions).hasSize(2);
            soft.assertThat(conditions.get(0)).isEqualTo(CriteriaCondition.eq(Element.of("name", "name")));
            soft.assertThat(conditions.get(1)).isEqualTo(CriteriaCondition.eq(Element.of("age", 10)));

        });
    }

    @DisplayName("Should parameter match")
    @Test
    public void shouldParameterMatch() {
        CursoredPage<Person> mock = Mockito.mock(CursoredPage.class);
        when(template.<Person>selectCursor(any(SelectQuery.class),
                any(PageRequest.class))).thenReturn(mock);

        CursoredPage<Person> page = personRepository.findPageParameter("name",
                PageRequest.afterCursor(PageRequest.Cursor.forKey("Ada"), 1, 10, false));

        SoftAssertions.assertSoftly(s -> s.assertThat(page).isEqualTo(mock));

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).selectCursor(captor.capture(), Mockito.any());
        var query = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.skip()).isEqualTo(0);
            soft.assertThat(query.limit()).isEqualTo(10);
            soft.assertThat(query.condition().isPresent()).isTrue();
            soft.assertThat(query.sorts()).hasSize(0);
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(EQUALS);
            assertElement(condition.element(), Element.of("name", "name"));

        });
    }

    @DisplayName("Should parameter query")
    @Test
    public void shouldParameterQuery() {
        CursoredPage<Person> mock = Mockito.mock(CursoredPage.class);
        var prepare = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);

        when(prepare.selectQuery()).thenReturn(Optional.of(SelectQuery.select().from("Person").where("name").eq("ada").build()));
        when(template.<Person>selectCursor(any(SelectQuery.class),
                any(PageRequest.class))).thenReturn(mock);

        when(template.prepare(any(String.class),
                any(String.class))).thenReturn(prepare);

        CursoredPage<Person> page = personRepository.cursorJQDL("name",
                PageRequest.afterCursor(PageRequest.Cursor.forKey("Ada"), 1, 10, false));

        SoftAssertions.assertSoftly(s -> s.assertThat(page).isEqualTo(mock));

        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).selectCursor(captor.capture(), Mockito.any());
        var query = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.skip()).isEqualTo(0);
            soft.assertThat(query.condition().isPresent()).isTrue();
            soft.assertThat(query.sorts()).hasSize(0);
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(EQUALS);
        });
    }


    @DisplayName("Should find by cursor by order")
    @Test
    public void shouldFindByCursorByOrder() {
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        PageRequest pageRequest = PageRequest.ofSize(10);
        Order<Person> order = Order.by(Sort.asc("name"));
        personRepository.findAll(pageRequest, order);
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).selectCursor(captor.capture(), Mockito.eq(pageRequest));
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.skip()).isEqualTo(0);
            soft.assertThat(query.limit()).isEqualTo(10);
            soft.assertThat(query.sorts()).hasSize(1);
            soft.assertThat(query.sorts()).contains(Sort.asc("name"));
            soft.assertThat(query.condition()).isEmpty();

        });
    }

    @DisplayName("Should find by cursor by sort")
    @Test
    public void shouldFindByCursorBySort() {
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        PageRequest pageRequest = PageRequest.ofSize(10);
        personRepository.findAll(pageRequest, Sort.asc("name"));
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).selectCursor(captor.capture(), Mockito.eq(pageRequest));
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.skip()).isEqualTo(0);
            soft.assertThat(query.limit()).isEqualTo(10);
            soft.assertThat(query.sorts()).hasSize(1);
            soft.assertThat(query.sorts()).contains(Sort.asc("name"));
            soft.assertThat(query.condition()).isEmpty();

        });
    }

    @DisplayName("Should page")
    @Test
    public void shouldPage() {
        when(template.singleResult(any(SelectQuery.class))).thenReturn(Optional
                .of(Person.builder().build()));

        PageRequest pageRequest = PageRequest.ofSize(10);
        personRepository.pageAll(pageRequest, Sort.asc("name"));
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.skip()).isEqualTo(0);
            soft.assertThat(query.limit()).isEqualTo(10);
            soft.assertThat(query.sorts()).hasSize(1);
            soft.assertThat(query.sorts()).contains(Sort.asc("name"));
            soft.assertThat(query.condition()).isEmpty();

        });
    }

    @DisplayName("Should provide the bound paged query to lazy totals without order by")
    @Test
    public void shouldProvideBoundPagedQueryToLazyTotalsWithoutOrderBy() {
        Person ada = Person.builder().age(20).name("Ada").build();
        var prepare = Mockito.mock(org.eclipse.jnosql.mapping.semistructured.PreparedStatement.class);
        var selectMapper = new AtomicReference<UnaryOperator<SelectQuery>>();
        var mappedQuery = new AtomicReference<SelectQuery>();
        SelectQuery parsedQuery = SelectQuery.select().from("Person").where("name").eq("Ada").build();

        Mockito.doAnswer(invocation -> {
            selectMapper.set(invocation.getArgument(0));
            return null;
        }).when(prepare).setSelectMapper(any());
        when(prepare.result()).thenAnswer(invocation -> {
            SelectQuery query = selectMapper.get().apply(parsedQuery);
            mappedQuery.set(query);
            return Stream.of(ada);
        });
        when(template.prepare("select * from Person where name = :name", "Person")).thenReturn(prepare);
        when(template.count(any(SelectQuery.class))).thenReturn(14L);

        PageRequest pageRequest = PageRequest.ofPage(2, 5, true);
        Page<Person> page = personRepository.pageJdql("Ada", pageRequest);

        verify(template, Mockito.never()).count(any(SelectQuery.class));
        assertThat(page.content()).containsExactly(ada);
        assertThat(page.totalElements()).isEqualTo(14L);
        assertThat(page.totalPages()).isEqualTo(3L);
        assertThat(page.hasTotals()).isTrue();
        verify(prepare).bind("name", "Ada");

        ArgumentCaptor<SelectQuery> countQuery = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).count(countQuery.capture());
        SelectQuery query = countQuery.getValue();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(query).isSameAs(mappedQuery.get());
            soft.assertThat(query.name()).isEqualTo("Person");
            soft.assertThat(query.skip()).isEqualTo(5L);
            soft.assertThat(query.limit()).isEqualTo(5L);
            soft.assertThat(query.sorts()).isEmpty();
            soft.assertThat(query.condition()).contains(CriteriaCondition.eq(Element.of("name", "Ada")));
        });
    }

    private PageRequest getPageRequest() {
        return PageRequest.ofPage(2).size(6);
    }

    interface PersonRepository extends BasicRepository<Person, Long> {

        Person findByName(String name, PageRequest pageRequest, Order<Person> order);

        @Find
        List<Person> parameter(@By("name") String name, @By("age") Integer age);

        @Find
        CursoredPage<Person> findAll(PageRequest pageRequest, Order<Person> order);

        @Find
        CursoredPage<Person> findAll(PageRequest pageRequest, Sort<Person> order);

        @Find
        Page<Person> pageAll(PageRequest pageRequest, Sort<Person> order);
        CursoredPage<Person> findByNameOrderByName(String name, PageRequest pageRequest);

        @Find
        CursoredPage<Person> findPageParameter(@By("name") String name, PageRequest pageRequest);

        @Query("select * from Person where name = :name")
        CursoredPage<Person> cursorJQDL(@Param("name") String name, PageRequest pageRequest);

        @Query("select * from Person where name = :name")
        Page<Person> pageJdql(@Param("name") String name, PageRequest pageRequest);

        List<Person> findByName(String name, Sort<Person> sort);

        List<Person> findByName(String name, Limit limit, Sort<Person> sort);

        List<Person> findByName(String name, Sort<Person> sort, PageRequest pageRequest);

        Page<Person> findByNameOrderByAge(String name, PageRequest pageRequest, Order<Person> order);

        Page<Person> findByAge(String age, PageRequest pageRequest);

        List<Person> findByNameAndAge(String name, Integer age, PageRequest pageRequest);

        Set<Person> findByAgeAndName(Integer age, String name, PageRequest pageRequest);

        Stream<Person> findByNameAndAgeOrderByName(String name, Integer age, PageRequest pageRequest);

        Queue<Person> findByNameAndAgeOrderByAge(String name, Integer age, PageRequest pageRequest);

        Set<Person> findByNameAndAgeGreaterThanEqual(String name, Integer age, PageRequest pageRequest);

        Set<Person> findByAgeGreaterThan(Integer age, PageRequest pageRequest);

        Set<Person> findByAgeLessThanEqual(Integer age, PageRequest pageRequest);

        Set<Person> findByAgeLessThan(Integer age, PageRequest pageRequest);

        Set<Person> findByAgeBetween(Integer ageA, Integer ageB, PageRequest pageRequest);

        Set<Person> findByNameLike(String name, PageRequest pageRequest);

    }

    public interface VendorRepository extends BasicRepository<Vendor, String> {

        Optional<Vendor> findByPrefixes(String prefix, PageRequest pageRequest);

        Optional<Vendor> findByPrefixesIn(List<String> prefix, PageRequest pageRequest);

    }

    private static void assertElement(Element actual, Element expected) {
        assertThat(actual.name()).isEqualTo(expected.name());
        assertThat(actual.get()).isEqualTo(expected.get());
    }

    @Nested
    @DisplayName("When the repository proxy page request is tested")
    class WhenTheRepositoryProxyPageRequestIsTested {
    }
}
