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

import jakarta.data.exceptions.MappingException;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.ConstructorBuilder;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.eclipse.jnosql.mapping.metadata.ParameterMetaData;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static org.eclipse.jnosql.mapping.metadata.MappingType.EMBEDDED;
import static org.eclipse.jnosql.mapping.metadata.MappingType.EMBEDDED_GROUP;
import static org.eclipse.jnosql.mapping.metadata.MappingType.ENTITY;


/**
 * This abstract class represents the converter between an entity and the {@link CommunicationEntity}.
 * Subclasses must implement methods to provide access to metadata about entities and converters.
 */
public abstract class EntityConverter {

    /**
     * Retrieves the metadata about entities.
     *
     * @return the metadata about entities
     */
    protected abstract EntitiesMetadata entities();

    /**
     * Retrieves the converters used for conversion.
     *
     * @return the converters used for conversion
     */
    protected abstract Converters converters();

    /**
     * Converts the provided entity instance to a {@link CommunicationEntity}.
     *
     * @param entity the entity instance to be converted
     * @return a {@link CommunicationEntity} instance representing the entity
     * @throws NullPointerException when the entity is null
     */
    public CommunicationEntity toCommunication(Object entity) {
        requireNonNull(entity, "entity is required");
        EntityMetadata mapping = entities().get(entity.getClass());
        CommunicationEntity communication = CommunicationEntity.of(mapping.name());
        mapping.fields().stream()
                .map(f -> to(f, entity))
                .map(f -> f.toElements(this, converters()))
                .flatMap(List::stream)
                .forEach(communication::add);

        mapping.inheritance().ifPresent(i -> communication.add(i.discriminatorColumn(),
                i.discriminatorValue()));
        return communication;
    }

    /**
     * Converts a {@link CommunicationEntity} to an entity of the specified type.
     *
     * @param type   the class of the entity to be converted to
     * @param entity the {@link CommunicationEntity} to be converted
     * @param <T>    the type of the entity
     * @return the entity instance
     * @throws NullPointerException when either the type or the entity are null
     */
    public <T> T toEntity(Class<T> type, CommunicationEntity entity) {
        requireNonNull(entity, "entity is required");
        requireNonNull(type, "type is required");
        return toEntity(type, entity.elements());
    }

    /**
     * Converts a {@link CommunicationEntity} to an entity, using the provided instance.
     * This method modifies the existing instance rather than creating a new one.
     *
     * @param type   the instance to be used for conversion
     * @param entity the {@link CommunicationEntity} to be converted
     * @param <T>    the type of the entity
     * @return the modified entity instance
     * @throws NullPointerException when either the type or the entity are null
     */
    @SuppressWarnings("unchecked")
    public <T> T toEntity(T type, CommunicationEntity entity) {
        requireNonNull(entity, "entity is required");
        requireNonNull(type, "type is required");

        if (type.getClass().isRecord()) {
            return (T) toEntity(type.getClass(), entity.elements());
        }
        EntityMetadata mapping = entities().get(type.getClass());
        return convertEntity(entity.elements(), mapping, type);
    }

    /**
     * Converts a {@link CommunicationEntity} to an entity, inferring the type from the entity's name.
     *
     * @param entity the {@link CommunicationEntity} to be converted
     * @param <T>    the type of the entity
     * @return the entity instance
     * @throws NullPointerException when the entity is null
     */
    public <T> T toEntity(CommunicationEntity entity) {
        requireNonNull(entity, "entity is required");
        EntityMetadata mapping = entities().findByName(entity.name());
        if (mapping.isInheritance()) {
            return mapInheritanceEntity(entity, mapping.type());
        }
        ConstructorMetadata constructor = mapping.constructor();
        if (constructor.isDefault()) {
            T instance = mapping.newInstance();
            return convertEntity(entity.elements(), mapping, instance);
        } else {
            return convertEntityByConstructor(entity.elements(), mapping);
        }
    }

    protected AttributeFieldValue to(FieldMetadata field, Object entity) {
        Object value = field.read(entity);
        return DefaultAttributeFieldValue.of(value, field);
    }

    protected <T> Consumer<String> feedObject(T entity, List<Element> elements, Map<String, FieldMetadata> fieldsGroupByName) {
        return (String k) -> {
            Optional<Element> element = elements.stream().filter(c -> c.name().equals(k))
                    .filter(e -> !e.value().isNull()).findFirst();
            FieldMetadata field = fieldsGroupByName.get(k);
            FieldConverter fieldConverter = FieldConverter.get(field);
            if (ENTITY.equals(field.mappingType())) {
                element.ifPresent(c -> fieldConverter.convert(entity, c, field, this));
            } else {
                fieldConverter.convert(entity, elements, element.orElse(null), field, this);
            }
        };
    }


