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
package org.eclipse.jnosql.mapping.metadata.repository.spi;

import jakarta.nosql.Template;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.MethodKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;

import java.util.Objects;


/**
 * Represents the invocation context for a repository operation, combining the resolved
 * {@link MethodKey} used to identify the method, the corresponding {@link RepositoryMethod}
 * metadata, the owning {@link RepositoryMetadata}, and the actual parameter values supplied
 * by the caller. This record provides a unified, reflection-agnostic view of a repository
 * method invocation, enabling both runtime and annotation-processor–generated implementations
 * to pass execution details to a {@code RepositoryOperationExecutor}.
 */
public record RepositoryInvocationContext(RepositoryMethod method,
                                          RepositoryMetadata metadata,
                                          EntityMetadata entityMetadata,
                                          Template template,
                                          Object[] parameters) {

    public RepositoryInvocationContext {
        Objects.requireNonNull(method, "method is required");
        Objects.requireNonNull(metadata, "metadata is required");
        Objects.requireNonNull(entityMetadata, "entityMetadata is required");
        Objects.requireNonNull(template, "template is required");
        Objects.requireNonNull(parameters, "parameters is required");
    }

    @SuppressWarnings("unchecked")
    public <T extends Template> T castTemplate() {
        return (T) template;
    }
}
