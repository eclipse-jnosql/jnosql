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
 * Represents a repository operation executed through a provider-defined query
 * mechanism. A method is mapped to a {@code ProviderOperation} when its query
 * annotation is associated with a provider via
 * {@link org.eclipse.jnosql.mapping.ProviderQuery}, enabling execution using a
 * custom query language or runtime distinct from Jakarta Data’s built-in
 * operations.
 */
public interface ProviderOperation extends RepositoryOperation {
}
