/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.keyvalue.configuration;


import jakarta.data.exceptions.MappingException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.keyvalue.BucketManagerFactory;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueDatabase;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@ApplicationScoped
/**
 * Supplies key-value bucket names from mapping configuration.
 */
public class CollectionSupplier {


    @Inject
    private BucketManagerFactory factory;


    /**
     * Produces a list from key-value configuration.
     *
     * @param injectionPoint the injection point
     * @param <T> the list element type
     * @return the configured list
     */
    @Produces
    @KeyValueDatabase("")
    public <T> List<T> getList(InjectionPoint injectionPoint) {
         CollectionElement<T> element = CollectionElement.of(injectionPoint);
         return factory.getList(element.bucketName, element.type);
    }

    /**
     * Produces a queue from key-value configuration.
     *
     * @param injectionPoint the injection point
     * @param <T> the queue element type
     * @return the configured queue
     */
    @Produces
    @KeyValueDatabase("")
    public <T> Queue<T> getQueue(InjectionPoint injectionPoint) {
        CollectionElement<T> element = CollectionElement.of(injectionPoint);
        return factory.getQueue(element.bucketName, element.type);
    }

    /**
     * Produces a set from key-value configuration.
     *
     * @param injectionPoint the injection point
     * @param <T> the set element type
     * @return the configured set
     */
    @Produces
    @KeyValueDatabase("")
    public <T> Set<T> getSet(InjectionPoint injectionPoint) {
        CollectionElement<T> element = CollectionElement.of(injectionPoint);
        return factory.getSet(element.bucketName, element.type);
    }

    /**
     * Produces a map from key-value configuration.
     *
     * @param injectionPoint the injection point
     * @param <K> the map key type
     * @param <V> the map value type
     * @return the configured map
     */
    @Produces
    @KeyValueDatabase("")
    public <K, V> Map<K, V> getMap(InjectionPoint injectionPoint) {
        MapElement<K, V> element = MapElement.of(injectionPoint);
        return factory.getMap(element.bucketName, element.key, element.value);
    }


    private record CollectionElement<T>(String bucketName, Class<T> type) {

        @SuppressWarnings("unchecked")
        private static <T> CollectionElement<T> of(InjectionPoint injectionPoint) {

            KeyValueDatabase keyValue = injectionPoint.getQualifiers()
                    .stream().filter(KeyValueDatabase.class::isInstance)
                    .map(KeyValueDatabase.class::cast)
                    .findFirst().orElseThrow(() -> new MappingException("There is an issue to load " +
                            "a Collection from the database"));
            String bucketName = keyValue.value();
            if (injectionPoint.getType() instanceof ParameterizedType param) {
                Type argument = param.getActualTypeArguments()[0];
                return new CollectionElement<>(bucketName, (Class<T>) argument);
            }
            throw new MappingException("There is an issue to load a Collection from the database");
        }
    }

    private record MapElement<K, V>(String bucketName, Class<K> key, Class<V> value) {

        @SuppressWarnings("unchecked")
        private static <K, V> MapElement<K, V> of(InjectionPoint injectionPoint) {
            KeyValueDatabase keyValue = injectionPoint.getQualifiers()
                    .stream().filter(KeyValueDatabase.class::isInstance)
                    .map(KeyValueDatabase.class::cast)
                    .findFirst().orElseThrow(() -> new MappingException("There is an issue to load " +
                            "a Collection from the database"));
            String bucketName = keyValue.value();
            if (injectionPoint.getType() instanceof ParameterizedType param) {
                Type key = param.getActualTypeArguments()[0];
                Type value = param.getActualTypeArguments()[1];
                return new MapElement<>(bucketName, (Class<K>) key, (Class<V>) value);
            }
            throw new MappingException("There is an issue to load the Map from the database");
        }
    }
}
