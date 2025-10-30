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
package org.eclipse.jnosql.mapping.semistructured;


import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.nosql.Query;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
public class QueryTest {

    @Inject
    private EntityConverter converter;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private DatabaseManager managerMock;

    private DefaultSemiStructuredTemplate template;

    private ArgumentCaptor<SelectQuery> captor;

    @BeforeEach
    public void setUp() {
        managerMock = Mockito.mock(DatabaseManager.class);
        EventPersistManager persistManager = Mockito.mock(EventPersistManager.class);
        Instance<DatabaseManager> instance = Mockito.mock(Instance.class);
        this.captor = ArgumentCaptor.forClass(SelectQuery.class);
        when(instance.get()).thenReturn(managerMock);
        this.template = new DefaultSemiStructuredTemplate(converter, instance,
                persistManager, entities, converters);
    }

    @Test
    @DisplayName("Should execute a simple query using From with List")
    void shouldSelectFrom(){
        Query query = this.template.query("FROM Person");
        query.result();

        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.condition()).isEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }

    @Test
    @DisplayName("Should execute a simple query using From with Stream")
    void shouldSelectFromStream(){
        Query query = this.template.query("FROM Person WHERE name = 'Ada'");
        query.stream();

        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }

    @Test
    @DisplayName("Should execute a simple query using From with Single Result")
    void shouldSelectFromSingleResult(){
        Query query = this.template.query("FROM Person WHERE name = 'Ada' ORDER BY name");
        query.stream();

        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }

    @Test
    @DisplayName("Should execute a simple query using From with Single Result")
    void shouldSelectFromSingleResultAsCount(){
        Mockito.when(managerMock.count(Mockito.any(SelectQuery.class))).thenReturn(1L);
        Query query = this.template.query("SELECT count(this) FROM Person WHERE name = 'Ada' ORDER BY name");
        Optional<Long> count = query.singleResult();

        Mockito.verify(managerMock).select(captor.capture());
        SelectQuery selectQuery = captor.getValue();

        SoftAssertions.assertSoftly(soft ->{
            soft.assertThat(count).isNotEmpty().get().isEqualTo(1L);
            soft.assertThat(selectQuery.name()).isEqualTo("Person");
            soft.assertThat(selectQuery.condition()).isNotEmpty();
            soft.assertThat(selectQuery.sorts()).isEmpty();
            soft.assertThat(selectQuery.isCount()).isFalse();
        });
    }


}
