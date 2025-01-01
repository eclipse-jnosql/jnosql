/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.reflection;

import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.entities.Worker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EmbeddedFieldMetadataTest {

    private EmbeddedFieldMetadata metadata;

    @BeforeEach
    void setUp() {
        ClassConverter converter = new ReflectionClassConverter();
        EntityMetadata entity = converter.apply(Worker.class);
        metadata = (EmbeddedFieldMetadata) entity.fieldsGroupByName().get("job");
    }

    @Test
    void shouldId(){
        Assertions.assertThat(metadata.isId()).isFalse();
    }

    @Test
    void shouldToString(){
        Assertions.assertThat(metadata.toString()).isNotEmpty();
    }

    @Test
    void shouldEqualsHasCode(){
        Assertions.assertThat(metadata).isEqualTo(metadata);
        Assertions.assertThat(metadata).hasSameHashCodeAs(metadata);
    }


}
