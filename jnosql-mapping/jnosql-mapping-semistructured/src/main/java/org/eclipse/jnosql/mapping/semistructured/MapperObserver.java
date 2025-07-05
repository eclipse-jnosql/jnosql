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
package org.eclipse.jnosql.mapping.semistructured;

import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.mapping.metadata.ClassInformationNotFoundException;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class MapperObserver implements CommunicationObserverParser {


    private final EntitiesMetadata mappings;

    private final List<String> fields = new ArrayList<>();

    private String entity;

    MapperObserver(EntitiesMetadata mappings) {
        this.mappings = mappings;
    }

    @Override
    public String fireEntity(String entity) {
        Optional<EntityMetadata> mapping = getEntityMetadata(entity);
        return mapping.map(EntityMetadata::name).orElse(entity);
    }

    @Override
    public String fireSelectField(String entity, String field) {
        this.fields.add(field);
        return mapField(entity, field);
    }

    @Override
    public String fireSortProperty(String entity, String field) {
        return mapField(entity, field);
    }

    @Override
    public String fireConditionField(String entity, String field) {
        return mapField(entity, field);
    }

    List<String> fields() {
        return fields;
    }

    String entity() {
        return entity;
    }

    private String mapField(String entity, String field) {
        Optional<EntityMetadata> mapping = getEntityMetadata(entity);
        return mapping.map(c -> c.columnField(field)).orElse(field);
    }

    private Optional<EntityMetadata> getEntityMetadata(String entity) {
        try {
            this.entity = entity;
            return Optional.of(this.mappings.findByName(entity));
        } catch (ClassInformationNotFoundException e) {
            return this.mappings.findBySimpleName(entity)
                    .or(() -> this.mappings.findByClassName(entity));
        }
    }

}
