package org.eclipse.jnosql.mapping.reflection.entities;

import java.util.function.Supplier;

public record Email(String value) implements Supplier<String> {

    @Override
    public String get() {
        return value;
    }
}
