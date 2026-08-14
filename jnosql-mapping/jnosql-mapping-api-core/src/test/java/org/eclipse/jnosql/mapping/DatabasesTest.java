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
package org.eclipse.jnosql.mapping;

import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.ProcessProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabasesTest {

    @Nested
    @DisplayName("When the database metadata is added")
    class WhenTheDatabaseMetadataIsAdded {

        @SuppressWarnings("rawtypes")
        @Test
        @DisplayName("Should add metadata when the annotation matches the database type")
        void shouldAddMetadataWhenTheAnnotationMatchesTheDatabaseType() {
            ProcessProducer processProducer = mock(ProcessProducer.class);
            AnnotatedMember annotatedMember = mock(AnnotatedMember.class);
            DatabaseType type = DatabaseType.DOCUMENT;
            Set<DatabaseMetadata> databases = new HashSet<>();

            Database databaseAnnotation = mock(Database.class);
            when(databaseAnnotation.value()).thenReturn(DatabaseType.DOCUMENT);

            Set<Annotation> annotations = new HashSet<>();
            annotations.add(databaseAnnotation);
            when(processProducer.getAnnotatedMember()).thenReturn(annotatedMember);
            when(annotatedMember.getAnnotations()).thenReturn(annotations);

            Databases.addDatabase(processProducer, type, databases);

            assertSoftly(softly -> {
                softly.assertThat(databases).hasSize(1);
                softly.assertThat(databases).contains(DatabaseMetadata.of(databaseAnnotation));
            });
        }

        @SuppressWarnings("rawtypes")
        @Test
        @DisplayName("Should not add metadata when no database annotation exists")
        void shouldNotAddMetadataWhenNoDatabaseAnnotationExists() {
            ProcessProducer processProducer = mock(ProcessProducer.class);
            DatabaseType type = DatabaseType.DOCUMENT;
            AnnotatedMember annotatedMember = mock(AnnotatedMember.class);
            Set<DatabaseMetadata> databases = new HashSet<>();

            Set<Annotation> annotations = new HashSet<>();
            when(processProducer.getAnnotatedMember()).thenReturn(annotatedMember);
            when(annotatedMember.getAnnotations()).thenReturn(annotations);

            Databases.addDatabase(processProducer, type, databases);

            assertSoftly(softly -> softly.assertThat(databases).isEmpty());
        }
    }

}