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

package org.eclipse.jnosql.mapping.reflection.entities.inheritance;

import jakarta.nosql.Column;
import jakarta.nosql.DiscriminatorValue;
import jakarta.nosql.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("Large")
public class LargeProject extends Project {

    @Column
    private BigDecimal budget;

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public BigDecimal getBudget() {
        return budget;
    }
}
