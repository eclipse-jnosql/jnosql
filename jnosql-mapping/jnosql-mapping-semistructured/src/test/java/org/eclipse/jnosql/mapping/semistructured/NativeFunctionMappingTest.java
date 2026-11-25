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
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
@DisplayName("Native functions with entity field mapping")
class NativeFunctionMappingTest {

    @Inject
    private EntitiesMetadata mappings;

    private CriteriaCondition condition(String query, String name, Object value) {
        var parsed = org.eclipse.jnosql.communication.query.data.SelectProvider.INSTANCE.apply(query, null);
        var params = new SelectQueryParser().apply(parsed, new MapperObserver(mappings));
        if (name != null) {
            params.params().bind(name, value);
        }
        return params.query().condition().orElseThrow();
    }

    @Test
    @DisplayName("should map an explicitly named attribute inside a native function")
    void shouldMapExplicitColumn() {
        var condition = condition("FROM Worker WHERE ABS(salary) > 100", null, null);
        var function = ((CriteriaOperand.Function) condition.leftOperand()).function();
        assertThat(function.arguments()).containsExactly(new FunctionArgument.AttributePath("money"));
    }

    @Test
    @DisplayName("should keep default field names and map the right operand function")
    void shouldMapDefaultAndRight() {
        var condition = condition("FROM Worker WHERE name = UPPER(salary)", null, null);
        assertThat(condition.leftOperand()).isEqualTo(new CriteriaOperand.Attribute("name"));
        var function = ((CriteriaOperand.Function) condition.rightOperand()).function();
        assertThat(function.arguments()).containsExactly(new FunctionArgument.AttributePath("money"));
    }

    @Test
    @DisplayName("should not rename literals or parameters that match an attribute name")
    void shouldNotMapLiteralsOrParameters() {
        var condition = condition("FROM Worker WHERE LEFT(salary, 5) = :value", "value", "salary");
        var function = ((CriteriaOperand.Function) condition.leftOperand()).function();
        assertThat(function.arguments().get(0)).isEqualTo(new FunctionArgument.AttributePath("money"));
        assertThat(function.arguments().get(1)).isEqualTo(new FunctionArgument.Literal(5));
        assertThat(((CriteriaOperand.Constant) condition.rightOperand()).value().get()).isEqualTo("salary");

        var literal = condition("FROM Worker WHERE UPPER(salary) = UPPER('salary')", null, null);
        assertThat(((CriteriaOperand.Constant) literal.rightOperand()).value().get()).isEqualTo("SALARY");
    }

    @Test
    @DisplayName("should map attributes of nested native functions")
    void shouldMapNested() {
        var condition = condition("FROM Worker WHERE UPPER(LEFT(salary, LENGTH(:value))) = 'AB'", "value", "ab");
        var outer = ((CriteriaOperand.Function) condition.leftOperand()).function();
        var inner = ((FunctionArgument.NativeArgument) outer.arguments().getFirst()).function();
        assertThat(inner.arguments().getFirst()).isEqualTo(new FunctionArgument.AttributePath("money"));
        assertThat(((FunctionArgument.ValueArgument) inner.arguments().get(1)).value().get()).isEqualTo(2);
    }
}
