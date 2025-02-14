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
package org.eclipse.jnosql.mapping.semistructured;


import jakarta.data.Sort;

import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;

import java.util.Collections;
import java.util.List;
import java.util.Optional;


/**
 * This record represents a mapping implementation of the {@link SelectQuery} interface.
 * It encapsulates information about sorting, limit, skip, criteria condition, and the entity name.
 */
public record MappingQuery(List<Sort<?>> sorts, long limit, long skip, CriteriaCondition criteriaCondition, String entity, List<String> columns)
        implements SelectQuery {


    @Override
    public String name() {
        return entity;
    }

    @Override
    public Optional<CriteriaCondition> condition() {
        return Optional.ofNullable(criteriaCondition);
    }

    @Override
    public List<Sort<?>> sorts() {
        return Collections.unmodifiableList(sorts);
    }

}
