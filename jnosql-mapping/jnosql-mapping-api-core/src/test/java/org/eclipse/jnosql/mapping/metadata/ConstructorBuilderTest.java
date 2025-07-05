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
package org.eclipse.jnosql.mapping.metadata;

import jakarta.nosql.NoSQLException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConstructorBuilderTest {

    @Test
    void shouldReturnValidConstructorBuilder() {
        ConstructorMetadata constructorMetadata = mock(ConstructorMetadata.class);
        ConstructorBuilderSupplier supplier = mock(ConstructorBuilderSupplier.class);
        ConstructorBuilder expectedBuilder = mock(ConstructorBuilder.class);

        when(supplier.apply(constructorMetadata)).thenReturn(expectedBuilder);

        // Mock static method
        try (MockedStatic<ServiceLoader> serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {
            ServiceLoader<ConstructorBuilderSupplier> serviceLoader = mock(ServiceLoader.class);
            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(ConstructorBuilderSupplier.class)).thenReturn(serviceLoader);
            when(serviceLoader.findFirst()).thenReturn(Optional.of(supplier));

            ConstructorBuilder builder = ConstructorBuilder.of(constructorMetadata);

            assertSame(expectedBuilder, builder);
            verify(supplier, times(1)).apply(constructorMetadata);
        }
    }

    @Test
    void shouldThrowExceptionWhenNoImplementationFound() {
        ConstructorMetadata constructorMetadata = mock(ConstructorMetadata.class);

        // Mock static method to return an empty ServiceLoader
        try (MockedStatic<ServiceLoader> serviceLoaderMockedStatic = mockStatic(ServiceLoader.class)) {
            ServiceLoader<ConstructorBuilderSupplier> serviceLoader = mock(ServiceLoader.class);
            serviceLoaderMockedStatic.when(() -> ServiceLoader.load(ConstructorBuilderSupplier.class)).thenReturn(serviceLoader);
            when(serviceLoader.findFirst()).thenReturn(Optional.empty());

            assertThrows(NoSQLException.class, () -> ConstructorBuilder.of(constructorMetadata));
        }
    }
}