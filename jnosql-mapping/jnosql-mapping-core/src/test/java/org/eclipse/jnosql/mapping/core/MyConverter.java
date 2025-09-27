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
package org.eclipse.jnosql.mapping.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.nosql.AttributeConverter;

@ApplicationScoped
public class MyConverter implements AttributeConverter<String, String> {

    @Inject
    private RandomText text;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return text.get();
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return text.get();
    }
}
