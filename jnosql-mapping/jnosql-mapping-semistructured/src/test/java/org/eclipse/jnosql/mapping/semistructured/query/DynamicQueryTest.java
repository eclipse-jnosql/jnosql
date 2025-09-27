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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.data.Limit;
import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.core.repository.SpecialParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThatThrownBy;



class DynamicQueryTest {

    @Mock
    private SpecialParameters special;

    @Mock
    private SelectQuery query;

    @Mock
    private Limit limit;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateDynamicQuery() {
        when(special.isEmpty()).thenReturn(true);
        when(query.condition()).thenReturn(Optional.empty());
        when(query.name()).thenReturn("sampleQuery");

        DynamicQuery dynamicQuery = new DynamicQuery(special, query);

        assertEquals(query, dynamicQuery.get());
    }

    @Test
    void shouldCreateDynamicQueryWithSortsAndLimit() {
        when(special.isEmpty()).thenReturn(false);
        when(special.hasOnlySort()).thenReturn(false);
        when(special.limit()).thenReturn(Optional.of(Limit.range(1,5)));
        when(query.condition()).thenReturn(Optional.empty());
        when(query.name()).thenReturn("sampleQuery");
        when(query.sorts()).thenReturn(List.of(Sort.asc("name"), Sort.desc("age")));
        when(query.skip()).thenReturn(0L);
        when(query.limit()).thenReturn(10L);

        DynamicQuery dynamicQuery = new DynamicQuery(special, query);
        SelectQuery selectQuery = dynamicQuery.get();

        assertSoftly(softly -> {
            softly.assertThat(selectQuery.name()).isEqualTo("sampleQuery");
            softly.assertThat(selectQuery.skip()).isEqualTo(0);
            softly.assertThat(selectQuery.limit()).isEqualTo(5L);
            softly.assertThat(selectQuery.sorts()).hasSize(2);
        });
    }

    @Test
    void shouldCreateDynamicQueryWithLimit() {
        when(special.isEmpty()).thenReturn(false);
        when(special.hasOnlySort()).thenReturn(false);
        when(limit.startAt()).thenReturn(1L);
        when(limit.maxResults()).thenReturn(5);
        when(special.limit()).thenReturn(Optional.of(limit));
        when(query.condition()).thenReturn(Optional.empty());
        when(query.name()).thenReturn("sampleQuery");
        when(query.sorts()).thenReturn(Collections.emptyList());
        when(query.skip()).thenReturn(0L);
        when(query.limit()).thenReturn(10L);

        DynamicQuery dynamicQuery = new DynamicQuery(special, query);

        SelectQuery selectQuery = dynamicQuery.get();

        assertSoftly(softly -> {
            softly.assertThat(selectQuery.name()).isEqualTo("sampleQuery");
            softly.assertThat(selectQuery.skip()).isEqualTo(0);
            softly.assertThat(selectQuery.limit()).isEqualTo(5);
            softly.assertThat(selectQuery.sorts()).isEmpty();
        });
    }

    @Test
    void shouldCreateDynamicQueryWithPageRequest() {
        when(special.isEmpty()).thenReturn(false);
        when(special.isEmpty()).thenReturn(false);
        when(special.sorts()).thenReturn(List.of(mock(Sort.class)));
        when(query.condition()).thenReturn(Optional.empty());
        when(query.name()).thenReturn("sampleQuery");
        when(query.sorts()).thenReturn(List.of(mock(Sort.class)));
        when(query.skip()).thenReturn(0L);
        when(query.limit()).thenReturn(10L);

        DynamicQuery dynamicQuery = new DynamicQuery(special, query);

        SelectQuery selectQuery = dynamicQuery.get();
        assertEquals("sampleQuery", selectQuery.name());
        assertEquals(0, selectQuery.skip());
        assertEquals(10, selectQuery.limit());
        assertEquals(1, selectQuery.sorts().size());
    }

    @Test
    void shouldReturnWhenThereIsLimitAndSort(){
        when(special.isEmpty()).thenReturn(false);
        when(special.pageRequest()).thenReturn(Optional.of(mock(PageRequest.class)));
        when(query.condition()).thenReturn(Optional.empty());
        when(query.name()).thenReturn("sampleQuery");
        when(query.sorts()).thenReturn(Collections.emptyList());
        when(query.skip()).thenReturn(0L);
        when(query.limit()).thenReturn(10L);

        DynamicQuery dynamicQuery = DynamicQuery.of(new Object[]{Sort.asc("name"), Limit.of(20)}
        , query);

        SelectQuery columnQuery = dynamicQuery.get();
        assertEquals("sampleQuery", columnQuery.name());
        assertEquals(0, columnQuery.skip());
        assertEquals(20, columnQuery.limit());
        assertEquals(1, columnQuery.sorts().size());
    }

