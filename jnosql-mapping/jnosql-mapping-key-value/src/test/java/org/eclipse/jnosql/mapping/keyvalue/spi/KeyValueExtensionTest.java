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
package org.eclipse.jnosql.mapping.keyvalue.spi;

import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueEntityConverter;
import org.eclipse.jnosql.mapping.keyvalue.KeyValueTemplate;
import org.eclipse.jnosql.mapping.keyvalue.MockProducer;
import org.eclipse.jnosql.mapping.keyvalue.entities.Person;
import org.eclipse.jnosql.mapping.keyvalue.entities.User;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
@AddPackages(Reflections.class)
class KeyValueExtensionTest {

    @Inject
    private KeyValueTemplate template;

    @Inject
    @Database(value = DatabaseType.KEY_VALUE, provider = "keyvalueMock")
    private KeyValueTemplate templateMock;

    @Inject
    private UserRepository userRepository;

    @Inject
    @Database(value = DatabaseType.KEY_VALUE)
    private UserRepository userRepositoryDefault;

    @Inject
    @Database(value = DatabaseType.KEY_VALUE, provider = "keyvalueMock")
    private UserRepository userRepositoryMock;

    @Nested
    @DisplayName("When the extension discovers beans")
    class WhenTheExtensionDiscoversBeans {

        @DisplayName("Should Use Mock")
        @Test
        void shouldUseMock() {
            Person person = template.get(10L, Person.class).get();

            Person personMock = templateMock.get(10L, Person.class).get();

            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(person.getName()).isEqualTo("Default");
                softly.assertThat(personMock.getName()).isEqualTo("keyvalueMock");
            });

        }

        @DisplayName("Should Use Repository")
        @Test
        void shouldUseRepository() {
            User user = userRepository.findById("user").get();
            User userDefault = userRepositoryDefault.findById("user").get();
            User userMock = userRepositoryMock.findById("user").get();
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(user.getName()).isEqualTo("Default");
                softly.assertThat(userDefault.getName()).isEqualTo("Default");
                softly.assertThat(userMock.getName()).isEqualTo("keyvalueMock");
            });
        }

    }

}
