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

import java.util.function.LongSupplier;


/**
 * A {@link LongSupplier} implementation that lazily computes a {@code long}
 * value once and caches the result for subsequent accesses.
 *
 * <p>The wrapped supplier is only invoked during the first call to
 * {@link #getAsLong()}. After the value is computed, the cached value is
 * returned for all future invocations.</p>
 *
 * <p>This implementation is thread-safe and guarantees that the delegate
 * supplier is executed at most once.</p>
 *
 * <p>This utility is useful for expensive operations such as database
 * aggregation queries, count operations, or remote service calls that should
 * be deferred until explicitly needed.</p>
 *
 * <pre>{@code
 * LongSupplier supplier = new LazyLongSupplier(
 *         () -> repository.count()
 * );
 *
 * // Count query executes here
 * long total = supplier.getAsLong();
 *
 * // Cached value reused
 * long cached = supplier.getAsLong();
 * }</pre>
 */
final class LazyLongSupplier implements LongSupplier {

    private final LongSupplier delegate;

    private volatile boolean loaded;

    private long value;

    private RuntimeException failure;

    private LazyLongSupplier(LongSupplier delegate) {
        this.delegate = delegate;
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
                    } finally {
                        loaded = true;
                    }
                }
            }
        }

        if (failure != null) {
            throw failure;
        }

        return value;
    }

    static LazyLongSupplier of(LongSupplier delegate) {
        return new LazyLongSupplier(delegate);
    }
}