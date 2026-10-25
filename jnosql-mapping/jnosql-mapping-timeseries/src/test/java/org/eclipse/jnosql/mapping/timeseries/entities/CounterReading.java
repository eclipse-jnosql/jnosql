/*
 *   Copyright (c) 2023 Contributors to the Eclipse Foundation
 *    All rights reserved. This program and the accompanying materials
 *    are made available under the terms of the Eclipse Public License 2.0
 *    and Apache License v2.0 which accompanies this distribution.
 *    The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *    and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *    You may elect to redistribute this code under either of these licenses.
 *
 *    Contributors:
 *
 *    Otavio Santana
 */
package org.eclipse.jnosql.mapping.timeseries.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;


@Entity
public class CounterReading {

    @Id
    private Long timestamp;

    @Column
    private String sensor;

    @Column
    private double temperature;

    public CounterReading() {
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getSensor() {
        return sensor;
    }

    public double getTemperature() {
        return temperature;
    }
}
