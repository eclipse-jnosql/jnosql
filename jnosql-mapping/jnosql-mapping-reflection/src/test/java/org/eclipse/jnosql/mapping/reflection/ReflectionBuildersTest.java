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

import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.ClassScanner;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.GroupEntityMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.reflection.repository.ReflectionRepositoriesMetadataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit tests for reflection-based builders")
class ReflectionBuildersTest {

    @Test
    @DisplayName("shouldCreateReflectionGroupEntityMetadataUsingBuilder")
    void shouldCreateReflectionGroupEntityMetadataUsingBuilder() {
        GroupEntityMetadata metadata = ReflectionGroupEntityMetadataBuilder.builder()
                .withScanner(ClassScanner.load())
                .withConverter(ClassConverter.load())
                .build();

        assertSoftly(softly -> {
            softly.assertThat(metadata).isNotNull();
            softly.assertThat(metadata.classes()).isNotEmpty();
            softly.assertThat(metadata.mappings()).isNotEmpty();
            softly.assertThat(metadata.projections()).isNotNull();
        });
    }

    @Test
    @DisplayName("shouldThrowExceptionWhenScannerIsNullInGroupBuilder")
    void shouldThrowExceptionWhenScannerIsNullInGroupBuilder() {
        assertThrows(NullPointerException.class, () ->
                ReflectionGroupEntityMetadataBuilder.builder().withScanner(null));
    }

    @Test
    @DisplayName("shouldCreateEntitiesMetadataUsingBuilder")
    void shouldCreateEntitiesMetadataUsingBuilder() {
        GroupEntityMetadata group = ReflectionGroupEntityMetadataBuilder.builder()
                .withScanner(ClassScanner.load())
                .withConverter(ClassConverter.load())
                .build();

        EntitiesMetadata entities = ReflectionEntitiesMetadataBuilder.builder()
                .withGroup(group)
                .build();

        assertThat(entities).isNotNull();
    }

    @Test
    @DisplayName("shouldCreateRepositoriesMetadataUsingBuilder")
    void shouldCreateRepositoriesMetadataUsingBuilder() {
        AtomicBoolean observerCalled = new AtomicBoolean(false);
        RepositoriesMetadata repositories = ReflectionRepositoriesMetadataBuilder.builder()
                .withObserver(projectionFound -> observerCalled.set(true))
                .build();

        assertSoftly(softly -> {
            softly.assertThat(repositories).isNotNull();
            // Since repositories are loaded via ClassScanner, we just check it doesn't throw
        });
    }

    @Test
    @DisplayName("shouldCreateRepositoriesMetadataWithNoOpObserver")
    void shouldCreateRepositoriesMetadataWithNoOpObserver() {
        RepositoriesMetadata repositories = ReflectionRepositoriesMetadataBuilder.builder()
                .build();

        assertThat(repositories).isNotNull();
    }
}