    protected <T> T toEntity(Class<T> type, List<Element> elements) {
        EntityMetadata mapping = entities().get(type);
        if (mapping.isInheritance()) {
            return inheritanceToEntity(elements, mapping);
        }
        ConstructorMetadata constructor = mapping.constructor();
        if (constructor.isDefault()) {
            T instance = mapping.newInstance();
            return convertEntity(elements, mapping, instance);
        } else {
            return convertEntityByConstructor(elements, mapping);
        }
    }

    private <T> T convertEntityByConstructor(List<Element> elements, EntityMetadata mapping) {
        ConstructorBuilder builder = ConstructorBuilder.of(mapping.constructor());
        for (ParameterMetaData parameter : builder.parameters()) {
            Predicate<Element> matchName = c -> c.name().equals(parameter.name());
            boolean isFlatEmbedded = EMBEDDED.equals(parameter.mappingType());
            Optional<Element> element = elements.stream()
                    .filter(matchName.or(e -> isFlatEmbedded))
                    .findFirst();
            element.ifPresentOrElse(c -> {
                var converter = ParameterConverter.of(parameter, entities());
                if (isFlatEmbedded) {
                    converter.convert(this, Element.of("_", elements), parameter, builder);
                } else {
                    converter.convert(this, c, parameter, builder);
                }
            }, builder::addEmptyParameter);
        }
        return builder.build();
    }

    private <T> T convertEntity(List<Element> elements, EntityMetadata mapping, T instance) {
        final Map<String, FieldMetadata> fieldsGroupByName = mapping.fieldsGroupByName();
        final List<String> names = elements.stream().map(Element::name).sorted().toList();
        final Predicate<String> existField = k -> Collections.binarySearch(names, k) >= 0;
        final Predicate<String> isElementType = k -> {
            var type = fieldsGroupByName.get(k).mappingType();
            return EMBEDDED.equals(type)|| EMBEDDED_GROUP.equals(type) || ENTITY.equals(type);
        };
        fieldsGroupByName.keySet().stream()
                .filter(existField.or(isElementType))
                .forEach(feedObject(instance, elements, fieldsGroupByName));

        return instance;
    }

    private <T> T mapInheritanceEntity(CommunicationEntity entity, Class<?> type) {
        Map<String, InheritanceMetadata> group = entities()
                .findByParentGroupByDiscriminatorValue(type);

        if (group.isEmpty()) {
            throw new MappingException("There is no discriminator inheritance to the document collection "
                    + entity.name());
        }
        String column = group.values()
                .stream()
                .findFirst()
                .map(InheritanceMetadata::discriminatorColumn)
                .orElseThrow();

        String discriminator = entity.find(column, String.class)
                .orElseThrow(
                        () -> new MappingException("To inheritance there is the discriminator column missing" +
                                " on the Document Collection, the document name: " + column));

        InheritanceMetadata inheritance = Optional.ofNullable(group.get(discriminator))
                .orElseThrow(() -> new MappingException("There is no inheritance map to the discriminator" +
                        " column value " + discriminator));

        EntityMetadata mapping = entities().get(inheritance.entity());
        ConstructorMetadata constructor = mapping.constructor();
        if (constructor.isDefault()) {
            T instance = mapping.newInstance();
            return convertEntity(entity.elements(), mapping, instance);
        } else {
            return convertEntityByConstructor(entity.elements(), mapping);
        }
    }

    private <T> T inheritanceToEntity(List<Element> elements, EntityMetadata mapping) {
        Map<String, InheritanceMetadata> group = entities()
                .findByParentGroupByDiscriminatorValue(mapping.type());

        if (group.isEmpty()) {
            throw new MappingException("There is no discriminator inheritance to the structure "
                    + mapping.name());
        }

        String column = group.values()
                .stream()
                .findFirst()
                .map(InheritanceMetadata::discriminatorColumn)
                .orElseThrow();

        String discriminator = elements.stream()
                .filter(d -> d.name().equals(column))
                .findFirst()
                .map(d -> d.get(String.class))
                .orElseThrow(
                        () -> new MappingException("To inheritance there is the discriminator element missing" +
                                " at the structure, the field's name: " + column));

        InheritanceMetadata inheritance = Optional.ofNullable(group.get(discriminator))
                .orElseThrow(() -> new MappingException("There is no inheritance map to the discriminator" +
                        " column value " + discriminator));

        EntityMetadata inheritanceMetadata = entities().get(inheritance.entity());
        T instance = inheritanceMetadata.newInstance();
        return convertEntity(elements, inheritanceMetadata, instance);
    }
}
