/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.function.FunctionArgument;
import org.eclipse.jnosql.communication.query.data.SelectProvider;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.CriteriaOperand;
import org.eclipse.jnosql.communication.semistructured.SelectQueryParser;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
@DisplayName("Mapper observer with native functions")
class MapperObserverNativeFunctionTest {

    @Inject
    private EntitiesMetadata mappings;

    private CriteriaCondition condition(String query, String name, Object value) {
        var parsed = SelectProvider.INSTANCE.apply(query, null);
        var params = new SelectQueryParser().apply(parsed, new MapperObserver(mappings));
        if (name != null) {
            params.params().bind(name, value);
        }
        return params.query().condition().orElseThrow();
    }

    @Nested
    @DisplayName("When mapping the attributes of a native function")
    class WhenTheFieldMapping {

        @Test
        @DisplayName("Should map an explicitly named column")
        void shouldMapExplicitColumn() {

            // When
            var condition = condition("FROM Worker WHERE ABS(salary) > 100", null, null);

            // Then
            var function = ((CriteriaOperand.Function) condition.leftOperand()).function();
            assertSoftly(softly -> softly.assertThat(function.arguments()).as("arguments")
                    .containsExactly(new FunctionArgument.AttributePath("money")));
        }

        @Test
        @DisplayName("Should keep the default field name and map the right operand function")
        void shouldMapDefaultAndRight() {

            // When
            var condition = condition("FROM Worker WHERE name = UPPER(salary)", null, null);

            // Then
            var function = ((CriteriaOperand.Function) condition.rightOperand()).function();
            assertSoftly(softly -> {
                softly.assertThat(condition.leftOperand()).as("left")
                        .isEqualTo(new CriteriaOperand.Attribute("name"));
                softly.assertThat(function.arguments()).as("right")
                        .containsExactly(new FunctionArgument.AttributePath("money"));
            });
        }

        @Test
        @DisplayName("Should not rename a parameter whose value matches an attribute name")
        void shouldNotMapParameter() {

            // When
            var condition = condition("FROM Worker WHERE LEFT(salary, 5) = :value", "value", "salary");

            // Then
            var function = ((CriteriaOperand.Function) condition.leftOperand()).function();
            assertSoftly(softly -> {
                softly.assertThat(function.arguments().get(0)).as("attribute")
                        .isEqualTo(new FunctionArgument.AttributePath("money"));
                softly.assertThat(function.arguments().get(1)).as("literal")
                        .isEqualTo(new FunctionArgument.Literal(5));
                softly.assertThat(((CriteriaOperand.Constant) condition.rightOperand()).value().get())
                        .as("parameter").isEqualTo("salary");
            });
        }

        @Test
        @DisplayName("Should not rename a literal that matches an attribute name")
        void shouldNotMapLiteral() {

            // When
            var condition = condition("FROM Worker WHERE UPPER(salary) = UPPER('salary')", null, null);

            // Then
            assertSoftly(softly -> softly.assertThat(
                    ((CriteriaOperand.Constant) condition.rightOperand()).value().get())
                    .as("literal").isEqualTo("SALARY"));
        }

        @Test
        @DisplayName("Should map the attribute of a nested native function")
        void shouldMapNested() {

            // When
            var condition = condition("FROM Worker WHERE UPPER(LEFT(salary, LENGTH(:value))) = 'AB'", "value", "ab");

            // Then
            var outer = ((CriteriaOperand.Function) condition.leftOperand()).function();
            var inner = ((FunctionArgument.NativeArgument) outer.arguments().get(0)).function();
            assertSoftly(softly -> {
                softly.assertThat(inner.arguments().get(0)).as("attribute")
                        .isEqualTo(new FunctionArgument.AttributePath("money"));
                softly.assertThat(((FunctionArgument.ValueArgument) inner.arguments().get(1)).value().get())
                        .as("length").isEqualTo(2);
            });
        }
    }
}
