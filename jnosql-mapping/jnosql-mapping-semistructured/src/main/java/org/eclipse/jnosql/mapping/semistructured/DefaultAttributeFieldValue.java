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

import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.ArrayFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.CollectionFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.DefaultFieldValue;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldValue;
import org.eclipse.jnosql.mapping.metadata.MapFieldMetadata;
import org.eclipse.jnosql.mapping.metadata.MappingType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.eclipse.jnosql.mapping.metadata.MappingType.ARRAY;
import static org.eclipse.jnosql.mapping.metadata.MappingType.COLLECTION;
import static org.eclipse.jnosql.mapping.metadata.MappingType.EMBEDDED;
import static org.eclipse.jnosql.mapping.metadata.MappingType.EMBEDDED_GROUP;
import static org.eclipse.jnosql.mapping.metadata.MappingType.ENTITY;
import static org.eclipse.jnosql.mapping.metadata.MappingType.MAP;

final class DefaultAttributeFieldValue implements AttributeFieldValue {

    private final FieldValue fieldValue;

    private DefaultAttributeFieldValue(FieldValue fieldValue) {
        this.fieldValue = fieldValue;
    }

    @Override
    public Object value() {
        return fieldValue.value();
    }

    @Override
    public FieldMetadata field() {
        return fieldValue.field();
    }

    @Override
    public boolean isNotEmpty() {
        return fieldValue.isNotEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <X, Y> List<Element> toElements(EntityConverter converter, Converters converters) {
        if (value() == null) {
            return singletonList(Element.of(name(), null));
        } else if (EMBEDDED.equals(type())) {
            return converter.toCommunication(value()).elements();
        } else if (ENTITY.equals(type())|| EMBEDDED_GROUP.equals(type())) {
            return singletonList(Element.of(name(), converter.toCommunication(value()).elements()));
        } else if (isEmbeddableCollection()) {
            return singletonList(Element.of(name(), columns(converter)));
        } else if (isEmbeddableArray()) {
            return singletonList(Element.of(name(), columnsToArray(converter)));
        } else if(ARRAY.equals(type())) {
            return singletonList(Element.of(name(), columnsToArray()));
        } else if(isEmbeddableMap()) {
            return convertMap(converter);
        }
        Optional<Class<AttributeConverter<Object, Object>>> optionalConverter = field().converter();
        if (optionalConverter.isPresent()) {
            AttributeConverter<X, Y> attributeConverter = converters.get(field());
            return singletonList(Element.of(name(), attributeConverter.convertToDatabaseColumn((X) value())));
        }
        return singletonList(Element.of(name(), value()));
    }

    private List<Element> convertMap(EntityConverter converter) {
        var map = (Map<?, ?>) value();
        var elements = new ArrayList<>();
        for (var key : map.keySet()) {
            var item = map.get(key);
            var element = Element.of(key.toString(), Value.of(converter.toCommunication(item).elements()));
            elements.add(element);
        }
        return singletonList(Element.of(name(), elements));
    }

    private List<List<Element>> columns(EntityConverter converter) {
        List<List<Element>> elements = new ArrayList<>();
        for (Object element : (Iterable<?>) value()) {
            elements.add(converter.toCommunication(element).elements());
        }
        return elements;
    }

    private Object columnsToArray() {
        if(value() instanceof Object[]) {
           var values = new ArrayList<>();
            Collections.addAll(values, (Object[]) value());
            return  values;
        }
        return value();
    }

    private List<List<Element>> columnsToArray(EntityConverter converter) {
        List<List<Element>> elements = new ArrayList<>();
        for (Object element : (Object[]) value()) {
            elements.add(converter.toCommunication(element).elements());
        }
        return elements;
    }

    private boolean isEmbeddableCollection() {
        return COLLECTION.equals(type()) && isEmbeddableElement() ;
    }

    private boolean isEmbeddableMap() {
        return MAP.equals(type()) && ((MapFieldMetadata) field()).isEmbeddable();
    }

    private boolean isEmbeddableArray() {
        return ARRAY.equals(type()) && isArrayEmbeddableElement();
    }

    private MappingType type() {
        return field().mappingType();
    }

    private String name() {
        return field().name();
    }

    private boolean isEmbeddableElement() {
        return ((CollectionFieldMetadata) field()).isEmbeddable();
    }

    private boolean isArrayEmbeddableElement() {
        return ((ArrayFieldMetadata) field()).isEmbeddable();
    }


    @Override
    public String toString() {
        return "ColumnFieldValue{" + "fieldValue=" + fieldValue +
                '}';
    }

    static AttributeFieldValue of(Object value, FieldMetadata field) {
        return new DefaultAttributeFieldValue(new DefaultFieldValue(value, field));
    }
}