    @Test
    void shouldMergeSortsWhenHasOnlySortAndNoLimit() {
        when(special.isEmpty()).thenReturn(false);
        when(special.hasOnlySort()).thenReturn(true);
        when(special.limit()).thenReturn(Optional.empty());
        when(special.sorts()).thenReturn(List.of(Sort.asc("lastName")));
        when(query.sorts()).thenReturn(List.of(Sort.asc("name")));
        when(query.skip()).thenReturn(5L);
        when(query.limit()).thenReturn(10L);
        when(query.condition()).thenReturn(Optional.empty());
        when(query.name()).thenReturn("sampleQuery");

        SelectQuery result = new DynamicQuery(special, query).get();

        assertSoftly(softly -> {
            softly.assertThat(result.name()).isEqualTo("sampleQuery");
            softly.assertThat(result.skip()).isEqualTo(5L);      // from original query
            softly.assertThat(result.limit()).isEqualTo(10L);    // from original query
            softly.assertThat(result.sorts()).hasSize(2);        // query.sorts + special.sorts
        });
    }

    @Test
    void shouldMergeSortsAndApplyLimitWhenHasOnlySort() {
        when(special.isEmpty()).thenReturn(false);
        when(special.hasOnlySort()).thenReturn(true);
        when(special.limit()).thenReturn(Optional.of(Limit.range(3, 7)));
        when(special.sorts()).thenReturn(List.of(Sort.asc("lastName")));
        when(query.sorts()).thenReturn(List.of(Sort.desc("age")));
        when(query.skip()).thenReturn(0L);
        when(query.limit()).thenReturn(50L);
        when(query.condition()).thenReturn(Optional.empty());
        when(query.name()).thenReturn("sampleQuery");

        SelectQuery result = new DynamicQuery(special, query).get();

        assertSoftly(softly -> {
            softly.assertThat(result.name()).isEqualTo("sampleQuery");
            softly.assertThat(result.skip()).isEqualTo(2L);
            softly.assertThat(result.limit()).isEqualTo(5L);
            softly.assertThat(result.sorts()).hasSize(2);
        });
    }

    @Test
    void shouldAppendSpecialSortsWhenLimitPresent() {
        when(special.isEmpty()).thenReturn(false);
        when(special.hasOnlySort()).thenReturn(false);
        when(special.limit()).thenReturn(Optional.of(Limit.of(10)));
        when(special.sorts()).thenReturn(List.of(Sort.asc("city"))); // appended after query sorts
        when(query.sorts()).thenReturn(List.of(Sort.asc("name")));
        when(query.skip()).thenReturn(0L);
        when(query.limit()).thenReturn(100L);
        when(query.condition()).thenReturn(Optional.empty());
        when(query.name()).thenReturn("sampleQuery");

        SelectQuery result = new DynamicQuery(special, query).get();

        assertSoftly(softly -> {
            softly.assertThat(result.limit()).isEqualTo(10L);
            softly.assertThat(result.skip()).isEqualTo(0L);
            softly.assertThat(result.sorts()).hasSize(2); // query.sorts then special.sorts
        });
    }

    @Test
    void shouldUsePageRequestWhenPresentMergingSorts() {
        when(special.isEmpty()).thenReturn(false);
        when(special.hasOnlySort()).thenReturn(false);
        when(special.limit()).thenReturn(Optional.empty());
        when(special.sorts()).thenReturn(List.of(Sort.desc("updatedAt")));
        PageRequest page = PageRequest.ofPage(2); // use real PageRequest to avoid static mocking
        when(special.pageRequest()).thenReturn(Optional.of(page));

        when(query.sorts()).thenReturn(List.of(Sort.asc("id")));
        when(query.condition()).thenReturn(Optional.empty());
        when(query.name()).thenReturn("sampleQuery");

        SelectQuery result = new DynamicQuery(special, query).get();

        long expectedSkip = NoSQLPage.skip(page); // use same util to compute expectation

        assertSoftly(softly -> {
            softly.assertThat(result.name()).isEqualTo("sampleQuery");
            softly.assertThat(result.limit()).isEqualTo(page.size());
            softly.assertThat(result.skip()).isEqualTo(expectedSkip);
            softly.assertThat(result.sorts()).hasSize(2); // query.sorts + special.sorts
        });
    }

    @Test
    void shouldReturnOriginalQueryWhenNoLimitNorPageRequestAndNotOnlySort() {
        when(special.isEmpty()).thenReturn(false);
        when(special.hasOnlySort()).thenReturn(false);
        when(special.limit()).thenReturn(Optional.empty());
        when(special.pageRequest()).thenReturn(Optional.empty());
        // sorts present but ignored because neither hasOnlySort nor limit/pageRequest
        when(special.sorts()).thenReturn(List.of(Sort.asc("ignored")));
        when(query.condition()).thenReturn(Optional.empty());

        DynamicQuery dynamic = new DynamicQuery(special, query);
        SelectQuery result = dynamic.get();

        assertEquals(query, result); // falls back to original query
    }

    @Test
    void shouldThrowOnNullArgsInFactory() {
        assertThatThrownBy(() -> DynamicQuery.of(null, query))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("args is required");
    }

    @Test
    void shouldThrowOnNullQueryInFactory() {
        assertThatThrownBy(() -> DynamicQuery.of(new Object[]{}, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("query is required");
    }
}