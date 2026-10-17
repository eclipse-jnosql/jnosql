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
package org.eclipse.jnosql.mapping.vector;

/**
 * Represents a vector value used by a vector database.
 * <p>
 * A regular Jakarta NoSQL entity can be persisted in a vector database by declaring
 * one persisted attribute whose type implements {@code Vector}. The attribute must be
 * annotated with {@code @Column}.
 * </p>
 *
 * <pre>{@code
 * @Entity
 * public class Article {
 *
 *     @Id
 *     private String id;
 *
 *     @Column
 *     private String content;
 *
 *     @Column
 *     private String author;
 *
 *     @Column
 *     private int year;
 *
 *     @Column
 *     private Vector embedding;
 * }
 * }</pre>
 *
 * <p>
 * The vector attribute does not require a predefined property name such as
 * {@code embedding}; it is identified by the {@code Vector} type hierarchy.
 * </p>
 *
 * <p>
 * Eclipse JNoSQL provides different vector representations:
 * </p>
 *
 * <ul>
 *     <li>{@link DenseVector} represents a vector where every dimension has an explicit value.</li>
 *     <li>{@link SparseVector} represents a vector where only selected dimensions are stored explicitly.</li>
 *     <li>{@link MultiVector} represents a value using multiple dense vectors.</li>
 * </ul>
 *
 * <p>
 * The appropriate representation depends on the vector model and the capabilities
 * supported by the database provider.
 * </p>
 *
 * <p>
 * Vector generation is outside the scope of Eclipse JNoSQL. Providers are responsible
 * for converting vector values to their database-native representations.
 * </p>
 */
public interface Vector {
}
