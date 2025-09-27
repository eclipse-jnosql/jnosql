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
package org.eclipse.jnosql.mapping.reflection.entities.constructor;

import jakarta.nosql.Column;
import jakarta.nosql.Convert;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import org.eclipse.jnosql.mapping.reflection.entities.Money;
import org.eclipse.jnosql.mapping.reflection.entities.MoneyConverter;


@Entity
public class Computer {

    @Id
    private final Long id;

    @Column
    private final String name;

    @Column
    private final int age;

    @Column
    private final String model;

    @Column
    @Convert(MoneyConverter.class)
    private final Money price;

    public Computer(@Id Long id, @Column("name") String name,
                    @Column("age") int age, @Column("model") String model,
                    @Column("price") @Convert(MoneyConverter.class) Money price) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.model = model;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getModel() {
        return model;
    }

    public Money getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Computer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", model='" + model + '\'' +
                ", price=" + price +
                '}';
    }
}
