/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.keyvalue;


import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

/**
 * Factory of {@link BucketManager} instances, created by a {@link KeyValueConfiguration}.
 * It is a {@link Function} from a bucket name to its {@link BucketManager}, and it also creates the
 * {@link List}, {@link Set}, {@link Queue} and {@link Map} data structures that some databases provide.
 * When the application has finished using the factory, and/or at application shutdown, it should close it.
 *
 * <pre>{@code
 * BucketManager bucket = factory.apply("people");
 * }</pre>
 */
public interface BucketManagerFactory extends Function<String, BucketManager>, AutoCloseable {

    /**
     * Creates a {@link List} from bucket name
     *
     * @param bucketName a bucket name
     * @param type      the value class
     * @param <T>        the value type
     * @return a {@link List} instance
     * @throws UnsupportedOperationException when the database does not have to it
     * @throws NullPointerException          when either bucketName or class are null
     */
    <T> List<T> getList(String bucketName, Class<T> type);

    /**
     * Creates a {@link Set} from bucket name
     *
     * @param bucketName a bucket name
     * @param type      the value class
     * @param <T>        the value type
     * @return a {@link Set} instance
     * @throws UnsupportedOperationException when the database does not have to it
     * @throws NullPointerException          when either bucketName or class are null
     */
    <T> Set<T> getSet(String bucketName, Class<T> type);

    /**
     * Creates a {@link Queue} from bucket name
     *
     * @param bucketName a bucket name
     * @param type      the value class
     * @param <T>        the value type
     * @return a {@link Queue} instance
     * @throws UnsupportedOperationException when the database does not have to it
     * @throws NullPointerException          when either bucketName or class are null
     */
    <T> Queue<T> getQueue(String bucketName, Class<T> type);

    /**
     * Creates a {@link  Map} from bucket name
     *
     * @param bucketName the bucket name
     * @param keyValue   the key class
     * @param valueValue the value class
     * @param <K>        the key type
     * @param <V>        the value type
     * @return a {@link Map} instance
     * @throws UnsupportedOperationException when the database does not have to it
     * @throws NullPointerException          when either bucketName or class are null
     */
    <K, V> Map<K, V> getMap(String bucketName, Class<K> keyValue, Class<V> valueValue);

    /**
     * closes a resource
     */
    @Override
    void close();
}
