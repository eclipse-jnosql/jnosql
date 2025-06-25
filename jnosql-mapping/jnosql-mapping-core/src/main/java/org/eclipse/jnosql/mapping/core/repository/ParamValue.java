package org.eclipse.jnosql.mapping.core.repository;

import org.eclipse.jnosql.communication.Condition;

/**
 * Represents a parameter value with its condition that can be used in repository queries that has the {@link jakarta.data.repository.Find} annotation at method.
 * It will get the {@link jakarta.data.repository.Param} to each parameter value and combine it with {@link jakarta.data.repository.Is}
 * by default value is {@link Condition#EQUALS}
 */
public record ParamValue(Condition condition, Object value, boolean negate){}