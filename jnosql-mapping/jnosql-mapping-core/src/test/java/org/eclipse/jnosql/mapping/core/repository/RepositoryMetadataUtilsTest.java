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
package org.eclipse.jnosql.mapping.core.repository;

import jakarta.data.Limit;
import jakarta.data.page.PageRequest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.mapping.core.Converters;
import org.eclipse.jnosql.mapping.core.VetedConverter;
import org.eclipse.jnosql.mapping.core.entities.People;
import org.eclipse.jnosql.mapping.metadata.repository.NameKey;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoriesMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMetadata;
import org.eclipse.jnosql.mapping.metadata.repository.RepositoryMethod;
import org.eclipse.jnosql.mapping.reflection.Reflections;
import org.eclipse.jnosql.mapping.reflection.spi.ReflectionEntityMetadataExtension;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.AddPackages;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@EnableAutoWeld
@AddPackages(value = Converters.class)
@AddPackages(value = VetedConverter.class)
@AddPackages(value = Reflections.class)
@AddExtensions(ReflectionEntityMetadataExtension.class)
class RepositoryMetadataUtilsTest {

    @Inject
    private RepositoriesMetadata repositoriesMetadata;

    private RepositoryMetadata repositoryMetadata;

    @BeforeEach
    void setUp() {
        repositoryMetadata = repositoriesMetadata.get(People.class)
                .orElseThrow();
    }

    @Test
    @DisplayName("should map param empty")
    void shouldMapParamEmpty() {
        RepositoryMethod method = repositoryMetadata.find(new NameKey("query")).orElseThrow();
        var params = RepositoryMetadataUtils.INSTANCE.getParams(method, new Object[]{});
        Assertions.assertThat(params).isEmpty();
    }

    @Test
    @DisplayName("should map param empty with special parameter")
    void shouldMapParamEmptyWithParameter() {
        RepositoryMethod method = repositoryMetadata.find(new NameKey("query0")).orElseThrow();
        var params = RepositoryMetadataUtils.INSTANCE.getParams(method, new Object[]{
                Limit.of(10),
                PageRequest.ofSize(10)});

        Assertions.assertThat(params).isEmpty();
    }

    @Test
    @DisplayName("should map params by name")
    void shouldMapParamsByName() {
        RepositoryMethod method = repositoryMetadata.find(new NameKey("query1")).orElseThrow();
        var params = RepositoryMetadataUtils.INSTANCE.getParams(method, new Object[]{
                "John",
                PageRequest.ofSize(10)});

        Assertions.assertThat(params)
                .hasSize(2)
                .containsEntry("native", "John")
                .containsEntry("?1", "John");
    }



    @Test
    @DisplayName("should map params by name")
    void shouldMapParamsByName2() {
        RepositoryMethod method = repositoryMetadata.find(new NameKey("query2")).orElseThrow();
        var params = RepositoryMetadataUtils.INSTANCE.getParams(method, new Object[]{
                "John",
                PageRequest.ofSize(10)});

        Assertions.assertThat(params)
                .hasSize(2)
                .containsEntry("arg0", "John")
                .containsEntry("?1", "John");
    }

    @Test
    @DisplayName("should map params with multiple params")
    void shouldMapParamsMultipleParams() {
        RepositoryMethod method = repositoryMetadata.find(new NameKey("query3")).orElseThrow();
        var params = RepositoryMetadataUtils.INSTANCE.getParams(method, new Object[]{
                "John",
                25,
                PageRequest.ofSize(10)});

        Assertions.assertThat(params)
                .hasSize(4)
                .containsEntry("name", "John")
                .containsEntry("?1", "John")
                .containsEntry("?2", 25);
    }

}