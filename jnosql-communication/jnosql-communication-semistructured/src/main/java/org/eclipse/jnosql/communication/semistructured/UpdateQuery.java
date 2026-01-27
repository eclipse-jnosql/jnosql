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

/**
 * Defines the structure and behavior of an update query within a database. This interface facilitates
 * updating specified entities based on certain conditions and allows converting the update operation
 * into a corresponding select query to review the affected records.
 */
public interface UpdateQuery {

    /**
     * Retrieves the name of the entity to be updated. The entity name is typically associated with
     * a specific table or data structure within the database.
     *
     * @return the name of the entity targeted by this update query.
     */
    String name();

    /**
     * Retrieves a list of modifications to be applied to the entity. Each {@link Element} in the list
     * specifies a field and the new value that the field should be set to during the update operation.
     *
     * @return a list of {@link Element} objects that detail the fields to be updated and their new values.
     */
    List<Element> set();

    /**
     * Retrieves the condition under which the entity's records are to be updated. The condition defines
     * the criteria that identify which records are affected by the update. If the condition is not provided,
     * the implementation behavior varies: it may throw an UnsupportedOperationException if conditions are
     * mandatory, or it may apply the update to all records in the entity, potentially leading to extensive
     * modifications.
     *
     * @return an {@link Optional} of {@link CriteriaCondition} that filters which records are updated;
     *         if empty, the update may be unrestricted or unsupported, depending on implementation.
     */
    Optional<CriteriaCondition> where();

    /**
     * Converts this update query into a {@link SelectQuery} that reflects the equivalent selection
     * criteria used in this update. This allows for reviewing or fetching the records that match
     * the update criteria before or after applying the update, providing a way to validate or examine
     * affected data.
     *
     * @return a {@link SelectQuery} representing the selection criteria equivalent to this update's conditions.
     */
    SelectQuery toSelectQuery();
}
