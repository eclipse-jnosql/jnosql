/*
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
 *   Georg Leber
 */
package org.eclipse.jnosql.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a {@code record} type is intended to serve as a projection
 * for mapping selected attributes from an entity in a query result.
 *
 * <p>This annotation allows Jakarta Data providers to understand that the
 * annotated record is designed for partial mapping of entity data, especially
 * in queries using {@code @Query} or {@code @Find} annotations.</p>
 *
 * <p>All Jakarta Data providers <strong>must</strong> support the use of records
 * annotated with {@code @Projection}.</p>
 *
 * <p>All Jakarta Data providers <strong>must</strong> support the use of records
 * annotated with {@code @Projection}.</p>
 * <p>Example usage:</p>
 *
 * <pre>{@code
 * @Projection
 * public record ProductSummary(String name, @Select("price") BigDecimal value) {}
 *
 * @Repository
 * public interface ProductRepository {
 *     @Find
 *     @Select("name")
 *     @Select("price")
 *     List<ProductSummary> fetchSummaries();
 * }
 * }</pre>
 *
 * @see jakarta.data.repository.Select
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface Projection {
}
