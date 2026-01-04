/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.PreparedStatement;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.EmailNotification;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.Notification;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class TemplateInheritanceTest {

    @Inject
    private EntityConverter converter;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private DatabaseManager managerMock;

    private DefaultSemiStructuredTemplate template;


    @BeforeEach
    void setUp() {
        managerMock = Mockito.mock(DatabaseManager.class);
        var documentEventPersistManager = Mockito.mock(EventPersistManager.class);

        Instance<DatabaseManager> instance = Mockito.mock(Instance.class);
        when(instance.get()).thenReturn(managerMock);
        this.template = new DefaultSemiStructuredTemplate(converter, instance,
                documentEventPersistManager, entities, converters);
    }

    @Test
    void shouldSelectFilter(){
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        template.select(EmailNotification.class).<EmailNotification>stream().toList();
        Mockito.verify(this.managerMock).select(captor.capture());
        var query = captor.getValue();
        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element()).isEqualTo(Element.of("dtype", "Email"));
        });
    }

    @Test
    void shouldSelectNoFilter(){
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        template.select(Notification.class).<Notification>stream().toList();
        Mockito.verify(this.managerMock).select(captor.capture());
        var query = captor.getValue();
        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isEmpty();
        });
    }

    @Test
    void shouldDeleteFilter(){
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        template.delete(EmailNotification.class).execute();
        Mockito.verify(this.managerMock).delete(captor.capture());
        var query = captor.getValue();
        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element()).isEqualTo(Element.of("dtype", "Email"));
        });
    }

    @Test
    void shouldDeleteNoFilter(){
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        template.delete(Notification.class).execute();
        Mockito.verify(this.managerMock).delete(captor.capture());
        var query = captor.getValue();
        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isEmpty();
        });
    }

    @Test
    void shouldSelectFilterCondition(){
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        template.select(EmailNotification.class).where("name")
                .eq("notification").<EmailNotification>stream().toList();
        Mockito.verify(this.managerMock).select(captor.capture());
        var query = captor.getValue();
        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);
            var documents = condition.element().get(new TypeReference<List<CriteriaCondition>>() {});
            soft.assertThat(documents).contains(CriteriaCondition.eq(Element.of("dtype", "Email")),
                    CriteriaCondition.eq(Element.of("name", "notification")));
        });
    }

    @Test
    void shouldDeleteFilterCondition(){
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        template.delete(EmailNotification.class).where("name")
                .eq("notification").execute();
        Mockito.verify(this.managerMock).delete(captor.capture());
        var query = captor.getValue();
        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);
            var documents = condition.element().get(new TypeReference<List<CriteriaCondition>>() {});
            soft.assertThat(documents).contains(CriteriaCondition.eq(Element.of("dtype", "Email")),
                    CriteriaCondition.eq(Element.of("name", "notification")));
        });
    }

    @Test
    void shouldCountAllFilter(){
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        template.count(EmailNotification.class);
        Mockito.verify(this.managerMock).count(captor.capture());
        var query = captor.getValue();

        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element()).isEqualTo(Element.of("dtype", "Email"));
        });
    }

    @Test
    void shouldFindAllFilter(){
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        template.findAll(EmailNotification.class);
        Mockito.verify(this.managerMock).select(captor.capture());
        var query = captor.getValue();

        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element()).isEqualTo(Element.of("dtype", "Email"));
        });
    }

    @Test
    void shouldDeleteAllFilter(){
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        template.deleteAll(EmailNotification.class);
        Mockito.verify(this.managerMock).delete(captor.capture());
        var query = captor.getValue();

        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element()).isEqualTo(Element.of("dtype", "Email"));
        });
    }


    @Test
    void shouldCountAllNoFilter(){
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        template.count(Notification.class);
        Mockito.verify(this.managerMock).count(captor.capture());
        var query = captor.getValue();

        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isEmpty();
        });
    }

    @Test
    void shouldFindAllNoFilter(){
        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        template.findAll(Notification.class);
        Mockito.verify(this.managerMock).select(captor.capture());
        var query = captor.getValue();

        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isEmpty();
        });
    }
    @Test
    void shouldDeleteAllNoFilter(){
        var captor = ArgumentCaptor.forClass(DeleteQuery.class);
        template.deleteAll(Notification.class);
        Mockito.verify(this.managerMock).delete(captor.capture());
        var query = captor.getValue();

        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isEmpty();
        });
    }

    @Test
    void shouldQueryGenerically() {
        PreparedStatement prepare = template.prepare("FROM Notification");
        prepare.result();

        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(this.managerMock).select(captor.capture());
        var query = captor.getValue();
        assertSoftly(soft -> {
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isEmpty();
        });
    }

    @Test
    void shouldQueryWithSpecialization() {
        var prepare = template.prepare("FROM EmailNotification WHERE email = 'email@gmail.com'");
        prepare.result();

        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(this.managerMock).select(captor.capture());
        var query = captor.getValue();
        assertSoftly(soft -> soft.assertThat(query.name()).isEqualTo("Notification"));
    }

    @Test
    void shouldQueryWithSpecializationWithCondition() {
        PreparedStatement prepare = template.prepare("FROM EmailNotification");
        prepare.result();

        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(this.managerMock).select(captor.capture());
        var query = captor.getValue();
        assertSoftly(soft -> {
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(condition.element()).isEqualTo(Element.of("dtype", "Email"));
        });
    }

}
