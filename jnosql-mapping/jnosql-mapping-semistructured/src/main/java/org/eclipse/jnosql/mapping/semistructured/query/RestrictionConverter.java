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

import jakarta.data.constraint.Between;
import jakarta.data.constraint.Constraint;
import jakarta.data.constraint.EqualTo;
import jakarta.data.constraint.GreaterThan;
import jakarta.data.constraint.GreaterThanOrEqual;
import jakarta.data.constraint.In;
import jakarta.data.constraint.LessThan;
import jakarta.data.constraint.LessThanOrEqual;
import jakarta.data.constraint.Like;
import jakarta.data.constraint.NotBetween;
import jakarta.data.constraint.NotEqualTo;
import jakarta.data.constraint.NotIn;
import jakarta.data.constraint.NotLike;
import jakarta.data.constraint.NotNull;
import jakarta.data.constraint.Null;
import jakarta.data.metamodel.BasicAttribute;
import jakarta.data.restrict.BasicRestriction;
import jakarta.data.restrict.CompositeRestriction;
import jakarta.data.restrict.Restriction;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

enum RestrictionConverter {
    INSTANCE;

    private static final Logger LOGGER = Logger.getLogger(RestrictionConverter.class.getName());

    Optional<CriteriaCondition> parser(Restriction<?> restriction, EntityMetadata entityMetadata, Converters converters) {
        LOGGER.fine(() -> "Converter is invoked for restriction " + restriction);

        CriteriaCondition criteriaCondition = null;
        switch (restriction){
            case BasicRestriction<?, ?> basicRestriction -> {
                if (basicRestriction.expression() instanceof BasicAttribute<?, ?> basicAttribute) {
                    Constraint<?> constraint = basicRestriction.constraint();
                    criteriaCondition = condition(basicAttribute, constraint, entityMetadata, converters);
                } else {
                    throw  new UnsupportedOperationException("The expression " + basicRestriction.expression() + " is not supported");
                }
            }
            case CompositeRestriction<?> compositeRestriction -> {

            }
            default -> throw new UnsupportedOperationException("Unsupported restriction type: " + restriction.getClass().getName());
        }
        return Optional.ofNullable(criteriaCondition);
    }

    private CriteriaCondition condition(BasicAttribute<?, ?> basicAttribute, Constraint<?> constraint,
                                        EntityMetadata entityMetadata, Converters converters) {
        var name = basicAttribute.name();
        var fieldMetadata = entityMetadata.fieldMapping(name);
        var converter = fieldMetadata.stream().flatMap(f -> f.converter().stream()).findFirst();

        switch (constraint) {
            case EqualTo<?> equalTo -> {
                var value = ValueConverter.of(equalTo::expression, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return CriteriaCondition.eq(name, value);
            }
            case NotEqualTo<?> notEqualTo -> {
                var value = ValueConverter.of(notEqualTo::expression, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return CriteriaCondition.eq(name, value).negate();
            }
            case LessThan<?> lessThan -> {
                var value = ValueConverter.of(lessThan::bound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return CriteriaCondition.lt(name, value);
            }

            case GreaterThan<?> greaterThan -> {
                var value = ValueConverter.of(greaterThan::bound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return CriteriaCondition.gt(name, value);
            }

            case GreaterThanOrEqual<?> greaterThanOrEqual -> {
                var value = ValueConverter.of(greaterThanOrEqual::bound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return CriteriaCondition.gte(name, value);
            }

            case LessThanOrEqual<?> lesserThanOrEqual -> {
                var value = ValueConverter.of(lesserThanOrEqual::bound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return CriteriaCondition.lte(name, value);
            }

            case Between<?> between -> {
                var lowerBound = ValueConverter.of(between::lowerBound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                var upperBound = ValueConverter.of(between::upperBound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return CriteriaCondition.between(name, List.of(lowerBound, upperBound));
            }

            case NotBetween<?> between -> {
                var lowerBound = ValueConverter.of(between::lowerBound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                var upperBound = ValueConverter.of(between::upperBound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return CriteriaCondition.between(name, List.of(lowerBound, upperBound)).negate();
            }

            case Like like -> {
                var value = ValueConverter.of(like::pattern, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return CriteriaCondition.like(name, value);
            }

            case NotLike like -> {
                var value = ValueConverter.of(like::pattern, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return CriteriaCondition.like(name, value).negate();
            }

            case Null<?> isNull -> {
                return CriteriaCondition.eq(name, Value.ofNull());
            }

            case NotNull<?> isNull -> {
                return CriteriaCondition.eq(name, Value.ofNull()).negate();
            }

            case In<?> in -> {
                var values = in.expressions().stream().map( expression -> ValueConverter.of(() -> expression, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null))).toList();
                return CriteriaCondition.in(name, values);
            }

            case NotIn<?> in -> {
                var values = in.expressions().stream().map( expression -> ValueConverter.of(() -> expression, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null))).toList();
                return CriteriaCondition.in(name, values).negate();
            }

            default -> throw new UnsupportedOperationException("Unexpected value: " + constraint);
        }

    }
}
