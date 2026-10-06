/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License 2.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at https://www.eclipse.org/legal/epl-2.0
 *   and the Apache License v2.0 is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnosql.mapping.vector.spi;

import org.eclipse.jnosql.mapping.DatabaseQualifier;
import org.eclipse.jnosql.mapping.vector.DenseVector;
import org.eclipse.jnosql.mapping.vector.VectorTemplate;
import org.eclipse.jnosql.mapping.vector.entities.Article;
import org.eclipse.jnosql.mapping.vector.entities.ArticleRepository;
import org.jboss.weld.environment.se.Weld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@DisplayName("Vector CDI bootstrap")
class VectorBootstrapTest {

    @Nested
    @DisplayName("When starting CDI with automatic bean and extension discovery")
    class WhenTheBootstrap {

        @Test
        @DisplayName("Should discover vector templates and repositories without manually registering the extension")
        void shouldDiscoverVectorMapping() {
            try (var container = new Weld().initialize()) {
                VectorTemplate template = container.select(VectorTemplate.class, DatabaseQualifier.ofVector("named")).get();
                ArticleRepository repository = container.select(ArticleRepository.class).get();

                Article named = template.insert(new Article("1", "input", DenseVector.of(1F), new float[]{2F}));
                Article defaultResult = repository.save(new Article("2", "input", DenseVector.of(1F), new float[]{2F}));

                assertSoftly(softly -> {
                    softly.assertThat(named.getContent()).as("named template").isEqualTo("named");
                    softly.assertThat(defaultResult.getContent()).as("default repository").isEqualTo("default");
                });
            }
        }
    }
}
