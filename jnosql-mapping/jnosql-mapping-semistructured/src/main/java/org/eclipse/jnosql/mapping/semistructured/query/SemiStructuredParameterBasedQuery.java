/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import jakarta.data.Sort;
import jakarta.data.page.PageRequest;
import jakarta.enterprise.inject.spi.CDI;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.mapping.core.NoSQLPage;
import org.eclipse.jnosql.mapping.semistructured.MappingQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import static org.eclipse.jnosql.mapping.core.util.ConverterUtil.getValue;

/**
 * The ColumnParameterBasedQuery class is responsible for generating Column queries based on a set of parameters.
 * It leverages the provided parameters, PageRequest information, and entity metadata to construct a ColumnQuery object
 * tailored for querying a specific entity'sort columns.
 */
public enum SemiStructuredParameterBasedQuery {


    INSTANCE;
    private static final IntFunction<CriteriaCondition[]> TO_ARRAY = CriteriaCondition[]::new;

    /**
     * Constructs a ColumnQuery based on the provided parameters, PageRequest information, and entity metadata.
     *
     * @param params          The map of parameters used for filtering columns.
     * @param entityMetadata  Metadata describing the structure of the entity.
     * @return                 A ColumnQuery instance tailored for the specified entity.
     */
    public org.eclipse.jnosql.communication.semistructured.SelectQuery toQuery(Map<String, Object> params,
                                                                               List<Sort<?>> sorts,
                                                                               EntityMetadata entityMetadata) {
        var convert = CDI.current().select(Converters.class).get();
        List<CriteriaCondition> conditions = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            conditions.add(condition(convert, entityMetadata, entry));
        }

        List<Sort<?>> updateSorter = getSorts(sorts, entityMetadata);

        var condition = condition(conditions);
        var entity = entityMetadata.name();
        return new MappingQuery(updateSorter, 0L, 0L, condition, entity, List.of());
    }

    /**
     * Constructs a ColumnQuery based on the provided parameters, PageRequest information, and entity metadata.
     * This method avoid CDI and don't start the container.
     * @param params          The map of parameters used for filtering columns.
     * @param pageRequest    The PageRequest object containing pagination information.
     * @param entityMetadata  Metadata describing the structure of the entity.
     * @return                 A ColumnQuery instance tailored for the specified entity.
     */
    public org.eclipse.jnosql.communication.semistructured.SelectQuery toQueryNative(Map<String, Object> params,
                                                                               List<Sort<?>> sorts, PageRequest pageRequest,
                                                                               EntityMetadata entityMetadata) {
        List<CriteriaCondition> conditions = new ArrayList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            conditions.add(condition(entityMetadata, entry));
        }

        List<Sort<?>> updateSorter = getSorts(sorts, entityMetadata);

        var condition = condition(conditions);
        var entity = entityMetadata.name();
        long limit = 0;
        long skip = 0;
        if(pageRequest != null) {
            limit = pageRequest.size();
            skip = NoSQLPage.skip(pageRequest);
        }
        return new MappingQuery(updateSorter, limit, skip, condition, entity, List.of());
    }

    private CriteriaCondition condition(List<CriteriaCondition> conditions) {
        if (conditions.isEmpty()) {
            return null;
        } else if (conditions.size() == 1) {
            return conditions.get(0);
        }
        return CriteriaCondition.and(conditions.toArray(TO_ARRAY));
    }

    private CriteriaCondition condition(Converters convert, EntityMetadata entityMetadata, Map.Entry<String, Object> entry) {
        var name = entityMetadata.fieldMapping(entry.getKey())
                .map(FieldMetadata::name)
                .orElse(entry.getKey());
        var value = getValue(entry.getValue(), entityMetadata, entry.getKey(), convert);
        return CriteriaCondition.eq(name, value);
    }

    private CriteriaCondition condition( EntityMetadata entityMetadata, Map.Entry<String, Object> entry) {
        var name = entityMetadata.fieldMapping(entry.getKey())
                .map(FieldMetadata::name)
                .orElse(entry.getKey());
        var value = entry.getValue();
        return CriteriaCondition.eq(name, value);
    }

    private List<Sort<?>> getSorts(List<Sort<?>> sorts, EntityMetadata entityMetadata) {
        List<Sort<?>> updateSorter = new ArrayList<>();
        for (Sort<?> sort : sorts) {
            var name = entityMetadata.fieldMapping(sort.property())
                    .map(FieldMetadata::name)
                    .orElse(sort.property());
            updateSorter.add(sort.isAscending()? Sort.asc(name): Sort.desc(name));
        }
        return updateSorter;
    }
}
