/*
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
 */
package org.eclipse.jnosql.mapping.keyvalue;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.nosql.Query;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.keyvalue.entities.User;
import org.eclipse.jnosql.mapping.keyvalue.spi.KeyValueExtension;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, KeyValueEntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class, KeyValueExtension.class})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class QueryTemplateTest {

    @Inject
    private KeyValueEntityConverter converter;

    @Inject
    private KeyValueEventPersistManager eventManager;

    @Mock
    private BucketManager manager;

    private KeyValueTemplate template;


    @BeforeEach
    void setUp() {
        Instance<BucketManager> instance = Mockito.mock(Instance.class);
        when(instance.get()).thenReturn(manager);
        this.template = new DefaultKeyValueTemplate(converter, instance, eventManager);
    }

    @Test
    @DisplayName("Should return error when query is null")
    void shouldReturnErrorOnQueryThatIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> template.query(null));
    }

    @Test
    @DisplayName("Should return error when update query")
    void shouldReturnErrorOnUpdateQuery() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.query("UPDATE User set name = 'Otavio' where id = 123"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT name, age FROM User", "FROM User", "From User skip 10", "From User limit 10", "From User ORDER BY name",
            "From User ORDER BY name DESC", "From User ORDER BY name ASC"})
    void shouldErrorWhenSelectIsNotSupportKeyValue(String text) {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.query(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {"FROM User where name = 'Ada'",
            "FROM User where age > 10",
            "FROM User where age < 10",
            "FROM User where age <= 10",
            "FROM User where name like 'Otavio'"})
    void shouldErrorWhenAttributeIsNotId(String text) {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> template.query(text));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SELECT * FROM User nickname ='Ada'", "FROM User nickname ='Ada'"})
    void shouldFindByIdWithoutParameter(String text) {
        var query = template.query(text);
        Optional<User> user = query.singleResult();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(user).isPresent();
            soft.assertThat(user.orElseThrow().getNickname()).isEqualTo("Ada");
            Mockito.verify(manager).get("Ada");
        });
    }

}
