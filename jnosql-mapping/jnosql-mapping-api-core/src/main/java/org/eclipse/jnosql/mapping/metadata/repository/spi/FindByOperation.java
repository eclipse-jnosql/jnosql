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
 * Executes a derived query operation for repository methods whose names begin
 * with {@code find} or {@code findBy}. These methods express query semantics
 * directly through their naming pattern (e.g., {@code findByName},
 * {@code findByStatusAndAge}), and the implementation interprets the encoded
 * attributes and operators to perform the selection.
 */
public interface FindByOperation extends RepositoryOperation {
}