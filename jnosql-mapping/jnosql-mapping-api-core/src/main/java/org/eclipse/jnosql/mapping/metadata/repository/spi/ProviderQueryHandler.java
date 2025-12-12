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
package org.eclipse.jnosql.mapping.metadata.repository.spi;

/**
 * Defines the contract that a query-language provider must implement in order
 * to support execution of repository methods annotated with a custom query
 * annotation marked by {@link org.eclipse.jnosql.mapping.ProviderQuery}. A
 * {@code ProviderQueryHandler} is the integration point between the Jakarta
 * Data repository model and an external query mechanism, allowing providers to
 * process metadata and arguments and return results using their own execution
 * strategy.
 */
public interface ProviderQueryHandler  extends RepositoryOperation {
}
