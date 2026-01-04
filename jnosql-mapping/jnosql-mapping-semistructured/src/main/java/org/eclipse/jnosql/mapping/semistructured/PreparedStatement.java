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

import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.CommunicationPreparedStatement;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.InheritanceMetadata;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Represents a prepared statement specifically for JNoSQL operations, facilitating
 * the binding and execution of queries with parameters. This class wraps a
 * {@link org.eclipse.jnosql.communication.semistructured.CommunicationPreparedStatement} to integrate easily
 * with different JNoSQL components and provides a high-level abstraction to interact
 * with various databases in a semi-structured format.
 *
 * @see org.eclipse.jnosql.mapping.PreparedStatement
 */
public final class PreparedStatement implements org.eclipse.jnosql.mapping.PreparedStatement {

    private final CommunicationPreparedStatement preparedStatement;

    private final EntityConverter converter;

    private final MapperObserver observer;

    private final EntitiesMetadata entitiesMetadata;

    private boolean updated;

    PreparedStatement(CommunicationPreparedStatement preparedStatement,
                      EntityConverter converter, MapperObserver observer, EntitiesMetadata entitiesMetadata) {
        this.preparedStatement = preparedStatement;
        this.converter = converter;
        this.observer = observer;
        this.entitiesMetadata = entitiesMetadata;
    }

    @Override
    public org.eclipse.jnosql.mapping.PreparedStatement bind(String name, Object value) {
        preparedStatement.bind(name, value);
        return this;
    }

    public org.eclipse.jnosql.mapping.PreparedStatement bind(int index, Object value) {
        preparedStatement.bind(index, value);
        return this;
    }

    public CommunicationPreparedStatement.PreparedStatementType type() {
        return this.preparedStatement.getType();
    }

    @Override
    public <T> Stream<T> result() {
        updateQuery();
        Function<T, T> fieldMapper = SelectFieldMapper.INSTANCE.map(observer, entitiesMetadata);
        return preparedStatement.result().<T>map(converter::toEntity).map(fieldMapper);
    }

    @Override
    public <T> Optional<T> singleResult() {
        updateQuery();
        Optional<CommunicationEntity> singleResult = preparedStatement.singleResult();
        Optional<T> result = singleResult.map(converter::toEntity);
        return result.map(SelectFieldMapper.INSTANCE.map(observer, entitiesMetadata));
    }

    @Override
    public long count() {
        updateQuery();
        return preparedStatement.count();
    }

    @Override
    public boolean isCount() {
        return selectQuery().map(SelectQuery::isCount).orElse(false);
    }

    /**
     * Optionally returns the underlying {@link SelectQuery} associated with this PreparedStatement,
     * if applicable.
     *
     * @return an optional {@link SelectQuery} representing the query details bound to this PreparedStatement
     */
    public Optional<SelectQuery> selectQuery() {
        return preparedStatement.select();
    }

    /**
     * Sets the operator to be used in the query.
     *
     * @param selectMapper the operator
     */
    public void setSelectMapper(UnaryOperator<SelectQuery> selectMapper) {
        Objects.requireNonNull(selectMapper, "selectMapper is required");
        this.updated = true;
        this.preparedStatement.setSelectMapper(selectMapper);
    }

    private void updateQuery() {
        if (this.observer.isInherited() && !this.updated) {
            this.preparedStatement.setSelectMapper(selectQuery ->
                    new MappingQuery(selectQuery.sorts(), selectQuery.limit(), selectQuery.skip(),
                            appendCriteriaCondition(selectQuery.condition().orElse(null),
                                    createInheritance(observer.entityMetadata())),
                            selectQuery.name(),
                            selectQuery.columns()));
        }
    }


    private static CriteriaCondition appendCriteriaCondition(CriteriaCondition condition, CriteriaCondition newCondition) {
        if (condition != null) {
            return CriteriaCondition.and(condition, newCondition);
        } else {
            return newCondition;
        }
    }

    private static CriteriaCondition createInheritance(EntityMetadata metadata) {
        if (metadata.inheritance().isPresent()) {
            InheritanceMetadata inheritanceMetadata = metadata.inheritance().orElseThrow();
            if (!inheritanceMetadata.parent().equals(metadata.type())) {
                return CriteriaCondition.eq(Element.of(inheritanceMetadata.discriminatorColumn(),
                        inheritanceMetadata.discriminatorValue()));
            }
        }
        return null;
    }
}
