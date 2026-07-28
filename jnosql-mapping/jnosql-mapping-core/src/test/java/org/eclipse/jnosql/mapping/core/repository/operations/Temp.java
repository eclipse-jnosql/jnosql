package org.eclipse.jnosql.mapping.core.repository.operations;

import jakarta.data.event.PostInsertEvent;
import jakarta.data.event.PreInsertEvent;
import jakarta.data.event.PreUpdateEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Inject;
import org.eclipse.jnosql.mapping.core.entities.Book;

@ApplicationScoped
public class Temp {

    @Inject
    private Event<Object> events;

    void preInsert(Book book) {
        events.select(BookLifecycleEventTypes.PRE_INSERT)
                .fire(new PreInsertEvent<>(book));
    }

    final class BookLifecycleEventTypes {

        static final TypeLiteral<PreInsertEvent<Book>> PRE_INSERT =
                new TypeLiteral<>() {};

        static final TypeLiteral<PostInsertEvent<Book>> POST_INSERT =
                new TypeLiteral<>() {};

        static final TypeLiteral<PreUpdateEvent<Book>> PRE_UPDATE =
                new TypeLiteral<>() {};

        // Remaining lifecycle types
    }
}
