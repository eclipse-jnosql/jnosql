/*
 *  Copyright (c) 2017 Otávio Santana and others
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
package org.eclipse.jnosql.mapping.column;

import jakarta.nosql.TypeReference;
import jakarta.nosql.Value;
import jakarta.nosql.column.Column;
import jakarta.nosql.mapping.AttributeConverter;
import org.eclipse.jnosql.mapping.reflection.ClassMapping;
import org.eclipse.jnosql.mapping.reflection.FieldMapping;
import org.eclipse.jnosql.mapping.reflection.GenericFieldMapping;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.eclipse.jnosql.mapping.reflection.FieldType.COLLECTION;
import static org.eclipse.jnosql.mapping.reflection.FieldType.EMBEDDED;
import static org.eclipse.jnosql.mapping.reflection.FieldType.SUB_ENTITY;

class ColumnFieldConverters {

    static class ColumnFieldConverterFactory {

        private final EmbeddedFieldConverter embeddedFieldConverter = new EmbeddedFieldConverter();
        private final DefaultConverter defaultConverter = new DefaultConverter();
        private final CollectionEmbeddableConverter embeddableConverter = new CollectionEmbeddableConverter();
        private final SubEntityConverter subEntityConverter = new SubEntityConverter();

        ColumnFieldConverter get(FieldMapping field) {
            if (EMBEDDED.equals(field.getType())) {
                return embeddedFieldConverter;
            } else if (SUB_ENTITY.equals(field.getType())) {
                return subEntityConverter;
            } else if (isCollectionEmbeddable(field)) {
                return embeddableConverter;
            } else {
                return defaultConverter;
            }
        }

        private boolean isCollectionEmbeddable(FieldMapping field) {
            return COLLECTION.equals(field.getType()) && ((GenericFieldMapping) field).isEmbeddable();
        }
    }


    private static class SubEntityConverter implements ColumnFieldConverter {

        @Override
        public <X, Y, T> void convert(T instance, List<Column> columns, Column subColumn, FieldMapping field,
                                      AbstractColumnEntityConverter converter) {

            if (Objects.nonNull(subColumn)) {
                converterSubDocument(instance, subColumn, field, converter);
            } else {
                field.write(instance, converter.toEntity(field.getNativeField().getType(), columns));
            }
        }

        private <T> void converterSubDocument(T instance, Column subColumn, FieldMapping field,
                                              AbstractColumnEntityConverter converter) {
            Object value = subColumn.get();
            if (value instanceof Map) {
                Map map = (Map) value;
                List<Column> embeddedColumns = new ArrayList<>();

                for (Map.Entry entry : (Set<Map.Entry>) map.entrySet()) {
                    embeddedColumns.add(Column.of(entry.getKey().toString(), entry.getValue()));
                }
                field.write(instance, converter.toEntity(field.getNativeField().getType(), embeddedColumns));

            } else {
                field.write(instance, converter.toEntity(field.getNativeField().getType(),
                        subColumn.get(new TypeReference<List<Column>>() {
                        })));
            }
        }
    }

    private static class EmbeddedFieldConverter implements ColumnFieldConverter {

        @Override
        public <X, Y, T> void convert(T instance, List<Column> columns, Column column,
                                      FieldMapping field, AbstractColumnEntityConverter converter) {
            Field nativeField = field.getNativeField();
            Object subEntity = converter.toEntity(nativeField.getType(), columns);
            ClassMapping mapping = converter.getClassMappings().get(subEntity.getClass());
            boolean areAllFieldsNull = mapping.getFields()
                    .stream()
                    .map(f -> f.read(subEntity))
                    .allMatch(Objects::isNull);
            if (!areAllFieldsNull) {
                field.write(instance, subEntity);
            }
        }
    }


    private static class DefaultConverter implements ColumnFieldConverter {


        @Override
        public <X, Y, T> void convert(T instance, List<Column> columns, Column column,
                                      FieldMapping field, AbstractColumnEntityConverter converter) {
            if (Objects.nonNull(column)) {
                Value value = column.getValue();
                Optional<Class<? extends AttributeConverter<X, Y>>> optionalConverter = field.getConverter();
                if (optionalConverter.isPresent()) {
                    AttributeConverter<X, Y> attributeConverter = converter.getConverters().get(optionalConverter.get());
                    Object attributeConverted = attributeConverter.convertToEntityAttribute((Y) value.get());
                    field.write(instance, field.getValue(Value.of(attributeConverted)));
                } else {
                    field.write(instance, field.getValue(value));
                }
            }
        }
    }


    private static class CollectionEmbeddableConverter implements ColumnFieldConverter {

        @Override
        public <X, Y, T> void convert(T instance, List<Column> columns, Column column, FieldMapping field,
                                      AbstractColumnEntityConverter converter) {

            if (Objects.nonNull(column)) {
                GenericFieldMapping genericField = (GenericFieldMapping) field;
                Collection collection = genericField.getCollectionInstance();
                List<List<Column>> embeddable = (List<List<Column>>) column.get();
                for (List<Column> columnList : embeddable) {
                    Object element = converter.toEntity(genericField.getElementType(), columnList);
                    collection.add(element);
                }
                field.write(instance, collection);
            }
        }
    }
}
