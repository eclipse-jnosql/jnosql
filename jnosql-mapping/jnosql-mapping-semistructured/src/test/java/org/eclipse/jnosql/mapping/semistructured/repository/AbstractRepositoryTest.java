package org.eclipse.jnosql.mapping.semistructured.repository;

import org.eclipse.jnosql.mapping.semistructured.SemiStructuredTemplate;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBookBookStore;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.ComicBookRepository;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.PhotoSocialMediaRepository;
import org.eclipse.jnosql.mapping.semistructured.repository.entities.VideoSocialMediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

abstract class AbstractRepositoryTest {

    protected SemiStructuredTemplate template;
    protected ComicBookRepository comicBookRepository;

    protected ComicBookBookStore bookStore;

    protected PhotoSocialMediaRepository photoSocialMediaRepository;

    protected VideoSocialMediaRepository videoSocialMediaRepository;



    @BeforeEach
    void setUP() {
        this.template = Mockito.mock(SemiStructuredTemplate.class);
        this.comicBookRepository = producer().get(ComicBookRepository.class, template);
        this.bookStore = producer().get(ComicBookBookStore.class, template);
        this.photoSocialMediaRepository = producer().get(PhotoSocialMediaRepository.class, template);
        this.videoSocialMediaRepository = producer().get(VideoSocialMediaRepository.class, template);
    }

    abstract SemistructuredRepositoryProducer producer();
}

