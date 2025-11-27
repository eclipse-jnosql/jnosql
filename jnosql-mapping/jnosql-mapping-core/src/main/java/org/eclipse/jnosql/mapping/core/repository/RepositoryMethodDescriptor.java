/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.repository;

import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethodType;

import java.util.Objects;

/**
 * Describes a repository method by combining its resolved
 * {@link RepositoryMethodType} with the corresponding
 * {@link RepositoryMethod} metadata when available.
 * This descriptor is the outcome of method analysis performed during
 * repository initialization. It classifies a Java {@code Method} into
 * one of the supported semantic types and, when applicable, provides
 * the parsed metadata used for query execution. Methods such as those
 * inherited from {@code Object} may not expose metadata but still
 * require classification, and this descriptor represents both cases.
 *
 * @param type   the resolved semantic type of the repository method
 * @param method the parsed repository method metadata, or {@code null}
 *               when the method does not provide Jakarta Data metadata
 *
 */
public record RepositoryMethodDescriptor(RepositoryMethodType type, RepositoryMethod method) {
    public RepositoryMethodDescriptor {
        Objects.requireNonNull(type, "type is required");
    }
}