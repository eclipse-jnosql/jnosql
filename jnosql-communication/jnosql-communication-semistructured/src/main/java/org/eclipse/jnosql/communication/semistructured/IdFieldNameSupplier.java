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
package org.eclipse.jnosql.communication.semistructured;

import java.util.Optional;

/**
 * This interface provides a method to supply the default ID field name used in the
 * communication layer for a specific database implementation. Implementations can
 * override the default behavior to define a custom ID field name.
 */
public interface IdFieldNameSupplier {

    /**
     * The id field name to be used in the communication layer for the specific database implementation.
     * If not present, the value of the {@code @Id} annotation will be used.
     * @return the field name
     */
    Optional<String> defaultIdFieldName();

}
