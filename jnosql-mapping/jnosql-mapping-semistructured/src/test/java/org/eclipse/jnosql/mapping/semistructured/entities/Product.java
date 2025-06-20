/*
 *  Copyright (c) 2025 Otávio Santana and others
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
package org.eclipse.jnosql.mapping.semistructured.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Convert;
import jakarta.nosql.Entity;

import java.math.BigDecimal;

@Entity
public class Product {

    @Column
    private String name;

    @Column
    private BigDecimal price;

    @Column
    private ProductType type;

    @Column
    @Convert(MoneyConverter.class)
    private Money amount;

    public enum ProductType {
        ELECTRONICS, CLOTHING, FOOD, FURNITURE
    }
}