/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses.
 *
 */

package org.eclipse.jnosql.mapping.semistructured;

import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DefaultSelectQuery;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.communication.semistructured.UpdateQuery;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

record SemistructureUpdateQuery(String name, List<Element> set, CriteriaCondition criteriaCondition) implements UpdateQuery {

    @Override
    public Optional<CriteriaCondition> condition() {
        return Optional.ofNullable(criteriaCondition);
    }

    @Override
    public SelectQuery toSelectQuery() {
        return new DefaultSelectQuery(0, 0, name, emptyList(), emptyList(), criteriaCondition, false);
    }
}
