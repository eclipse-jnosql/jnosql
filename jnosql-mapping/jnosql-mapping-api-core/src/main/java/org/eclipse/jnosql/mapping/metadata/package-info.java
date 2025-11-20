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

/**
 * The org.eclipse.jnosql.mapping.metadata package provides APIs for generating and reading metadata
 * through Java annotations. It contains information about Entities, which are classes annotated with
 * the {@link jakarta.nosql.Entity @Entity} annotation, their fields and the * respective annotations
 * applied to those fields. The metadata in this package can be used to facilitate mapping between Java objects
 * and NoSQL databases * in a type-safe manner. Developers can use the metadata to extract information about entities'
 * structure and * relationships and their fields at runtime without direct reflection.
 * The implementation of this package offers multiple solutions to achieve metadata generation and reading.
 * Developers can use reflection or other techniques like Java annotation processors to
 * work with the metadata.
 * This design allows flexibility and can cater to different preferences and requirements in various projects.
 *
 * @since 1.0.1
 * @see jakarta.nosql.Entity
 **/
package org.eclipse.jnosql.mapping.metadata;