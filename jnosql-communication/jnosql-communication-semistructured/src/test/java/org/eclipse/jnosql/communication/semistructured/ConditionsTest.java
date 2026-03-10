/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */
package org.eclipse.jnosql.communication.semistructured;

import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.query.ArrayQueryValue;
import org.eclipse.jnosql.communication.query.NumberQueryValue;
import org.eclipse.jnosql.communication.query.QueryCondition;
import org.eclipse.jnosql.communication.query.QueryValue;
import org.eclipse.jnosql.communication.query.StringQueryValue;
import org.eclipse.jnosql.communication.query.data.DefaultQueryCondition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("Conditions Tests")
class ConditionsTest {

    private final Params params = Params.newParams();

    private final CommunicationObserverParser observer = mock(CommunicationObserverParser.class);

    private final String entity = "Person";

    private QueryCondition condition(String field, Condition operator, String value) {
        return new DefaultQueryCondition(field, operator, StringQueryValue.of(value));
    }

    @Nested
    @DisplayName("When processing comparison operators")
    class ComparisonOperators {

        @Test
        @DisplayName("Should translate EQUALS condition")
        void shouldTranslateEquals() {

            QueryCondition query = condition("name", Condition.EQUALS, "Ada");

            when(observer.fireConditionField(entity, "name")).thenReturn("name");

            CriteriaCondition result =
                    Conditions.getCondition(query, params, observer, entity);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should translate GREATER_THAN condition")
        void shouldTranslateGreaterThan() {

            QueryCondition query = condition("age", Condition.GREATER_THAN, "30");

            when(observer.fireConditionField(entity, "age")).thenReturn("age");

            CriteriaCondition result =
                    Conditions.getCondition(query, params, observer, entity);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should translate LESSER_THAN condition")
        void shouldTranslateLesserThan() {

            QueryCondition query = condition("age", Condition.LESSER_THAN, "40");

            when(observer.fireConditionField(entity, "age")).thenReturn("age");

            CriteriaCondition result =
                    Conditions.getCondition(query, params, observer, entity);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should translate BETWEEN condition")
        void shouldTranslateBetween() {

            ArrayQueryValue arrayQueryValue = () -> new QueryValue[]{NumberQueryValue.of(20), NumberQueryValue.of(40)};
            QueryCondition query = new DefaultQueryCondition("age", Condition.BETWEEN, arrayQueryValue);
            when(observer.fireConditionField(entity, "age")).thenReturn("age");

            CriteriaCondition result =
                    Conditions.getCondition(query, params, observer, entity);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("When processing string operators")
    class StringOperators {

        @Test
        @DisplayName("Should translate LIKE condition")
        void shouldTranslateLike() {

            QueryCondition query = condition("name", Condition.LIKE, "%Ada%");

            when(observer.fireConditionField(entity, "name")).thenReturn("name");

            CriteriaCondition result =
                    Conditions.getCondition(query, params, observer, entity);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should translate CONTAINS condition")
        void shouldTranslateContains() {

            QueryCondition query = condition("name", Condition.CONTAINS, "Ada");

            when(observer.fireConditionField(entity, "name")).thenReturn("name");

            CriteriaCondition result =
                    Conditions.getCondition(query, params, observer, entity);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should translate STARTS_WITH condition")
        void shouldTranslateStartsWith() {

            QueryCondition query = condition("name", Condition.STARTS_WITH, "Ada");

            when(observer.fireConditionField(entity, "name")).thenReturn("name");

            CriteriaCondition result =
                    Conditions.getCondition(query, params, observer, entity);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should translate ENDS_WITH condition")
        void shouldTranslateEndsWith() {

            QueryCondition query = condition("name", Condition.ENDS_WITH, "Ada");

            when(observer.fireConditionField(entity, "name")).thenReturn("name");

            CriteriaCondition result =
                    Conditions.getCondition(query, params, observer, entity);

            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("When resolving the condition field name")
    class ObserverResolution {

        @Test
        @DisplayName("Should delegate field resolution to observer")
        void shouldDelegateFieldResolution() {

            QueryCondition query = condition("name", Condition.EQUALS, "Otavio");

            when(observer.fireConditionField(entity, "name"))
                    .thenReturn("person_name");

            Conditions.getCondition(query, params, observer, entity);

            verify(observer).fireConditionField(entity, "name");
        }
    }
}