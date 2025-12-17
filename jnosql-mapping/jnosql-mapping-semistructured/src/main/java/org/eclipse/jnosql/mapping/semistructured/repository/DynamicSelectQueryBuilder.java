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
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.semistructured.repository;

import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.MappingQuery;

enum DynamicSelectQueryBuilder {

INSTANCE;


    SelectQuery includeInheritance(SelectQuery query, EntityMetadata metadata) {
        var condition = includeInheritance(metadata);
        if (condition == null) {
            return query;
        }
        if (query.condition().isPresent()) {
            CriteriaCondition columnCondition = query.condition().orElseThrow();
            condition = condition.and(columnCondition);
        }
        return new MappingQuery(query.sorts(), query.limit(), query.skip(),
                condition, query.name(), query.columns());
    }

    CriteriaCondition includeInheritance(EntityMetadata metadata) {
        if (metadata.inheritance().isPresent()) {
            InheritanceMetadata inheritanceMetadata = metadata.inheritance().orElseThrow();
            if (!inheritanceMetadata.parent().equals(metadata.type())) {
                return CriteriaCondition.eq(Element.of(inheritanceMetadata.discriminatorColumn(),
                        inheritanceMetadata.discriminatorValue()));
            }
        }
        return null;
    }

    SelectQuery applyInheritance(SelectQuery query, RepositoryInvocationContext context) {
        var entityMetadata = context.entityMetadata();
        return includeInheritance(query, entityMetadata);
    }
}
