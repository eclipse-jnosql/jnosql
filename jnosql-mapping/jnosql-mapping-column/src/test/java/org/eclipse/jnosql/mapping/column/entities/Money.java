/*
 *   Copyright (c) 2023 Contributors to the Eclipse Foundation
 *    All rights reserved. This program and the accompanying materials
 *    are made available under the terms of the Eclipse Public License v1.0
 *    and Apache License v2.0 which accompanies this distribution.
 *    The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *    and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *    You may elect to redistribute this code under either of these licenses.
 *
 *    Contributors:
 *
 *    Otavio Santana
 */
package org.eclipse.jnosql.mapping.column.entities;


import java.math.BigDecimal;

public record Money(String currency, BigDecimal value) {

    @Override
    public String toString() {
        return currency + " " + value.toString();
    }

    public static Money parse(String dbData) {
        String[] values = dbData.split(" ");
        String currency = values[0];
        BigDecimal value = BigDecimal.valueOf(Double.valueOf(values[1]));
        return new Money(currency, value);
    }
}

