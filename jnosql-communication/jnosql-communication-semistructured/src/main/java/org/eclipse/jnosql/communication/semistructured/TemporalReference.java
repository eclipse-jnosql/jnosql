/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *  and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.communication.semistructured;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The reference date and time shared by every temporal expression of one query.
 * <p>
 * Once {@link #refresh()} is called, all the expressions of the query observe the same instant until the next
 * refresh, so a single execution never mixes two different moments. Before the first refresh the reference
 * is "live" and always reads the clock, which avoids stale values for queries without an execution lifecycle.
 * The clock is the application clock; the database server clock is never consulted.
 */
final class TemporalReference {

    private final Clock clock;

    private volatile LocalDateTime pinned;

    private TemporalReference(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    LocalDateTime get() {
        var current = pinned;
        return current != null ? current : LocalDateTime.now(clock);
    }

    LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    void refresh() {
        this.pinned = LocalDateTime.now(clock);
    }

    static TemporalReference live() {
        return new TemporalReference(Clock.systemDefaultZone());
    }

    static TemporalReference live(Clock clock) {
        return new TemporalReference(clock);
    }

    static TemporalReference pinned(Clock clock) {
        var reference = new TemporalReference(clock);
        reference.refresh();
        return reference;
    }
}
