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
 *  Matheus Oliveira
 */
package org.eclipse.jnosql.mapping.semistructured;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.communication.Condition;
import org.eclipse.jnosql.mapping.semistructured.Function;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.UpdateQuery;
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

import static org.mockito.Mockito.when;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
public class MapperUpdateTest {

    @Inject
    private EntityConverter converter;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private DatabaseManager managerMock;

    private DefaultSemiStructuredTemplate template;

    private ArgumentCaptor<UpdateQuery> captor;

    @BeforeEach
    void setUp() {
        managerMock = Mockito.mock(DatabaseManager.class);
        EventPersistManager persistManager = Mockito.mock(EventPersistManager.class);
        Instance<DatabaseManager> instance = Mockito.mock(Instance.class);
        this.captor = ArgumentCaptor.forClass(UpdateQuery.class);
        when(instance.get()).thenReturn(managerMock);
        this.template = new DefaultSemiStructuredTemplate(
                converter, instance, persistManager, entities, converters);
    }

    @Test
    @DisplayName("Should update a single field")
    void shouldUpdateSingleField() {
        template.update(Person.class)
                .set("name").to("Ada")
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(update.name()).isEqualTo("Person");
            soft.assertThat(update.sets()).hasSize(1);
            soft.assertThat(update.sets().getFirst().name()).isEqualTo("name");
            soft.assertThat(update.sets().getFirst().get()).isEqualTo("Ada");
            soft.assertThat(update.where()).isEmpty();
        });
    }

    @Test
    @DisplayName("Should update a multiple field")
    void shouldUpdateMultipleFields() {
        template.update(Person.class)
                .set("name").to("Ada")
                .set("age").to(30)
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(update.name()).isEqualTo("Person");
            soft.assertThat(update.sets()).hasSize(2);
            soft.assertThat(update.sets().getFirst().name()).isEqualTo("name");
            soft.assertThat(update.sets().getFirst().get()).isEqualTo("Ada");
            soft.assertThat(update.sets().get(1).name()).isEqualTo("age");
            soft.assertThat(update.sets().get(1).get()).isEqualTo(30);
            soft.assertThat(update.where()).isEmpty();
        });
    }

    @Test
    @DisplayName("Should update with equality condition")
    void shouldUpdateWithEqCondition() {
        template.update(Person.class)
                .set("name").to("Ada")
                .where("age").eq(30)
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(update.where()).isPresent();
            soft.assertThat(update.sets()).contains(Element.of("name", "Ada"));
            soft.assertThat(update.where().orElseThrow())
                    .isInstanceOf(CriteriaCondition.class);
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).isEqualTo(CriteriaCondition.eq(Element.of("age", 30)));
        });
    }

    @Test
    @DisplayName("Should update with AND condition")
    void shouldUpdateWithAndCondition() {
        template.update(Person.class)
                .set("active").to(true)
                .where("age").gte(18)
                .and("name").eq("Ada")
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(update.where()).isPresent();
            var condition = update.where().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.AND);
            var conditions = condition.element().get(new TypeReference<List<CriteriaCondition>>() {
            });
            soft.assertThat(conditions).hasSize(2);
            soft.assertThat(conditions.get(0)).isEqualTo(CriteriaCondition.gte(Element.of("age", 18)));
            soft.assertThat(conditions.get(1)).isEqualTo(CriteriaCondition.eq(Element.of("name", "Ada")));
        });
    }

    @Test
    @DisplayName("Should update with OR condition")
    void shouldUpdateWithOrCondition() {
        template.update(Person.class)
                .set("active").to(true)
                .where("role").eq("ADMIN")
                .or("role").eq("USER")
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(Condition.OR);
            var conditions = condition.element().get(new TypeReference<List<CriteriaCondition>>() {
            });
            soft.assertThat(conditions).hasSize(2);
            soft.assertThat(conditions.get(0)).isEqualTo(CriteriaCondition.eq(Element.of("role", "ADMIN")));
            soft.assertThat(conditions.get(1)).isEqualTo(CriteriaCondition.eq(Element.of("role", "USER")));
        });
    }

    @Test
    @DisplayName("Should update with NOT condition")
    void shouldUpdateWithNotCondition() {
        template.update(Person.class)
                .set("active").to(false)
                .where("status")
                .not()
                .eq("DELETED")
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).isEqualTo(CriteriaCondition.eq(Element.of("status", "DELETED")).negate());

        });
    }

    @Test
    @DisplayName("Should update with greater-than condition")
    void shouldUpdateWithGtCondition() {
        template.update(Person.class)
                .set("age").to(30)
                .where("age").gt(18)
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).isEqualTo(CriteriaCondition.gt(Element.of("age", 18)));
        });
    }

    @Test
    @DisplayName("Should update with greater-than-or-equal condition")
    void shouldUpdateWithGteCondition() {
        template.update(Person.class)
                .set("age").to(30)
                .where("age").gte(18)
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).isEqualTo(CriteriaCondition.gte(Element.of("age", 18)));
        });
    }

    @Test
    @DisplayName("Should update with less-than condition")
    void shouldUpdateWithLtCondition() {
        template.update(Person.class)
                .set("age").to(30)
                .where("age").lt(65)
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).isEqualTo(CriteriaCondition.lt(Element.of("age", 65)));
        });
    }

    @Test
    @DisplayName("Should update with less-than-or-equal condition")
    void shouldUpdateWithLteCondition() {
        template.update(Person.class)
                .set("age").to(30)
                .where("age").lte(65)
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).isEqualTo(CriteriaCondition.lte(Element.of("age", 65)));
        });
    }

    @Test
    @DisplayName("Should update with IN condition")
    void shouldUpdateWithInCondition() {
        template.update(Person.class)
                .set("status").to("ACTIVE")
                .where("status").in(List.of("ACTIVE", "PENDING"))
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).isEqualTo(CriteriaCondition.in(Element.of("status", List.of("ACTIVE", "PENDING"))));
        });
    }

    @Test
    @DisplayName("Should update with BETWEEN condition")
    void shouldUpdateWithBetweenCondition() {
        template.update(Person.class)
                .set("age").to(30)
                .where("age").between(18, 65)
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).isEqualTo(CriteriaCondition.between(Element.of("age", List.of(18, 65))));
        });
    }

    @Test
    @DisplayName("Should update with LIKE condition")
    void shouldUpdateWithLikeCondition() {
        template.update(Person.class)
                .set("name").to("Ada")
                .where("name").like("Ad%")
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).isEqualTo(CriteriaCondition.like(Element.of("name", "Ad%")));
        });
    }

    @Test
    @DisplayName("Should update with CONTAINS condition")
    void shouldUpdateWithContainsCondition() {
        template.update(Person.class)
                .set("name").to("Ada")
                .where("name").contains("d")
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).isEqualTo(CriteriaCondition.contains(Element.of("name", "d")));
        });
    }

    @Test
    @DisplayName("Should update with STARTS WITH condition")
    void shouldUpdateWithStartsWithCondition() {
        template.update(Person.class)
                .set("name").to("Ada")
                .where("name").startsWith("A")
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).isEqualTo(CriteriaCondition.startsWith(Element.of("name", "A")));
        });
    }

    @Test
    @DisplayName("Should update with ENDS WITH condition")
    void shouldUpdateWithEndsWithCondition() {
        template.update(Person.class)
                .set("name").to("Ada")
                .where("name").endsWith("a")
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).isEqualTo(CriteriaCondition.endsWith(Element.of("name", "a")));
        });
    }

    @Test
    @DisplayName("Should update with UPPER function in where clause")
    void shouldUpdateWhereFunctionUpper() {
        template.update(Person.class)
                .set("name").to("Ada")
                .where(Function.upper("name")).eq("ADA")
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).as("condition matches UPPER(name) = ADA")
                    .isEqualTo(CriteriaCondition.eq(Element.of("UPPER(name)", "ADA")));
        });
    }

    @Test
    @DisplayName("Should update with LEFT function in where clause")
    void shouldUpdateWhereFunctionLeft() {
        template.update(Person.class)
                .set("name").to("Ada")
                .where(Function.left("name", 2)).eq("Ad")
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition).as("condition matches LEFT(name, 2) = Ad")
                    .isEqualTo(CriteriaCondition.eq(Element.of("LEFT(name, 2)", "Ad")));
        });
    }

    @Test
    @DisplayName("Should update with LOWER function in AND condition")
    void shouldUpdateAndFunctionLower() {
        template.update(Person.class)
                .set("name").to("Ada")
                .where("age").gt(10).and(Function.lower("name")).eq("ada")
                .execute();

        Mockito.verify(managerMock).update(captor.capture());
        var update = captor.getValue();

        SoftAssertions.assertSoftly(soft -> {
            var condition = update.where().orElseThrow();
            soft.assertThat(condition.condition()).as("condition is AND").isEqualTo(Condition.AND);
        });
    }

}
