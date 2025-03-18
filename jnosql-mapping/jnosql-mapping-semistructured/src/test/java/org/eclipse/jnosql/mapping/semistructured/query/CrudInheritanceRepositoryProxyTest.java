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
package org.eclipse.jnosql.mapping.semistructured.query;


import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.NoSQLRepository;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.entities.inheritance.EmailNotification;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.eclipse.jnosql.communication.Condition.AND;
import static org.mockito.Mockito.verify;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class CrudInheritanceRepositoryProxyTest {

    private SemiStructuredTemplate template;

    @Inject
    private EntitiesMetadata entities;

    @Inject
    private Converters converters;

    private EmailRepository emailRepository;



    @BeforeEach
    public void setUp() {
        this.template = Mockito.mock(SemiStructuredTemplate.class);

        SemiStructuredRepositoryProxy<EmailNotification, Long> personHandler = new SemiStructuredRepositoryProxy<>(template,
                entities, EmailRepository.class, converters);


        emailRepository = (EmailRepository) Proxy.newProxyInstance(EmailRepository.class.getClassLoader(),
                new Class[]{EmailRepository.class},
                personHandler);
    }

    @Test
    void shouldPutFilterAtFindAll() {
        emailRepository.findAll();
        verify(template).findAll(EmailNotification.class);
    }

    @Test
    void shouldPutFilterAtCount() {
        emailRepository.countBy();
        verify(template).count(EmailNotification.class);
    }

    @Test
    void shouldPutFilterAtFindByName() {
        emailRepository.findByName("name");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).select(captor.capture());
        SelectQuery query = captor.getValue();
        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(AND);
            var conditions = condition.element().get(new TypeReference<List<CriteriaCondition>>() {
            });
            soft.assertThat(conditions).hasSize(2).contains(CriteriaCondition.eq(Element.of("dtype", "Email")));
        });
    }

    @Test
    void shouldPutFilterAtCountByName() {
        emailRepository.countByName("name");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).count(captor.capture());
        SelectQuery query = captor.getValue();
        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(AND);
            var conditions = condition.element().get(new TypeReference<List<CriteriaCondition>>() {
            });
            soft.assertThat(conditions).hasSize(2).contains(CriteriaCondition.eq(Element.of("dtype", "Email")));
        });
    }


    @Test
    void shouldPutFilterAtExistByName() {
        emailRepository.existsByName("name");
        ArgumentCaptor<SelectQuery> captor = ArgumentCaptor.forClass(SelectQuery.class);
        verify(template).exists(captor.capture());
        SelectQuery query = captor.getValue();
        assertSoftly(soft ->{
            soft.assertThat(query.name()).isEqualTo("Notification");
            soft.assertThat(query.condition()).isPresent();
            CriteriaCondition condition = query.condition().orElseThrow();
            soft.assertThat(condition.condition()).isEqualTo(AND);
            var conditions = condition.element().get(new TypeReference<List<CriteriaCondition>>() {
            });
            soft.assertThat(conditions).hasSize(2).contains(CriteriaCondition.eq(Element.of("dtype", "Email")));
        });
    }

    @Test
    void shouldPutFilterAtDeleteAll() {
        emailRepository.deleteAll();
        verify(template).deleteAll(EmailNotification.class);
    }


    public interface EmailRepository extends NoSQLRepository<EmailNotification, String> {

        List<EmailNotification> findByName(String name);

        long countByName(String name);

        boolean existsByName(String name);


    }
}
