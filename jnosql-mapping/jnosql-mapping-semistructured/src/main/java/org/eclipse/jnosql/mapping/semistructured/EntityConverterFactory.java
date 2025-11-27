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
 *   Michele Rastelli
 */
package org.eclipse.jnosql.mapping.semistructured;

import org.eclipse.jnosql.communication.semistructured.IdFieldNameSupplier;

/**
 * Factory interface for creating instances of {@link EntityConverter}.
 * Allows customization of entity conversion behaviors by supplying an implementation
 * of {@link IdFieldNameSupplier}.
 * This interface abstracts the creation logic for {@link EntityConverter},
 * enabling contextual configuration for specific database implementation.
 */
public interface EntityConverterFactory {
    EntityConverter create(IdFieldNameSupplier idFieldNameSupplier);
}
