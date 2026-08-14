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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.DisplayName;
import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.data.restrict.Restriction;
import org.assertj.core.api.SoftAssertions;
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

class SpecialParametersTest {

    private static final Function<String, String> SORT_MAPPER = Function.identity();























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

    @Nested
    @DisplayName("When the special parameters operates")
    class WhenTheSpecialParametersOperates {

        @DisplayName("Should return empty")
        @Test
        void shouldReturnEmpty() {
            SpecialParameters parameters = SpecialParameters.of(new Object[0], SORT_MAPPER);
            assertThat(parameters.isEmpty()).isTrue();
        }
        @DisplayName("Should return empty non special parameters")
        @Test
        void shouldReturnEmptyNonSpecialParameters() {
            SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio"}, SORT_MAPPER);
            assertThat(parameters.isEmpty()).isTrue();
        }
        @DisplayName("Should return page request")
        @Test
        void shouldReturnPageRequest() {
            PageRequest pageRequest = PageRequest.ofPage(10);
            SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", pageRequest}, SORT_MAPPER);
            assertThat(parameters.isEmpty()).isFalse();
            assertThat(parameters.pageRequest().orElseThrow()).isEqualTo(pageRequest);
            assertThat(parameters.isSortEmpty()).isTrue();
        }
        @DisplayName("Should return sort")
        @Test
        void shouldReturnSort() {
            Sort<?> sort = Sort.asc("name");
            SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", sort}, SORT_MAPPER);
            assertThat(parameters.isEmpty()).isFalse();
            assertThat(parameters.hasOnlySort()).isTrue();
            assertThat(parameters.pageRequest().isEmpty()).isTrue();
            assertThat(parameters.isSortEmpty()).isFalse();
            assertThat(parameters.sorts()).hasSize(1)
                    .contains(Sort.asc("name"));
        }
        @DisplayName("Should keep order")
        @Test
        void shouldKeepOrder() {
            Sort<?> sort = Sort.asc("name");
            PageRequest pageRequest = PageRequest.ofPage(10);

            SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", sort, pageRequest}, SORT_MAPPER);
            assertThat(parameters.isEmpty()).isFalse();
            assertThat(parameters.hasOnlySort()).isFalse();
            assertThat(parameters.pageRequest().orElseThrow()).isEqualTo(pageRequest);
            assertThat(parameters.isSortEmpty()).isFalse();
            assertThat(parameters.sorts()).hasSize(1)
                    .containsExactly(sort);
        }
        @DisplayName("Should return limit")
        @Test
        void shouldReturnLimit() {
            SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", Limit.of(10)}, SORT_MAPPER);
            assertThat(parameters.isEmpty()).isFalse();
            Optional<Limit> limit = parameters.limit();
            assertThat(limit.isPresent()).isTrue();
            Limit limit1 = limit.orElseThrow();
            assertThat(limit1.startAt()).isEqualTo(1);
            assertThat(limit1.maxResults()).isEqualTo(10);
        }
        @DisplayName("Should return iterable sort")
        @Test
        void shouldReturnIterableSort(){
            SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio",
                    List.of(Sort.asc("name"), Sort.desc("age"))}, SORT_MAPPER);
            assertThat(parameters.isEmpty()).isFalse();
            assertThat(parameters.sorts()).hasSize(2)
                    .containsExactly(Sort.asc("name"),
                            Sort.desc("age"));
        }
        @DisplayName("Should return order")
        @Test
        void shouldReturnOrder(){
            Order<?> order = Order.by(Sort.asc("name"), Sort.desc("age"));
            SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", order}, SORT_MAPPER);
            assertThat(parameters.isEmpty()).isFalse();
            assertThat(parameters.isSortEmpty()).isFalse();
            assertThat(parameters.sorts()).hasSize(2)
                    .contains(Sort.asc("name"),
                            Sort.desc("age"));
        }
        @DisplayName("Should return iterable order")
        @Test
        void shouldReturnIterableOrder(){
            PageRequest pageRequest = PageRequest.ofPage(10);
            Order<?> order = Order.by(Sort.asc("name"));
            SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio",
                    List.of(Sort.asc("name"), Sort.desc("age")), pageRequest, order}, SORT_MAPPER);
            assertThat(parameters.isEmpty()).isFalse();
            assertThat(parameters.pageRequest().orElseThrow()).isEqualTo(pageRequest);
            assertThat(parameters.isSortEmpty()).isFalse();
            assertThat(parameters.sorts()).hasSize(3)
                    .contains(Sort.asc("name"),
                            Sort.desc("age"));
        }
        @DisplayName("Should return array order")
        @Test
        void shouldReturnArrayOrder(){
            SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio",
                    new Sort[]{Sort.asc("name"), Sort.desc("age")}}, SORT_MAPPER);
            assertThat(parameters.isEmpty()).isFalse();
            assertThat(parameters.sorts()).hasSize(2)
                    .containsExactly(Sort.asc("name"),
                            Sort.desc("age"));
        }
        @DisplayName("Should return order mapper")
        @Test
        void shouldReturnOrderMapper(){
            Function<String, String> upper = String::toUpperCase;
            Order<?> order = Order.by(Sort.asc("name"), Sort.desc("age"));
            SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", order}, upper);
            assertThat(parameters.isEmpty()).isFalse();
            assertThat(parameters.isSortEmpty()).isFalse();
            assertThat(parameters.sorts()).hasSize(2)
                    .contains(Sort.asc("NAME"),
                            Sort.desc("AGE"));
        }
        @DisplayName("Should return iterable order mapper")
        @Test
        void shouldReturnIterableOrderMapper(){
            Function<String, String> upper = String::toUpperCase;
            PageRequest pageRequest = PageRequest.ofPage(10);
            Order<?> order = Order.by(Sort.asc("name"));
            SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio",
                    List.of(Sort.asc("name"), Sort.desc("age")), pageRequest, order}, upper);
            assertThat(parameters.isEmpty()).isFalse();
            assertThat(parameters.pageRequest().orElseThrow()).isEqualTo(pageRequest);
            assertThat(parameters.isSortEmpty()).isFalse();
            assertThat(parameters.sorts()).hasSize(3)
                    .contains(Sort.asc("NAME"),
                            Sort.desc("AGE"));
        }
        @DisplayName("Should return restriction")
        @Test
        void shouldReturnRestriction() {
            Restriction<String> restriction = () -> null;
            SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", restriction}, SORT_MAPPER);
            assertThat(parameters.isEmpty()).isFalse();
            Optional<Restriction<?>> restrictionOptional = parameters.restriction();
            assertThat(restrictionOptional.isPresent()).isTrue();
            Restriction<?> restriction1 = restrictionOptional.orElseThrow();
            assertThat(restriction1).isEqualTo(restriction);
        }
        @DisplayName("Should check to string")
        @Test
        void shouldCheckToString() {
            Restriction<String> restriction = () -> null;
            SpecialParameters parameters = SpecialParameters.of(new Object[]{10, "Otavio", restriction}, SORT_MAPPER);
            assertThat(parameters.toString()).isNotNull();
        }
        @DisplayName("Should check equals")
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
        @DisplayName("Should return true special parameter")
        @ParameterizedTest
        @ValueSource(classes = {Sort.class, Limit.class, PageRequest.class, Order.class, Restriction.class})
        void shouldReturnTrueSpecialParameter(Class<?> type){
            assertThat(SpecialParameters.isSpecialParameter(type)).isTrue();
        }
        @DisplayName("Should return not special parameter")
        @ParameterizedTest
        @ValueSource(classes = {String.class, Integer.class, Long.class, Double.class, Float.class, Boolean.class, Object.class})
        void shouldReturnNotSpecialParameter(Class<?> type){
            assertThat(SpecialParameters.isNotSpecialParameter(type)).isTrue();
        }
        @DisplayName("Should return true special parameter")
        @ParameterizedTest
        @MethodSource("org.eclipse.jnosql.mapping.core.repository.SpecialParametersTest#provideSpecialParameters")
        void shouldReturnTrueSpecialParameter(Object parameter){
            assertThat(SpecialParameters.isSpecialParameter(parameter)).isTrue();
        }
        @DisplayName("Should return not special parameter")
        @ParameterizedTest
        @MethodSource("org.eclipse.jnosql.mapping.core.repository.SpecialParametersTest#provideNonSpecialParameters")
        void shouldReturnNotSpecialParameter(Object parameter){
            assertThat(SpecialParameters.isNotSpecialParameter(parameter)).isTrue();
        }
    }
}
