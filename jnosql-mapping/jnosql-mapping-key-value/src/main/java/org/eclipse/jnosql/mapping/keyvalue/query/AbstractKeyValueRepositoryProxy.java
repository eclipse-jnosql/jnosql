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

import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.core.query.AbstractRepositoryProxy;

import java.lang.reflect.Method;

public abstract class AbstractKeyValueRepositoryProxy<T, K> extends AbstractRepositoryProxy<T, K> {


    protected abstract KeyValueTemplate template();

    protected abstract Class<T> type();

    @Override
    protected abstract Class<?> repositoryType();


    @Override
    protected Object executeQuery(Object instance, Method method, Object[] params) {
        throw new UnsupportedOperationException("Key Value repository does not query");
    }

    @Override
    protected Object executeDeleteByAll(Object instance, Method method, Object[] params) {
        throw new UnsupportedOperationException("Key Value repository does not support query method");
    }

    @Override
    protected Object executeFindAll(Object instance, Method method, Object[] params) {
        throw new UnsupportedOperationException("Key Value repository does not support query method");
    }

    @Override
    protected Object executeExistByQuery(Object instance, Method method, Object[] params) {
        throw new UnsupportedOperationException("Key Value repository does not support query method");
    }

    @Override
    protected Object executeCountByQuery(Object instance, Method method, Object[] params) {
        throw new UnsupportedOperationException("Key Value repository does not support query method");
    }

    @Override
    protected Object executeFindByQuery(Object instance, Method method, Object[] params) {
        throw new UnsupportedOperationException("Key Value repository does not support query method");
    }

    @Override
    protected Object executeParameterBased(Object instance, Method method, Object[] params) {
        throw new UnsupportedOperationException("Key Value repository does not support query method");
    }

    @Override
    protected Object executeCursorPagination(Object instance, Method method, Object[] params) {
        throw new UnsupportedOperationException("Key Value repository does not support Cursor Pagination");
    }

    @Override
    protected Object executeDeleteRestriction(Object instance, Method method, Object[] params) {
        throw  new UnsupportedOperationException("Key Value repository does not support query method");
    }
}
