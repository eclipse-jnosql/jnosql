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

import jakarta.data.restrict.Restriction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.repository.operations.CoreDeleteOperation;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.spi.DeleteOperation;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;
import org.eclipse.jnosql.mapping.semistructured.MappingDeleteQuery;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.query.RestrictionConverter;

import java.util.Optional;


/**
 * Semistructured-specific extension of the core {@link DeleteOperation}.
 *
 * <p>This implementation adds support for delete operations based on
 * {@link Restriction} parameters when using a semistructured data model.
 * Repository methods that declare a delete operation with a restriction
 * argument are translated into a semistructured delete query and executed
 * against the underlying {@link SemiStructuredTemplate}.</p>
 *
 * <p>In addition to the core delete semantics (single entity, collections, or
 * arrays), this implementation supports restriction-based deletes and applies
 * inheritance rules defined in the entity metadata when building the delete
 * query.</p>
 *
 * <p>This class is selected automatically by the execution engine when a
 * semistructured repository is in use, replacing the default behavior provided
 * by {@link CoreDeleteOperation}.</p>
 */
@ApplicationScoped
@Typed({SemiStructuredDeleteOperation.class, DeleteOperation.class})
public class SemiStructuredDeleteOperation extends CoreDeleteOperation {

    private final SemistructuredQueryBuilder queryBuilder;

    private final Converters converters;

    @Inject
    SemiStructuredDeleteOperation(SemistructuredQueryBuilder queryBuilder, Converters converters) {
        this.queryBuilder = queryBuilder;
        this.converters = converters;
    }

    SemiStructuredDeleteOperation() {
        this.queryBuilder = null;
        this.converters = null;
    }

    @Override
    protected void deleteByRestriction(RepositoryInvocationContext context, Restriction<?> restriction) {

        var template = (SemiStructuredTemplate) context.template();
        EntityMetadata entityMetadata = context.entityMetadata();
        Optional<CriteriaCondition> condition = RestrictionConverter.INSTANCE.parser(restriction, entityMetadata, converters);
        var deleteQuery = new MappingDeleteQuery(entityMetadata.name(), condition.orElse(null));
        var updateDeleteQuery = queryBuilder.includeInheritance(deleteQuery, entityMetadata);
        template.delete(updateDeleteQuery);
    }
}
