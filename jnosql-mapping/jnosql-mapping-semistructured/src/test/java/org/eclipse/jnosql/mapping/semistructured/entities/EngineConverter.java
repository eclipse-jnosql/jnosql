/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.semistructured.entities;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.nosql.AttributeConverter;

import java.util.Map;
import java.util.logging.Logger;


public class EngineConverter implements AttributeConverter<Engine,Object> {

    private static final Logger LOGGER = Logger.getLogger(EngineConverter.class.getName());

    private static final Jsonb JSONB = JsonbBuilder.create();

    @Override
    public Object convertToDatabaseColumn(Engine attribute) {
        LOGGER.info("Converting to database column: " + attribute);
        if (attribute == null) {
            return null;
        }
        return JSONB.fromJson(JSONB.toJson(attribute), Map.class);
    }

    @Override
    public Engine convertToEntityAttribute(Object dbData) {

        if(dbData == null) {
            return null;
        }
        if(dbData instanceof Map) {
            return JSONB.fromJson(JSONB.toJson(dbData), Engine.class);
        }
        throw new IllegalArgumentException("The dbData is not a valid type: " + dbData.getClass());
    }
}