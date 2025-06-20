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

import jakarta.data.metamodel.BasicAttribute;
import jakarta.data.metamodel.NumericAttribute;
import jakarta.data.metamodel.StaticMetamodel;
import jakarta.data.metamodel.TextAttribute;

import javax.annotation.processing.Generated;
import java.math.BigDecimal;

//CHECKSTYLE:OFF
@StaticMetamodel(Product.class)
@Generated(value = "The StaticMetamodel of the class ProductA provider by Eclipse JNoSQL", date = "2025-06-09T09:16:59.979587")
public interface _Product {

    String NAME = "name";
    String PRICE = "price";
    String TYPE = "type";
    String AMOUNT = "amount";

    TextAttribute<Product> name = TextAttribute.of(Product.class, NAME);
    NumericAttribute<Product, BigDecimal> price = NumericAttribute.of(Product.class, PRICE, java.math.BigDecimal.class);
    BasicAttribute<Product, Product.ProductType> type = BasicAttribute.of(Product.class, TYPE, Product.ProductType.class);
    BasicAttribute<Product, Money> amount = BasicAttribute.of(Product.class, AMOUNT, Money.class);

}
//CHECKSTYLE:ON