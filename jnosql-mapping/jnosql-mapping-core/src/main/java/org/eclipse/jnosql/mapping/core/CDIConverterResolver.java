/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
 *   Maximillian Arruda
 */
package org.eclipse.jnosql.mapping.core;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.mapping.metadata.FieldParameterMetadata;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

final class CDIConverterResolver implements ConverterResolver {

    private static final Logger LOGGER = Logger.getLogger(CDIConverterResolver.class.getName());

    private final BeanManager beanManager;

    CDIConverterResolver(BeanManager beanManager) {
        this.beanManager = Objects.requireNonNull(beanManager, "The beanManager is required");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<AttributeConverter<?, ?>> resolve(FieldParameterMetadata metadata) {
        Objects.requireNonNull(metadata, "The metadata is required");
        Class<?> type = metadata.converter().orElse(null);
        if (type == null) {
            return Optional.empty();
        }

        Iterator<Bean<?>> iterator = beanManager.getBeans(type).iterator();
        if (!iterator.hasNext()) {
            LOGGER.log(Level.FINE, "The converter type: {0} not found on CDI context", type);
            return Optional.empty();
        }

        Bean<?> bean = iterator.next();
        var context = beanManager.createCreationalContext(bean);
        return Optional.of((AttributeConverter<?, ?>) beanManager.getReference(bean, type, context));
    }
}
