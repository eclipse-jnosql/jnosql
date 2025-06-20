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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.data.expression.Expression;
import jakarta.data.metamodel.BasicAttribute;
import jakarta.data.spi.expression.literal.Literal;
import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;

import java.util.function.Supplier;

enum ValueConverter {

INSTANCE;

    static Object of(Supplier<Expression<?, ?>> supplier, BasicAttribute<?, ?> basicAttribute,
                              Converters converters,
                                     Class<AttributeConverter<Object, Object>> converter,
                              FieldMetadata fieldMetadata) {
        var expression = supplier.get();
        var literal = getLiteral(expression);
        return getValue(basicAttribute, converters, literal, converter, fieldMetadata);
    }

    private static Literal<?> getLiteral(Expression<?, ?> expression) {
        if(expression instanceof Literal<?> literal) {
            return literal;
        } else {
            throw new UnsupportedOperationException("Currently only Literal values are supported for EqualTo constraints, but got: " + expression);
        }
    }

    private static Object getValue(BasicAttribute<?, ?> basicAttribute, Converters converters,
                                   Literal<?> literal,
                                   Class<AttributeConverter<Object, Object>> converter,
                                   FieldMetadata fieldMetadata) {
        if (converter != null) {
            var attributeConverter = converters.get(fieldMetadata);
            return attributeConverter.convertToDatabaseColumn(literal.value());
        } else {
            return Value.of(literal.value()).get(basicAttribute.attributeType());
        }
    }
}
