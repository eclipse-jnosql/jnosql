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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.reflection.spi;

import jakarta.enterprise.inject.spi.CDI;
import org.eclipse.jnosql.mapping.metadata.GroupEntityMetadata;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@EnableAutoWeld
@AddExtensions(ReflectionEntityMetadataExtension.class)
class ReflectionEntityMetadataExtensionTest {

    @BeforeAll
    static void beforeAll() {
        Logger.getLogger(ReflectionEntityMetadataExtension.class.getName()).setLevel(Level.FINEST);
    }

    @Test
    void shouldPerformTheExtensionSuccessfully() {
        assertSoftly(softly -> {

            var cdiBean = CDI.current().select(GroupEntityMetadata.class);

            softly.assertThat(cdiBean)
                    .as("should return an non-null {} instance from CDI", GroupEntityMetadata.class)
                    .isNotNull();
            softly.assertThat(cdiBean.isResolvable())
                    .as("should return a resolvable cdi bean for {} class", GroupEntityMetadata.class)
                    .isTrue();
            softly.assertThat(cdiBean.isAmbiguous())
                    .as("should return a non ambiguous bean for {} class", GroupEntityMetadata.class)
                    .isFalse();
            softly.assertThat(cdiBean.getHandle().getBean().getBeanClass())
                    .as("should return a bean class equals to {} class", ReflectionEntityMetadataExtension.CDIGroupEntityMetadata.class)
                    .isEqualTo(ReflectionEntityMetadataExtension.CDIGroupEntityMetadata.class);


            GroupEntityMetadata groupEntityMetadata = cdiBean.get();

            softly.assertThat(groupEntityMetadata)
                    .as("should return an instance of GroupEntityMetadata from CDI")
                    .isNotNull();
            softly.assertThat(groupEntityMetadata.classes())
                    .as("should return non-empty classes's map from the GroupEntityMetadata.classes() method")
                    .isNotEmpty();
            softly.assertThat(groupEntityMetadata.mappings())
                    .as("should return non-empty mapping's map from the GroupEntityMetadata.mappings() method")
                    .isNotEmpty();

        });
    }
}