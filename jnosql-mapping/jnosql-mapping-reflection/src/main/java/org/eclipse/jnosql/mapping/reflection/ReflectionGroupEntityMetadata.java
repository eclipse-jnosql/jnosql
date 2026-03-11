/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.reflection;

import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.GroupEntityMetadata;
import org.eclipse.jnosql.mapping.metadata.ProjectionMetadata;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * A static, immutable implementation of {@link GroupEntityMetadata}.
 * It holds the maps of {@link EntityMetadata} and {@link ProjectionMetadata} discovered during the scanning phase.
 */
record ReflectionGroupEntityMetadata(Map<String, EntityMetadata> mappings,
                                    Map<Class<?>, EntityMetadata> classes,
                                    Map<Class<?>, ProjectionMetadata> projections)
        implements GroupEntityMetadata {

    ReflectionGroupEntityMetadata {
        Objects.requireNonNull(mappings, "mappings is required");
        Objects.requireNonNull(classes, "classes is required");
        Objects.requireNonNull(projections, "projections is required");
        mappings = Collections.unmodifiableMap(mappings);
        classes = Collections.unmodifiableMap(classes);
        projections = Collections.unmodifiableMap(projections);
    }
}
