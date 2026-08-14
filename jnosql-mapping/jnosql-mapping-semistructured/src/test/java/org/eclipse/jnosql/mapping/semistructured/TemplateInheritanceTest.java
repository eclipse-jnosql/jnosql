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
import java.util.List;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
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

    @DisplayName("Should select filter")
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

    @DisplayName("Should select no filter")
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

    @DisplayName("Should delete filter")
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

    @DisplayName("Should delete no filter")
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

    @DisplayName("Should select filter condition")
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

    @DisplayName("Should delete filter condition")
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

    @DisplayName("Should count all filter")
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

    @DisplayName("Should find all filter")
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

    @DisplayName("Should delete all filter")
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


    @DisplayName("Should count all no filter")
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

    @DisplayName("Should find all no filter")
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
    @DisplayName("Should delete all no filter")
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

    @DisplayName("Should query generically")
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

    @DisplayName("Should query with specialization")
    @Test
    void shouldQueryWithSpecialization() {
        var prepare = template.prepare("FROM EmailNotification WHERE email = 'email@gmail.com'");
        prepare.result();

        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(this.managerMock).select(captor.capture());
        var query = captor.getValue();
        assertSoftly(soft -> soft.assertThat(query.name()).isEqualTo("Notification"));
    }

    @DisplayName("Should query with specialization with condition")
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

    @DisplayName("Should query with specialization with condition append")
    @Test
    void shouldQueryWithSpecializationWithConditionAppend() {
        var prepare = template.prepare("FROM EmailNotification WHERE email = 'email@email'");
        prepare.result();

        var captor = ArgumentCaptor.forClass(SelectQuery.class);
        Mockito.verify(this.managerMock).select(captor.capture());
        var query = captor.getValue();
        assertSoftly(soft -> {
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);
            var conditions = condition.element().get(new TypeReference<List<CriteriaCondition>>() {
            });
            soft.assertThat(conditions.getFirst().condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(conditions.getFirst().element()).isEqualTo(Element.of("email", "email@email"));

            soft.assertThat(conditions.get(1).condition()).isEqualTo(Condition.EQUALS);
            soft.assertThat(conditions.get(1).element()).isEqualTo(Element.of("dtype", "Email"));
        });
    }


    @Nested
    @DisplayName("When the template inheritance is tested")
    class WhenTheTemplateInheritanceIsTested {
    }
}
