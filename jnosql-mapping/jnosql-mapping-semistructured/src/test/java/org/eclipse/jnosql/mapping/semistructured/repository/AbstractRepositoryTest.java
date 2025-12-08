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
package org.eclipse.jnosql.mapping.semistructured.repository;

import org.eclipse.jnosql.communication.semistructured.DeleteQuery;
import org.eclipse.jnosql.communication.semistructured.SelectQuery;
import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBookBookStore;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBookRepository;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.PhotoSocialMediaRepository;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.VideoSocialMediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

abstract class AbstractRepositoryTest {

    protected SemiStructuredTemplate template;
    protected ComicBookRepository comicBookRepository;

    protected ComicBookBookStore bookStore;

    protected PhotoSocialMediaRepository photoSocialMediaRepository;

    protected VideoSocialMediaRepository videoSocialMediaRepository;

    protected ArgumentCaptor<SelectQuery> selectQueryCaptor;
    protected ArgumentCaptor<DeleteQuery> deleteQueryCaptor;



    @BeforeEach
    void setUP() {
        this.template = Mockito.mock(SemiStructuredTemplate.class);
        this.comicBookRepository = producer().get(ComicBookRepository.class, template);
        this.bookStore = producer().get(ComicBookBookStore.class, template);
        this.photoSocialMediaRepository = producer().get(PhotoSocialMediaRepository.class, template);
        this.videoSocialMediaRepository = producer().get(VideoSocialMediaRepository.class, template);
        this.selectQueryCaptor = ArgumentCaptor.forClass(SelectQuery.class);
        this.deleteQueryCaptor = ArgumentCaptor.forClass(DeleteQuery.class);
    }

    abstract SemistructuredRepositoryProducer producer();
}

