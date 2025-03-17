/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.mapping.reflection;

import jakarta.nosql.Convert;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.jnosql.mapping.metadata.ClassConverter;
import org.eclipse.jnosql.mapping.metadata.ConstructorBuilder;
import org.eclipse.jnosql.mapping.metadata.ConstructorMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.BookUser;
import org.eclipse.jnosql.mapping.reflection.entities.constructor.Counter;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@EnableAutoWeld
@AddPackages(value = Convert.class)
@AddPackages(value = DefaultGroupEntityMetadata.class)
class DefaultConstructorBuilderTest {

    private ConstructorMetadata constructor;
    private ConstructorMetadata counterConstructor;

    @BeforeEach
    void setUp(){
        ClassConverter converter = new ReflectionClassConverter();
        EntityMetadata entityMetadata = converter.apply(BookUser.class);
        this.constructor = entityMetadata.constructor();
        
        EntityMetadata counterMetadata = converter.apply(Counter.class);
        this.counterConstructor = counterMetadata.constructor();
    }

    @Test
    void shouldToString(){
        ConstructorBuilder builder = DefaultConstructorBuilder.of(constructor);
        assertThat(builder.toString()).isNotEmpty().isNotBlank().isNotNull();
    }

    @Test
    void shouldCreateEmpty(){
        ConstructorBuilder builder = DefaultConstructorBuilder.of(constructor);
        builder.addEmptyParameter();
        builder.addEmptyParameter();
        builder.addEmptyParameter();
        builder.addEmptyParameter();
        BookUser user = builder.build();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(user.getName()).isNull();
            soft.assertThat(user.getNickname()).isNull();
            soft.assertThat(user.getBooks()).isNull();
        });
    }


    @Test
    void shouldCreateWithValues(){
        ConstructorBuilder builder = DefaultConstructorBuilder.of(constructor);
        builder.add("id");
        builder.add("name");
        builder.addEmptyParameter();
        builder.addEmptyParameter();

        BookUser user = builder.build();

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(user.getNickname()).isEqualTo("id");
            soft.assertThat(user.getName()).isEqualTo("name");
            soft.assertThat(user.getBooks()).isNull();
        });
    }

    @Test
    void shouldParameters(){
        ConstructorBuilder builder = DefaultConstructorBuilder.of(constructor);
        builder.add("id");
        builder.add("name");
        builder.addEmptyParameter();

        assertThat(builder.parameters()).hasSize(4);
    }

    @Test
    void shouldEqualsHashCode(){
        ConstructorBuilder builder = DefaultConstructorBuilder.of(constructor);
        ConstructorBuilder other = DefaultConstructorBuilder.of(constructor);
        assertThat(builder).isEqualTo(other);
        assertThat(builder).hasSameHashCodeAs(other);
    }
    
    @Test
    void shouldHandleEmptyPrimitives() {
        ConstructorBuilder builder = DefaultConstructorBuilder.of(counterConstructor);
    	builder.addEmptyParameter();
    	builder.addEmptyParameter();
    	builder.addEmptyParameter();
    	builder.addEmptyParameter();
    	Counter counter = builder.build();
    	assertThat(counter.count()).isEqualTo(0);
    	assertThat(counter.active()).isFalse();
    	assertThat(counter.ratio()).isEqualTo(0d);
    	assertThat(counter.code()).isEqualTo((char)0);
    }
}
