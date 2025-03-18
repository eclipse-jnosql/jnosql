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
package org.eclipse.jnosql.mapping.document.configuration;

import jakarta.data.exceptions.MappingException;
import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.document.MockProducer;
import org.eclipse.jnosql.mapping.document.spi.DocumentExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.DOCUMENT_DATABASE;
import static org.eclipse.jnosql.mapping.core.config.MappingConfigurations.DOCUMENT_PROVIDER;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, DocumentExtension.class})
class DocumentManagerSupplierTest {

    @Inject
    private DocumentManagerSupplier supplier;

    @BeforeEach
    void beforeEach(){
        System.clearProperty(DOCUMENT_PROVIDER.get());
        System.clearProperty(DOCUMENT_DATABASE.get());
    }

    @Test
    void shouldGetManager() {
        System.setProperty(DOCUMENT_PROVIDER.get(), DocumentConfigurationMock.class.getName());
        System.setProperty(DOCUMENT_DATABASE.get(), "database");
        DatabaseManager manager = supplier.get();
        Assertions.assertNotNull(manager);
        assertThat(manager).isInstanceOf(DocumentConfigurationMock.DocumentManagerMock.class);
    }


    @Test
    void shouldUseDefaultConfigurationWhenProviderIsWrong() {
        System.setProperty(DOCUMENT_PROVIDER.get(), Integer.class.getName());
        System.setProperty(DOCUMENT_DATABASE.get(), "database");
        DatabaseManager manager = supplier.get();
        Assertions.assertNotNull(manager);
        assertThat(manager).isInstanceOf(DocumentConfigurationMock2.DocumentManagerMock.class);
    }

    @Test
    void shouldUseDefaultConfiguration() {
        System.setProperty(DOCUMENT_DATABASE.get(), "database");
        DatabaseManager manager = supplier.get();
        Assertions.assertNotNull(manager);
        assertThat(manager).isInstanceOf(DocumentConfigurationMock2.DocumentManagerMock.class);
    }

    @Test
    void shouldReturnErrorWhenThereIsNotDatabase() {
        Assertions.assertThrows(MappingException.class, () -> supplier.get());
    }

    @Test
    void shouldClose(){
        DatabaseManager manager = Mockito.mock(DatabaseManager.class);
        supplier.close(manager);
        Mockito.verify(manager).close();
    }
}
