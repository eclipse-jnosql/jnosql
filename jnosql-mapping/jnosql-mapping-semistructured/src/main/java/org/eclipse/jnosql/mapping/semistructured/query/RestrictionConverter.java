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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.data.restrict.BasicRestriction;
import jakarta.data.restrict.CompositeRestriction;
import jakarta.data.restrict.Restriction;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.util.Optional;
import java.util.logging.Logger;

enum RestrictionConverter {
    INSTANCE;

    private static final Logger LOGGER = Logger.getLogger(RestrictionConverter.class.getName());

    Optional<CriteriaCondition> parser(Restriction<?> restriction, EntityMetadata entityMetadata) {
        LOGGER.fine(() -> "Converter is invoked for restriction " + restriction);
        switch (restriction){
            case BasicRestriction<?, ?> basicRestriction -> {

            }
            case CompositeRestriction<?> compositeRestriction -> {

            }
            default -> throw new UnsupportedOperationException("Unsupported restriction type: " + restriction.getClass().getName());
        }
        return Optional.empty();
    }
}
