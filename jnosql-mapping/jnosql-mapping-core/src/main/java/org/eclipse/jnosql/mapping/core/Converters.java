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
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.mapping.metadata.FieldParameterMetadata;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * The {@link jakarta.nosql.Convert} collection, this instance will generate/create an instance.
 */
@ApplicationScoped
public class Converters {

    private ConverterResolver resolver;

    @Inject
    public Converters(BeanManager beanManager) {
        this(new CDIConverterResolver(beanManager));
    }

    public Converters() {
    }

    private Converters(ConverterResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "The resolver is required");
    }

    public static Converters withResolver(ConverterResolver resolver) {
        return new Converters(Objects.requireNonNull(resolver, "The resolver is required"));
    }

    public static Converters withResolvers(ConverterResolver... resolvers) {
        return withResolver(new CompositeConverterResolver(resolvers));
    }

    public static Converters autoResolver() {
        return withResolver(ConverterResolver.noOp());
    }

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
    private <X, Y> AttributeConverter<X, Y> getInstance(FieldParameterMetadata metadata) {
        var resolved = resolver().resolve(metadata);
        if (resolved.isPresent()) {
            return (AttributeConverter<X, Y>) resolved.get();
        }
        return (AttributeConverter<X, Y>) metadata.newConverter()
                .orElseThrow(() -> new NoSuchElementException("There is not converter to the field: "
                        + metadata.name() + " in the Field: " + metadata.type()));
    }

    private ConverterResolver resolver() {
        return resolver == null ? ConverterResolver.noOp() : resolver;
    }

    @Override
    public String toString() {
        return "DefaultConverters{" +
                "resolver=" + resolver +
                '}';
    }
}
