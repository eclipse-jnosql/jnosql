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
package org.eclipse.jnosql.mapping.core.repository.returns;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * Looks up executors from CDI and JNDI. The Jakarta Concurrency API is optional: its absence is reported
 * as an unavailable source rather than as an error.
 */
enum ContainerExecutorLookup implements ExecutorLookup {

    INSTANCE;

    static final String DEFAULT_MANAGED_EXECUTOR = "java:comp/DefaultManagedExecutorService";

    @Override
    public Optional<Executor> managedFromCdi() {
        try {
            return ManagedExecutorLookup.fromCdi();
        } catch (LinkageError e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Executor> managedFromJndi() {
        return jndi(DEFAULT_MANAGED_EXECUTOR);
    }

    @Override
    public Optional<Executor> named(String name) {
        if (name.startsWith("java:")) {
            return jndi(name);
        }
        Instance<Executor> instance;
        try {
            instance = CDI.current().select(Executor.class, NamedLiteral.of(name));
        } catch (IllegalStateException e) {
            return Optional.empty();
        }
        if (instance.isAmbiguous()) {
            throw new IllegalStateException("More than one executor bean is named '" + name + "'");
        }
        return instance.isResolvable() ? Optional.of(instance.get()) : Optional.empty();
    }

    private Optional<Executor> jndi(String name) {
        Object resource;
        try {
            var context = new InitialContext();
            try {
                resource = context.lookup(name);
            } finally {
                context.close();
            }
        } catch (NamingException e) {
            return Optional.empty();
        }
        if (resource instanceof Executor executor) {
            return Optional.of(executor);
        }
        throw new IllegalStateException("The resource '" + name + "' is not a " + Executor.class.getName());
    }
}
