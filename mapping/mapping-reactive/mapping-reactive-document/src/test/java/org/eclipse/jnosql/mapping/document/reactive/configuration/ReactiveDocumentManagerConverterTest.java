/*
 *  Copyright (c) 2020 Otávio Santana and others
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *  You may elect to redistribute this code under either of these licenses.
 *  Contributors:
 *  Otavio Santana
 */
package org.eclipse.jnosql.mapping.document.reactive.configuration;

import jakarta.nosql.tck.test.CDIExtension;
import org.eclipse.jnosql.mapping.document.reactive.ReactiveDocumentTemplate;
import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.UUID;

@CDIExtension
class ReactiveDocumentManagerConverterTest {

    @Inject
    private Config config;

    @Test
    public void shouldReturnTemplate() {
        final String prefix = UUID.randomUUID().toString();
        System.setProperty(prefix, prefix);
        System.setProperty(prefix + ".settings.key", "value");
        System.setProperty(prefix + ".settings.key2", "value2");
        System.setProperty(prefix + ".provider", DocumentConfigurationMock.class.getName());
        System.setProperty(prefix + ".database", "database");
        final ReactiveDocumentTemplate template = config.getValue(prefix, ReactiveDocumentTemplate.class);
        Assertions.assertNotNull(template);
        System.clearProperty(prefix);
        System.clearProperty(prefix + ".settings.key");
        System.clearProperty(prefix + ".settings.key2");
        System.clearProperty(prefix + ".provider");
        System.clearProperty(prefix + ".database");
    }

}