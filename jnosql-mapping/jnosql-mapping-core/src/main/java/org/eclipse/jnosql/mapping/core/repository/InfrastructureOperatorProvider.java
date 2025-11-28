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

/**
 * Provides access to the operational components required by the dynamic
 * repository proxy. Each returned component defines how a specific category
 * of infrastructure-level method invocation should be performed.
 *
 * <p>
 * These operators are used exclusively by the proxy implementation and are
 * not part of the semantic execution layer. Code-generated repositories do
 * not depend on these components.
 *
 * @since 1.1
 */
public interface InfrastructureOperatorProvider {

    /**
     * Returns the operator responsible for invoking default interface methods
     * defined in user repository interfaces.
     *
     * @return the default method operator
     */
    BuiltInMethodOperator buildInMethodOperator();

    /**
     * Returns the operator responsible for invoking methods inherited from
     * {@code java.lang.Object} on the proxy instance.
     *
     * @return the object method operator
     */
    ObjectMethodOperator objectMethodOperator();

    /**
     * Returns the operator responsible for invoking custom CDI-backed repository
     * methods defined outside the standard Jakarta Data contract.
     *
     * @return the custom repository method operator
     */
    CustomRepositoryMethodOperator customRepositoryMethodOperator();

    /**
     * Returns the operator responsible for invoking default methods defined
     * in repository interfaces. These operators execute default interface
     * methods outside the semantic repository operation pipeline, leveraging
     * the JVM's default-method invocation mechanism.
     *
     * @return an instance of DefaultMethodOperator to handle default method
     *         invocation in repository interfaces
     */
    DefaultMethodOperator defaultMethodOperator();
}