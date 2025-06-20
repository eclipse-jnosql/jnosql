/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.core.query;

import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.UUID;

public class TestRepositoryProxy extends AbstractRepositoryProxy<TestEntity, UUID> {

    @Override
    protected AbstractRepository<TestEntity, UUID> repository() {
        return Mockito.mock(AbstractRepository.class);
    }

    @Override
    protected Class<?> repositoryType() {
        return TestRepository.class;
    }

    @Override
    protected EntityMetadata entityMetadata() {
        return Mockito.mock(EntityMetadata.class);
    }

    @Override
    protected Object executeQuery(Object instance, Method method, Object[] params) {
        return "executeQuery";
    }

    @Override
    protected Object executeDeleteByAll(Object instance, Method method, Object[] params) {
        return "executeDeleteByAll";
    }

    @Override
    protected Object executeFindAll(Object instance, Method method, Object[] params) {
        return "executeFindAll";
    }

    @Override
    protected Object executeExistByQuery(Object instance, Method method, Object[] params) {
        return "executeExistByQuery";
    }

    @Override
    protected Object executeCountByQuery(Object instance, Method method, Object[] params) {
        return "executeCountByQuery";
    }

    @Override
    protected Object executeFindByQuery(Object instance, Method method, Object[] params) {
        return "executeFindByQuery";
    }

    @Override
    protected Object executeCursorPagination(Object instance, Method method, Object[] params) {
        return "executeCursorPagination";
    }

    @Override
    protected Object executeParameterBased(Object instance, Method method, Object[] params) {
        return "executeParameterBased";
    }

    @Override
    protected Object executeDeleteRestriction(Object instance, Method method, Object[] params) {
        return "executeDeleteRestriction";
    }
}