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
package org.eclipse.jnosql.mapping.core;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.mapping.metadata.FieldParameterMetadata;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides access to {@link AttributeConverter} instances used by the mapping layer.
 *
 * <p>This component is responsible for resolving converter instances declared
 * through {@link jakarta.nosql.Convert}. Converters are obtained from the CDI
 * container when available, allowing them to participate in dependency injection
 * and lifecycle management. When no CDI-managed bean exists for a converter type,
 * the converter is instantiated using its no-argument constructor.</p>
 *
 * <p>The converter resolution process is transparent to mapping implementations,
 * providing a centralized mechanism for obtaining converter instances regardless
 * of whether they are managed by CDI or created directly.</p>
 */
@ApplicationScoped
public class Converters {

    private static final Logger LOGGER = Logger.getLogger(Converters.class.getName());

    @Inject
    private BeanManager beanManager;

    /**
     * Returns a converter instance where it might use scope from CDI.
     *
     * @param metadata the metadata field
     * @param <X> the type of the entity attribute
     * @param <Y> the type of the database column
     * @return a converter instance
     * @throws NullPointerException when converter is null
     */
    public <X, Y> AttributeConverter<X, Y> get(FieldParameterMetadata metadata) {
        Objects.requireNonNull(metadata, "The metadata is required");
        return getInstance(metadata);
    }



    @SuppressWarnings("unchecked")
    private <T> T getInstance(FieldParameterMetadata metadata) {
        Class<T> type = (Class<T>) metadata.converter()
                .orElseThrow(() -> new NoSuchElementException("There is not converter to the field: "
                        + metadata.name() + " in the Field: " + metadata.type()));

        Iterator<Bean<?>> iterator = beanManager.getBeans(type).iterator();
        if (iterator.hasNext()) {
            Bean<T> bean = (Bean<T>) iterator.next();
            CreationalContext<T> ctx = beanManager.createCreationalContext(bean);
            return (T) beanManager.getReference(bean, type, ctx);
        } else {
            LOGGER.log(Level.FINE, "The converter type: " + type + " not found on CDI context, creating by constructor");
            return (T) metadata.newConverter() .orElseThrow(() -> new NoSuchElementException("There is not converter to the field: "
                    + metadata.name() + " in the Field: " + metadata.type()));
        }

    }


    @Override
    public String toString() {
        return "DefaultConverters{" +
                "beanManager=" + beanManager +
                '}';
    }
}
