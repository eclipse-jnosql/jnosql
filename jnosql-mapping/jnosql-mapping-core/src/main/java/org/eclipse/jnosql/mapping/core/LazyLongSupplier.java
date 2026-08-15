/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core;

import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * A thread-safe {@link LongSupplier} that evaluates its delegate at most once.
 * Both a successfully computed value and a runtime exception or error are cached.
 */
final class LazyLongSupplier implements LongSupplier {

    private final LongSupplier delegate;

    private volatile boolean loaded;

    private long value;

    private RuntimeException failure;

    private Error error;

    private LazyLongSupplier(LongSupplier delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate is required");
    }

    @Override
    public long getAsLong() {
        if (!loaded) {
            synchronized (this) {
                if (!loaded) {
                    try {
                        value = delegate.getAsLong();
                    } catch (RuntimeException exception) {
                        failure = exception;
                    } catch (Error error) {
                        this.error = error;
                    } finally {
                        loaded = true;
                    }
                }
            }
        }

        if (failure != null) {
            throw failure;
        }
        if (error != null) {
            throw error;
        }
        return value;
    }

    static LazyLongSupplier of(LongSupplier delegate) {
        return new LazyLongSupplier(delegate);
    }
}
