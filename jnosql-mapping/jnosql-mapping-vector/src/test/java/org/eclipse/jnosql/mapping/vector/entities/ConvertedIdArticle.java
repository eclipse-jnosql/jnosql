/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.vector.entities;

import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Column;
import jakarta.nosql.Convert;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import org.eclipse.jnosql.mapping.vector.DenseVector;

@Entity
public record ConvertedIdArticle(@Id @Convert(IdentifierConverter.class) Long id, @Column DenseVector features) {

    public static class IdentifierConverter implements AttributeConverter<Long, String> {

        @Override
        public String convertToDatabaseColumn(Long attribute) {
            return "stored:" + attribute;
        }

        @Override
        public Long convertToEntityAttribute(String value) {
            return Long.valueOf(value.substring("stored:".length()));
        }
    }
}
