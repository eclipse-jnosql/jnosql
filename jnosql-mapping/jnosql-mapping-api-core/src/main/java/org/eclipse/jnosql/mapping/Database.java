/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping;


import jakarta.inject.Qualifier;
import jakarta.nosql.Template;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * The database qualifier used on Jakarta NoSQL Mapping such as defines which interpreter will be used on {@link Template}
 * with {@link Database}
 *
 * <pre>{@code
 * @Inject
 * @Database(DatabaseType.DOCUMENT)
 * Template template;
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
@Qualifier
public @interface Database {


    /**
     * Defines the type on qualifier
     *
     * @return the database type
     */
    DatabaseType value();

    /**
     * Defines the database provider, used to select one of several databases of the same type.
     * The default empty value selects the default provider.
     *
     * @return the provider
     */
    String provider() default "";


}
