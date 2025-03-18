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
package org.eclipse.jnosql.mapping.semistructured.query;

import jakarta.inject.Inject;
import org.eclipse.jnosql.communication.Params;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.query.SelectQuery;
import org.eclipse.jnosql.communication.query.method.SelectMethodProvider;
import org.eclipse.jnosql.communication.semistructured.CriteriaCondition;
import org.eclipse.jnosql.communication.semistructured.Element;
import org.eclipse.jnosql.communication.semistructured.QueryParams;
import org.eclipse.jnosql.communication.semistructured.SelectQueryParser;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.semistructured.EntityConverter;
import org.eclipse.jnosql.mapping.semistructured.MockProducer;
import org.eclipse.jnosql.mapping.semistructured.entities.Person;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.eclipse.jnosql.mapping.core.util.ParamsBinder;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableAutoWeld
@AddPackages(value = {Converters.class, EntityConverter.class})
@AddPackages(MockProducer.class)
@AddPackages(Reflections.class)
@AddExtensions({ReflectionEntityMetadataExtension.class})
class ParamsBinderTest {


    @Inject
    private EntitiesMetadata mappings;

    @Inject
    private Converters converters;

    private ParamsBinder paramsBinder;

    @Test
    void shouldConvert() {

        Method method = Stream.of(PersonRepository.class.getMethods())
                .filter(m -> m.getName().equals("findByAge")).findFirst().get();
        EntityMetadata entityMetadata = mappings.get(Person.class);
        RepositorySemiStructuredObserverParser parser = new RepositorySemiStructuredObserverParser(entityMetadata);
        paramsBinder = new ParamsBinder(entityMetadata, converters);

        SelectMethodProvider selectMethodFactory = SelectMethodProvider.INSTANCE;
        SelectQuery selectQuery = selectMethodFactory.apply(method, entityMetadata.name());
        SelectQueryParser queryParser = new SelectQueryParser();
        QueryParams columnQueryParams = queryParser.apply(selectQuery, parser);
        Params params = columnQueryParams.params();
        Object[] args = {10};
        paramsBinder.bind(params, args, method);
        var query = columnQueryParams.query();
        CriteriaCondition columnCondition = query.condition().get();
        Value value = columnCondition.element().value();
        assertEquals(10, value.get());

    }

    @Test
    void shouldConvert2() {

        Method method = Stream.of(PersonRepository.class.getMethods())
                .filter(m -> m.getName().equals("findByAgeAndName")).findFirst().get();
        EntityMetadata entityMetadata = mappings.get(Person.class);
        RepositorySemiStructuredObserverParser parser = new RepositorySemiStructuredObserverParser(entityMetadata);
        paramsBinder = new ParamsBinder(entityMetadata, converters);

        SelectMethodProvider selectMethodFactory = SelectMethodProvider.INSTANCE;
        SelectQuery selectQuery = selectMethodFactory.apply(method, entityMetadata.name());
        SelectQueryParser queryParser = new SelectQueryParser();
        var queryParams = queryParser.apply(selectQuery, parser);
        Params params = queryParams.params();
        paramsBinder.bind(params, new Object[]{10L, "Ada"}, method);
        var query = queryParams.query();
        var columnCondition = query.condition().get();
        List<CriteriaCondition> conditions = columnCondition.element().get(new TypeReference<>() {
        });
        List<Object> values = conditions.stream().map(CriteriaCondition::element)
                .map(Element::value)
                .map(Value::get).toList();
        assertEquals(10, values.get(0));
        assertEquals("Ada", values.get(1));

    }


    interface PersonRepository {

        List<Person> findByAge(Integer age);

        List<Person> findByAgeAndName(Long age, String name);
    }


}
