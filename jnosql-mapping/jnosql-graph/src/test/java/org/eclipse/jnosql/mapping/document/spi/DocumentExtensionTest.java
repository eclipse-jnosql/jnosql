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
package org.eclipse.jnosql.mapping.document.spi;

import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.document.DocumentTemplate;
import org.eclipse.jnosql.mapping.document.MockProducer;
import org.eclipse.jnosql.mapping.document.entities.Person;
import org.eclipse.jnosql.mapping.document.entities.PersonRepository;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.core.spi.EntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class, DocumentTemplate.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({EntityMetadataExtension.class, DocumentExtension.class})
class DocumentExtensionTest {


    @Inject
    @Database(value = DatabaseType.DOCUMENT)
    private PersonRepository repository;

    @Inject
    @Database(value = DatabaseType.DOCUMENT, provider = "documentRepositoryMock")
    private PersonRepository repositoryMock;

    @Inject
    @Database(value = DatabaseType.DOCUMENT, provider = "documentRepositoryMock")
    private DocumentTemplate templateMock;

    @Inject
    private DocumentTemplate template;

    @Test
    void shouldInitiate() {
        assertNotNull(repository);
        Person person = repository.save(Person.builder().build());
        assertEquals("Default", person.getName());
    }

    @Test
    void shouldUseMock(){
        assertNotNull(repositoryMock);
        Person person = repositoryMock.save(Person.builder().build());
        assertEquals("documentRepositoryMock", person.getName());
    }

    @Test
    void shouldInjectTemplate() {
        assertNotNull(templateMock);
        assertNotNull(template);
    }

    @Test
    void shouldInjectRepository() {
        assertNotNull(repository);
        assertNotNull(repositoryMock);
    }
}
