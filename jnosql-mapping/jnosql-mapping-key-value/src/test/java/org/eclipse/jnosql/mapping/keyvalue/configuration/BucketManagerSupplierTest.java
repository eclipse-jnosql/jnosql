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
 */
package org.eclipse.jnosql.mapping.keyvalue.configuration;

import jakarta.data.exceptions.MappingException;
import org.assertj.core.api.SoftAssertions;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueEntityConverter;
import org.eclipse.jnosql.mapping.keyvalue.MockProducer;
import org.eclipse.jnosql.mapping.keyvalue.spi.KeyValueExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.KEY_VALUE_DATABASE;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.KEY_VALUE_PROVIDER;

@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
class BucketManagerSupplierTest {

    @Inject
    private BucketManagerSupplier supplier;

    @BeforeEach
    void beforeEach(){
        System.clearProperty(KEY_VALUE_PROVIDER.get());
        System.clearProperty(KEY_VALUE_DATABASE.get());
    }

    @Nested
    @DisplayName("When the manager supplier provides managers")
    class WhenTheManagerSupplierProvidesManagers {

        @DisplayName("Should Get Bucket Manager")
        @Test
        void shouldGetBucketManager() {
            System.setProperty(KEY_VALUE_PROVIDER.get(), KeyValueConfigurationMock.class.getName());
            System.setProperty(KEY_VALUE_DATABASE.get(), "database");
            BucketManager manager = supplier.get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(manager).isNotNull();
                softly.assertThat(manager).isInstanceOf(KeyValueConfigurationMock.BucketManagerMock.class);
            });
        }

        @DisplayName("Should Use Default Configuration When Provider Is Wrong")
        @Test
        void shouldUseDefaultConfigurationWhenProviderIsWrong() {
            System.setProperty(KEY_VALUE_PROVIDER.get(), Integer.class.getName());
            System.setProperty(KEY_VALUE_DATABASE.get(), "database");
            BucketManager manager = supplier.get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(manager).isNotNull();
                softly.assertThat(manager).isInstanceOf(KeyValueConfigurationMock2.BucketManagerMock.class);
            });
        }

        @DisplayName("Should Use Default Configuration")
        @Test
        void shouldUseDefaultConfiguration() {
            System.setProperty(KEY_VALUE_DATABASE.get(), "database");
            BucketManager manager = supplier.get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(manager).isNotNull();
                softly.assertThat(manager).isInstanceOf(KeyValueConfigurationMock2.BucketManagerMock.class);
            });
        }

        @DisplayName("Should Return Error When There Is Not Database")
        @Test
        void shouldReturnErrorWhenThereIsNotDatabase() {
            assertThatThrownBy(() -> supplier.get()).isInstanceOf(MappingException.class);
        }

        @DisplayName("Should Close")
        @Test
        void shouldClose(){
            BucketManager manager = Mockito.mock(BucketManager.class);
            supplier.close(manager);
            Mockito.verify(manager).close();
        }

    }

}
