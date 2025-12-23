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
package org.eclipse.jnosql.mapping.core.repository.operations;

import jakarta.data.restrict.Restriction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.Arrays;

/**
 * Default implementation of the {@link DeleteOperation} used by the core
 * execution engine.
 *
 * <p>This operation executes a repository {@code delete} method whose
 * signature accepts exactly one argument and returns {@code void}.
 *
 * <p>Delete-by-restriction is not supported by default. Providers that support
 * restriction-based deletes must override {@link #deleteByRestriction}.</p>
 *
 * <p>If the repository method declares an unsupported return type or an
 * invalid number of parameters, an {@link IllegalArgumentException} is thrown.</p>
 */
@ApplicationScoped
@Typed(CoreDeleteOperation.class)
public class CoreDeleteOperation implements DeleteOperation {

    @Override
    public <T> T execute(RepositoryInvocationContext context) {
        var parameters = context.parameters();
        var returnType = context.method().returnType().orElse(void.class);
        var template = context.template();
        if (parameters.length != 1) {
            throw new IllegalArgumentException("Delete operation requires one parameter instead of: "
                    + Arrays.asList(parameters));
        }
        if (isNotVoidReturn(returnType)) {
            throw new IllegalArgumentException("Delete operation doesn't support return type: " + returnType +
                    " it supports void as return");
        }
        var entity = parameters[0];
        if (entity instanceof Restriction<?> restriction) {
            deleteByRestriction(context, restriction);
        } else if (entity instanceof Iterable<?> entities) {
            template.delete(entities);
        } else if (entity.getClass().isArray()) {
            template.delete(Arrays.asList((Object[]) entity));
        } else {
            template.delete(entity);
        }
        return null;
    }

    /**
     * Executes a delete operation based on a {@link Restriction}.
     *
     * <p>This method is not supported by the core engine and must be overridden
     * by provider-specific implementations that support restriction-based
     * deletion.</p>
     *
     * @param context the repository invocation context
     * @param restriction the deletion restriction
     * @throws UnsupportedOperationException if not overridden by a provider
     */
    protected void deleteByRestriction(RepositoryInvocationContext context, Restriction<?> restriction) {
        throw  new UnsupportedOperationException("Delete by restriction is not supported by default");
    }

    private boolean isNotVoidReturn(Class<?> returnType) {
        return !returnType.equals(void.class);
    }
}
