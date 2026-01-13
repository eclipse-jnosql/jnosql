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
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.data.repository.By;
import org.eclipse.jnosql.mapping.metadata.ClassInformationNotFoundException;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

enum SelectFieldMapper {

    INSTANCE;

    <T> Function<T, T> map(MapperObserver observer, EntitiesMetadata entitiesMetadata) {
        if (observer.fields().isEmpty()) {
            return Function.identity();
        }
        List<String> fields = observer.fields();
        EntityMetadata metadata = entitiesMetadata.findByName(observer.entity());
        if (fields.size() == 1) {
            var field = fields.getFirst();
            return entity -> field(entity, metadata, field);
        } else {
            return entity -> fields(entity, fields, metadata);
        }
    }


    @SuppressWarnings("unchecked")
    private <T> T fields(T entity, List<String> fields, EntityMetadata metadata) {
        List<Object> values = new ArrayList<>();
        for (String field : fields) {
            values.add(field(entity, metadata, field));
        }
        return (T) values.toArray();
    }

    @SuppressWarnings("unchecked")
    <T, E> E field(T entity, EntityMetadata metadata, String field) {
        if(By.ID.equals(field)) {
            var id = metadata.id().orElseThrow(() -> new ClassInformationNotFoundException(
                    "The entity " + metadata.name() + " does not have id mapping"));
            return (E) id.read(entity);
        }
        var fieldMetadata = metadata.fieldMapping(field).orElseThrow();
        var value = fieldMetadata.read(entity);
        return (E) value;
    }
}
