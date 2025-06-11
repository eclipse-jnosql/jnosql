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
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.and;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.between;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.eq;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.gt;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.gte;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.in;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.like;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.lt;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.lte;
import static org.eclipse.jnosql.communication.semistructured.CriteriaCondition.or;


/**
 * Converts a {@link jakarta.data.restrict.Restriction} into its internal representation
 * for query construction.
 * <p>
 * This utility is used internally by the query engine to transform domain-specific
 * constraints into a format suitable for building executable queries.
 * </p>
 *
 *
 * @see jakarta.data.restrict.Restriction
 */
public enum RestrictionConverter {
    INSTANCE;

    private static final Logger LOGGER = Logger.getLogger(RestrictionConverter.class.getName());

    /**
     * Parses a {@link jakarta.data.restrict.Restriction} and attempts to convert it
     * into a {@link CriteriaCondition} based on the provided entity metadata and value converters.
     *
     * <p>This method is used internally to bridge the constraint API and the query
     * execution engine.</p>
     *
     * @param restriction     the user-defined query restriction
     * @param entityMetadata  metadata about the entity being queried, used to resolve attributes
     * @param converters      converters for translating Java types to query-native formats
     * @return an {@code Optional<CriteriaCondition>} if conversion is possible; otherwise, an empty Optional
     * @throws NullPointerException if any of the arguments are {@code null}
     */
    public Optional<CriteriaCondition> parser(Restriction<?> restriction, EntityMetadata entityMetadata, Converters converters) {
        Objects.requireNonNull(restriction, "restriction is required");
        Objects.requireNonNull(entityMetadata, "entityMetadata is required");
        Objects.requireNonNull(converters, "converters is required");

        LOGGER.fine(() -> "Converter is invoked for restriction " + restriction);

        CriteriaCondition criteriaCondition;
        switch (restriction) {
            case BasicRestriction<?, ?> basicRestriction -> {
                if (basicRestriction.expression() instanceof BasicAttribute<?, ?> basicAttribute) {
                    Constraint<?> constraint = basicRestriction.constraint();
                    criteriaCondition = condition(basicAttribute, constraint, entityMetadata, converters);
                } else {
                    throw new UnsupportedOperationException("The expression " + basicRestriction.expression() + " is not supported");
                }
            }
            case CompositeRestriction<?> compositeRestriction -> {
                var negated = compositeRestriction.isNegated();
                var conditions = compositeRestriction.restrictions()
                        .stream()
                        .filter(r -> r instanceof BasicRestriction<?, ?>)
                        .map(r -> negated ? r.negate() : r)
                        .map(r -> parser(r, entityMetadata,
                                converters))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toArray(CriteriaCondition[]::new);
                criteriaCondition = switch (compositeRestriction.type()) {
                    case ALL -> negated ? or(conditions) : and(conditions);
                    case ANY -> negated ? and(conditions) : or(conditions);
                };
            }
            default ->
                    throw new UnsupportedOperationException("Unsupported restriction type: " + restriction.getClass().getName());
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
                return eq(name, value);
            }
            case NotEqualTo<?> notEqualTo -> {
                var value = ValueConverter.of(notEqualTo::expression, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return eq(name, value).negate();
            }
            case LessThan<?> lessThan -> {
                var value = ValueConverter.of(lessThan::bound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return lt(name, value);
            }

            case GreaterThan<?> greaterThan -> {
                var value = ValueConverter.of(greaterThan::bound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return gt(name, value);
            }

            case GreaterThanOrEqual<?> greaterThanOrEqual -> {
                var value = ValueConverter.of(greaterThanOrEqual::bound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return gte(name, value);
            }

            case LessThanOrEqual<?> lesserThanOrEqual -> {
                var value = ValueConverter.of(lesserThanOrEqual::bound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return lte(name, value);
            }

            case Between<?> between -> {
                var lowerBound = ValueConverter.of(between::lowerBound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                var upperBound = ValueConverter.of(between::upperBound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return between(name, List.of(lowerBound, upperBound));
            }

            case NotBetween<?> between -> {
                var lowerBound = ValueConverter.of(between::lowerBound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                var upperBound = ValueConverter.of(between::upperBound, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return between(name, List.of(lowerBound, upperBound)).negate();
            }

            case Like like -> {
                var value = ValueConverter.of(like::pattern, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return like(name, value);
            }

            case NotLike like -> {
                var value = ValueConverter.of(like::pattern, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null));
                return like(name, value).negate();
            }

            case Null<?> isNull -> {
                return eq(name, Value.ofNull());
            }

            case NotNull<?> isNull -> {
                return eq(name, Value.ofNull()).negate();
            }

            case In<?> in -> {
                var values = in.expressions().stream().map(expression -> ValueConverter.of(() -> expression, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null))).toList();
                return in(name, values);
            }

            case NotIn<?> in -> {
                var values = in.expressions().stream().map(expression -> ValueConverter.of(() -> expression, basicAttribute, converters,
                        converter.orElse(null), fieldMetadata.orElse(null))).toList();
                return in(name, values).negate();
            }

            default -> throw new UnsupportedOperationException("Unexpected value: " + constraint);
        }

    }
}
