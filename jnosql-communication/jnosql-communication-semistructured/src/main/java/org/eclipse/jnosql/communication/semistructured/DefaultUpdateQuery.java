/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */

package org.eclipse.jnosql.communication.semistructured;


import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

/**
 * Represents a default implementation of the {@link UpdateQuery} interface for constructing and
 * handling database update operations. This record encapsulates details such as the target entity name,
 * the list of field-value pairs to be updated, and the criteria used to filter records for the update.
 * It also supports converting the update query into a corresponding {@link SelectQuery} to review affected records.
 *
 * @param name               the name of the entity targeted by the update query
 * @param sets               the list of {@link Element} instances representing the fields and their updated values
 * @param criteriaCondition  the {@link CriteriaCondition} specifying the criteria to filter records for the update
 */
public record DefaultUpdateQuery(String name, List<Element> sets, CriteriaCondition criteriaCondition) implements UpdateQuery {

    @Override
    public Optional<CriteriaCondition> where() {
        return Optional.ofNullable(criteriaCondition);
    }

    @Override
    public SelectQuery toSelectQuery() {
        return new DefaultSelectQuery(0, 0, name, emptyList(), emptyList(), criteriaCondition, false);
    }
}
