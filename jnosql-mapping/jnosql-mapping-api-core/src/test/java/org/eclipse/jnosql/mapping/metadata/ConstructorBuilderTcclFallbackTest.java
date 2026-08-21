/*
 *  Copyright (c) 2022,2026 Contributors to the Eclipse Foundation
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
 *   Mohan Lal
 */
package org.eclipse.jnosql.mapping.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Regression test for the TCCL-first-with-fallback ServiceLoader lookup in
 * {@link ConstructorBuilder}.
 * <p>
 * IMPORTANT: {@code CONSTRUCTOR_BUILDER_SUPPLIER} is a field on an interface, and is
 * therefore initialized exactly once per JVM, the first time {@link ConstructorBuilder}
 * is loaded. Because of this, this test class MUST be run in isolation — as the only
 * test in its Surefire fork — so that the TCCL swap below happens before any other
 * code has triggered class initialization. Running it alongside other tests that touch
 * {@link ConstructorBuilder} first will make this test pass regardless of whether the
 * fallback logic actually works, since the static field will already be populated.
 *
 * <pre>
 * mvn -pl jnosql-mapping/jnosql-mapping-api-core \
 *     -Dtest=ConstructorBuilderTcclFallbackTest test
 * </pre>
 */
class ConstructorBuilderTcclFallbackTest {

    @Nested
    @DisplayName("When loading the ConstructorBuilder supplier")
    class WhenTheConstructorBuilderSupplierIsLoaded {

        @Test
        @DisplayName("Should fall back to the defining class loader when the TCCL cannot find the provider")
        void shouldFallBackToDefiningClassLoaderWhenTcclCannotFindProvider() {
            // Given
            ClassLoader original = Thread.currentThread().getContextClassLoader();
            ClassLoader isolated = new ClassLoader(null) {
            };
            ConstructorMetadata metadata = mock(ConstructorMetadata.class);

            try {
                Thread.currentThread().setContextClassLoader(isolated);

                // When
                // First-ever touch of ConstructorBuilder in this JVM fork.
                // The TCCL cannot see the provider, so this must fall back
                // to ConstructorBuilder.class.getClassLoader() to succeed.
                ConstructorBuilder builder = ConstructorBuilder.of(metadata);

                // Then
                assertThat(builder).isNotNull();
            } finally {
                Thread.currentThread().setContextClassLoader(original);
            }
        }
    }
}
