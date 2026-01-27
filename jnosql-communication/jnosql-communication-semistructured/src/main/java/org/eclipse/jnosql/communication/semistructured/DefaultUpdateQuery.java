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

record DefaultUpdateQuery(String name, List<Element> set, CriteriaCondition criteriaCondition) implements UpdateQuery {

    @Override
    public Optional<CriteriaCondition> where() {
        return Optional.ofNullable(criteriaCondition);
    }

    @Override
    public SelectQuery toSelectQuery() {
        return new DefaultSelectQuery(0, 0, name, emptyList(), emptyList(), criteriaCondition, false);
    }
}
