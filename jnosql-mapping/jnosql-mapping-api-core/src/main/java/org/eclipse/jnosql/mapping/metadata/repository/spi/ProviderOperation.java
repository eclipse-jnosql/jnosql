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
 * Represents an operation executed through a provider-defined query mechanism.
 * A repository method is mapped to a {@code ProviderOperation} when it uses a
 * custom query annotation annotated with
 * {@link org.eclipse.jnosql.mapping.ProviderQuery}.
 * This enables external providers to supply their own query model or execution
 * strategy without altering Jakarta Data’s built-in semantics.
 *
 */
public interface ProviderOperation extends RepositoryOperation {
}
