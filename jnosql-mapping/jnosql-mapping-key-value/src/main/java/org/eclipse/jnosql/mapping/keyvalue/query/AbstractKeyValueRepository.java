/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.keyvalue.query;


import org.eclipse.jnosql.mapping.core.query.AbstractRepository;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;

/**
 * The template method to key-value repository
 */
public abstract class AbstractKeyValueRepository<T, K> extends AbstractRepository<T, K> {

    @Override
    protected abstract KeyValueTemplate template();

    @Override
    protected String getErrorMessage() {
        return "The key-value type does not support %s method";
    }

}
