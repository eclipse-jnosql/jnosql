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
package org.eclipse.jnosql.mapping.semistructured.repository.entities;

import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.By;
import jakarta.data.repository.Find;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Select;

import java.util.List;

@Repository
public interface PhotoSocialMediaRepository extends BasicRepository<PhotoSocialMedia, Long> {

    List<ComicBook> findByName(String name);

    long countAll();

    long countByName(String name);

    boolean existsByName(String name);

    boolean existsBy();

    long countBy();

    void deleteByName(String name);

    void deleteBy();

    List<SocialMediaSummary> findByNameAndPhotoId(String name, String photoId);

    @Find
    @Select("id")
    @Select("name")
    List<SocialMediaSummary> find(@By("name") String name);

    @Query("SELECT id, name from PhotoSocialMedia where name = :name")
    List<SocialMediaSummary> query(@Param("name") String name);
}
