/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.repository;

import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.page.PageRequest;
import jakarta.data.Sort;
import jakarta.data.restrict.Restriction;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SpecialParametersTest {

    private static final Function<String, String> SORT_MAPPER = Function.identity();

    @Test
    void shouldReturnEmpty() {
        SpecialParameters parameters = SpecialParameters.of(new Object[0], SORT_MAPPER);
        assertTrue(parameters.isEmpty());
    }

    @Test
    void shouldReturnEmptyNonSpecialParameters() {
        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio"}, SORT_MAPPER);
        assertTrue(parameters.isEmpty());
    }

    @Test
    void shouldReturnPageRequest() {
        PageRequest pageRequest = PageRequest.ofPage(10);
        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", pageRequest}, SORT_MAPPER);
        assertFalse(parameters.isEmpty());
        Assertions.assertEquals(pageRequest, parameters.pageRequest().orElseThrow());
        assertTrue(parameters.isSortEmpty());
    }

    @Test
    void shouldReturnSort() {
        Sort<?> sort = Sort.asc("name");
        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", sort}, SORT_MAPPER);
        assertFalse(parameters.isEmpty());
        assertTrue(parameters.hasOnlySort());
        assertTrue(parameters.pageRequest().isEmpty());
        assertFalse(parameters.isSortEmpty());
        assertThat(parameters.sorts()).hasSize(1)
                .contains(Sort.asc("name"));
    }

    @Test
    void shouldKeepOrder() {
        Sort<?> sort = Sort.asc("name");
        PageRequest pageRequest = PageRequest.ofPage(10);

        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", sort, pageRequest}, SORT_MAPPER);
        assertFalse(parameters.isEmpty());
        assertFalse(parameters.hasOnlySort());
        Assertions.assertEquals(pageRequest, parameters.pageRequest().orElseThrow());
        assertFalse(parameters.isSortEmpty());
        assertThat(parameters.sorts()).hasSize(1)
                .containsExactly(sort);
    }

    @Test
    void shouldReturnLimit() {
        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", Limit.of(10)}, SORT_MAPPER);
        assertFalse(parameters.isEmpty());
        Optional<Limit> limit = parameters.limit();
        assertTrue(limit.isPresent());
        Limit limit1 = limit.orElseThrow();
        assertEquals(1, limit1.startAt());
        assertEquals(10, limit1.maxResults());
    }

    @Test
    void shouldReturnIterableSort(){
        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio",
                List.of(Sort.asc("name"), Sort.desc("age"))}, SORT_MAPPER);
        assertFalse(parameters.isEmpty());
        assertThat(parameters.sorts()).hasSize(2)
                .containsExactly(Sort.asc("name"),
                        Sort.desc("age"));
    }

    @Test
    void shouldReturnOrder(){
        Order<?> order = Order.by(Sort.asc("name"), Sort.desc("age"));
        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", order}, SORT_MAPPER);
        assertFalse(parameters.isEmpty());
        assertFalse(parameters.isSortEmpty());
        assertThat(parameters.sorts()).hasSize(2)
                .contains(Sort.asc("name"),
                        Sort.desc("age"));
    }

    @Test
    void shouldReturnIterableOrder(){
        PageRequest pageRequest = PageRequest.ofPage(10);
        Order<?> order = Order.by(Sort.asc("name"));
        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio",
                List.of(Sort.asc("name"), Sort.desc("age")), pageRequest, order}, SORT_MAPPER);
        assertFalse(parameters.isEmpty());
        Assertions.assertEquals(pageRequest, parameters.pageRequest().orElseThrow());
        assertFalse(parameters.isSortEmpty());
        assertThat(parameters.sorts()).hasSize(3)
                .contains(Sort.asc("name"),
                        Sort.desc("age"));
    }

    @Test
    void shouldReturnArrayOrder(){
        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio",
                new Sort[]{Sort.asc("name"), Sort.desc("age")}}, SORT_MAPPER);
        assertFalse(parameters.isEmpty());
        assertThat(parameters.sorts()).hasSize(2)
                .containsExactly(Sort.asc("name"),
                        Sort.desc("age"));
    }

    @Test
    void shouldReturnOrderMapper(){
        Function<String, String> upper = String::toUpperCase;
        Order<?> order = Order.by(Sort.asc("name"), Sort.desc("age"));
        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", order}, upper);
        assertFalse(parameters.isEmpty());
        assertFalse(parameters.isSortEmpty());
        assertThat(parameters.sorts()).hasSize(2)
                .contains(Sort.asc("NAME"),
                        Sort.desc("AGE"));
    }

    @Test
    void shouldReturnIterableOrderMapper(){
        Function<String, String> upper = String::toUpperCase;
        PageRequest pageRequest = PageRequest.ofPage(10);
        Order<?> order = Order.by(Sort.asc("name"));
        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio",
                List.of(Sort.asc("name"), Sort.desc("age")), pageRequest, order}, upper);
        assertFalse(parameters.isEmpty());
        Assertions.assertEquals(pageRequest, parameters.pageRequest().orElseThrow());
        assertFalse(parameters.isSortEmpty());
        assertThat(parameters.sorts()).hasSize(3)
                .contains(Sort.asc("NAME"),
                        Sort.desc("AGE"));
    }

    @Test
    void shouldReturnRestriction() {
        Restriction<String> restriction = () -> null;
        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", restriction}, SORT_MAPPER);
        assertFalse(parameters.isEmpty());
        Optional<Restriction<?>> restrictionOptional = parameters.restriction();
        assertTrue(restrictionOptional.isPresent());
        Restriction<?> restriction1 = restrictionOptional.orElseThrow();
        assertEquals(restriction, restriction1);
    }

    @Test
    void shouldCheckToString() {
        Restriction<String> restriction = () -> null;
        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", restriction}, SORT_MAPPER);
        Assertions.assertNotNull(parameters.toString());
    }

    @Test
    void shouldCheckEquals() {
        Restriction<String> restriction = () -> null;
        SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", restriction}, SORT_MAPPER);
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(parameters).isEqualTo(SpecialParameters.of(new Object[]{10, "Otavio", restriction}, SORT_MAPPER));
            soft.assertThat(parameters).isNotEqualTo(null);
            soft.assertThat(parameters).isNotEqualTo(new Object());
            soft.assertThat(parameters).isNotEqualTo(SpecialParameters.of(new Object[]{10, "Otavio"}, SORT_MAPPER));
            soft.assertThat(parameters).isNotEqualTo(SpecialParameters.of(new Object[]{10, "Otavio", Sort.asc("name")}, SORT_MAPPER));
            soft.assertThat(parameters.hashCode()).isEqualTo(SpecialParameters.of(new Object[]{10, "Otavio", restriction}, SORT_MAPPER).hashCode());
        });
    }



    @ParameterizedTest
    @ValueSource(classes = {Sort.class, Limit.class, PageRequest.class, Order.class, Restriction.class})
    void shouldReturnTrueSpecialParameter(Class<?> type){
        org.assertj.core.api.Assertions.assertThat(SpecialParameters.isSpecialParameter(type)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(classes = {String.class, Integer.class, Long.class, Double.class, Float.class, Boolean.class, Object.class})
    void shouldReturnNotSpecialParameter(Class<?> type){
        org.assertj.core.api.Assertions.assertThat(SpecialParameters.isNotSpecialParameter(type)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideSpecialParameters")
    void shouldReturnTrueSpecialParameter(Object parameter){
        org.assertj.core.api.Assertions.assertThat(SpecialParameters.isSpecialParameter(parameter)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideNonSpecialParameters")
    void shouldReturnNotSpecialParameter(Object parameter){
        org.assertj.core.api.Assertions.assertThat(SpecialParameters.isNotSpecialParameter(parameter)).isTrue();
    }


    private static Stream<Arguments> provideSpecialParameters() {
        return Stream.of(Arguments.of(Sort.asc("name")),
                Arguments.of(Limit.of(10)),
                Arguments.of(PageRequest.ofPage(10)),
                Arguments.of(Order.by(Sort.asc("name"), Sort.desc("age"))));
    }

    private static Stream<Arguments> provideNonSpecialParameters() {
        return Stream.of(Arguments.of("123"),
                Arguments.of(10L),
                Arguments.of(BigDecimal.valueOf(10)),
                Arguments.of(Boolean.TRUE));
    }
}