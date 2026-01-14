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
package org.eclipse.jnosql.mapping.core.repository;

import jakarta.data.constraint.Constraint;
import jakarta.data.metamodel.TextAttribute;
import jakarta.data.restrict.BasicRestriction;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.metadata.repository.spi.RepositoryInvocationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.eclipse.jnosql.mapping.core.repository.DynamicReturn.toSingleResult;

/**
 * Utility component for extracting and normalizing parameter metadata from
 * {@link RepositoryMethod} definitions at runtime.
 * This helper bridges repository method metadata and invocation-time arguments,
 * producing structured representations that can be consumed by query builders,
 * criteria resolvers, and provider-specific execution engines.
 * <p>The utility focuses on two complementary concerns:</p>
 * <ul>
 *   <li>Resolving method parameters into named and positional query parameters</li>
 *   <li>Interpreting {@code by}/{@code is} semantics into executable conditions</li>
 * </ul>
 *
 * Special parameters (such as pagination, sorting, or limits) are automatically
 * ignored, ensuring only semantic query parameters are processed.
 */
public enum RepositoryMetadataUtils {

    INSTANCE;

    /**
     * Extracts query parameters from a repository method invocation.
     * This method maps repository method parameters to both:
     * <ul>
     *   <li>Named parameters, using the parameter name</li>
     *   <li>Positional parameters, using {@code ?1}, {@code ?2}, …</li>
     * </ul>
     *
     * Only non-special parameters are included. Pagination, sorting, and other
     * infrastructural parameters are ignored.
     *
     * @param method the repository method metadata
     * @param args the invocation arguments passed to the method
     * @return a map containing named and positional query parameters
     */
    public Map<String, Object> getParams(RepositoryMethod method, Object[] args) {
        Map<String, Object> params = new HashMap<>();

        var parameters = method.params();
        int queryIndex = 1;
        for (int index = 0; index < parameters.size(); index++) {
            var parameter = parameters.get(index);
            boolean isNotSpecialParameter = SpecialParameters.isNotSpecialParameter(parameter.type());
            if (isNotSpecialParameter) {
                var param = parameter.param();
                params.put(param, args[index]);
                params.put("?" + queryIndex++, args[index]);
            }
        }
        return params;
    }

    /**
     * Extracts query parameters from a repository method invocation.
     * This method maps repository method parameters to:
     * <ul>
     *   <li>Named parameters, using the parameter name</li>
     * </ul>
     *
     * Only non-special parameters are included. Pagination, sorting, and other
     * infrastructural parameters are ignored.
     *
     * @param method the repository method metadata
     * @param args the invocation arguments passed to the method
     * @return a map containing named and positional query parameters
     */
    public Map<String, Object> getParamsFromName(RepositoryMethod method, Object[] args) {
        Map<String, Object> params = new HashMap<>();

        var parameters = method.params();
        for (int index = 0; index < parameters.size(); index++) {
            var parameter = parameters.get(index);
            boolean isNotSpecialParameter = SpecialParameters.isNotSpecialParameter(parameter.type());
            if (isNotSpecialParameter) {
                var param = parameter.param();
                params.put(param, args[index]);
            }
        }
        return params;
    }

    /**
     * Resolves {@code by}/{@code is} parameter semantics into structured condition values.
     *
     * <p>
     * This method interprets repository method parameters used in derived queries
     * (for example {@code findByAgeGreaterThan}) and converts them into
     * {@link ParamValue} instances describing:
     * </p>
     * <ul>
     *   <li>The comparison operation</li>
     *   <li>The argument value</li>
     *   <li>Whether the condition is negated</li>
     * </ul>
     *
     * <p>
     * Constraints may be provided either explicitly via {@link Constraint} instances
     * or implicitly through the parameter metadata.
     * </p>
     *
     * @param method the repository method metadata
     * @param arguments the invocation arguments passed to the method
     * @param entityMetadata the entity metadata
     * @return a map of property names to resolved condition values
     */
    public Map<String, ParamValue> getBy(RepositoryMethod method, Object[] arguments, EntityMetadata entityMetadata) {
        Map<String, ParamValue> params = new HashMap<>();

        var parameters = method.params();
        for (int index = 0; index < parameters.size(); index++) {
            var parameter = parameters.get(index);
            var value = arguments[index];
            boolean isNotSpecialParameter = SpecialParameters.isNotSpecialParameter(parameter.type());
            var by = parameter.by();
            String attribute = entityMetadata.columnField(by);
            if (value instanceof BasicRestriction<?, ?> basicRestriction &&
                basicRestriction.expression() instanceof TextAttribute<?> textAttribute) {
                attribute = entityMetadata.columnField(textAttribute.name());
            }

            var is = parameter.is();
            if (isNotSpecialParameter) {
                params.put(attribute, condition(is.orElse(null), value));
            }
        }
        return params;
    }

    /**
     * Executes a repository method using the provided result stream and adapts it
     * to the method’s declared return type.
     * @param context the repository invocation context containing method metadata
     * @param result the stream of query results to be adapted
     * @param <T> the expected return type of the repository method
     * @return the result converted to the method’s declared return type
     * @throws IllegalStateException if the method return type cannot be resolved
     */
    @SuppressWarnings("unchecked")
    public <T> T execute(RepositoryInvocationContext context, Stream<?> result) {
        var method = context.method();
        var metadata = context.metadata();
        return (T) DynamicReturn.builder()
                .methodName(method.name())
                .classSource(metadata.type())
                .returnType(method.returnType().orElseThrow())
                .result(() ->  result)
                .singleResult(toSingleResult(method.name()).apply(() -> result))
                .build()
                .execute();
    }

    private ParamValue condition(Class<? extends Constraint<?>> isType, Object value) {
        if (Objects.isNull(isType) && !(value instanceof Constraint<?>)) {
            return new ParamValue(Condition.EQUALS, value, false);
        } else if (value instanceof Constraint<?> constraint) {
            return ParamValueUtils.valueFromConstraintInstance(constraint);
        }
        return ParamValueUtils.getParamValue(value, isType);
    }
}
