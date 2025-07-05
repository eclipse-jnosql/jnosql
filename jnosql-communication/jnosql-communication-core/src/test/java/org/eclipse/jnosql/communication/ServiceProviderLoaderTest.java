/*
 *
 *  Copyright (c) 2025 Contributors to the Eclipse Foundation
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
 *
 */
package org.eclipse.jnosql.communication;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceProviderLoaderTest {


    interface TestService {
        String getValue();
    }

    public static class ServiceProviderLoaderTestService implements TestService {
        public String getValue() {
            return "Hello";
        }
    }

    @Test
    void shouldLoadSingleInstance() {
        Supplier<TestService> singleton = ServiceProviderLoader.lazySingleton(
                TestService.class,
                () -> new IllegalStateException("No SPI found")
        );

        TestService instance = singleton.get();
        assertNotNull(instance);
        assertEquals("Hello", instance.getValue());
    }

    @Test
    void shouldLoadListOfInstances() {
        Supplier<List<TestService>> listSupplier = ServiceProviderLoader.lazyList(TestService.class);

        List<TestService> services = listSupplier.get();
        assertFalse(services.isEmpty());
        assertEquals("Hello", services.get(0).getValue());
    }

    @Test
    void shouldFailWhenNoImplementationFound() {
        Supplier<Object> supplier = ServiceProviderLoader.lazySingleton(
                Object.class,
                () -> new IllegalStateException("Not found")
        );
        assertThrows(IllegalStateException.class, supplier::get);
    }
}