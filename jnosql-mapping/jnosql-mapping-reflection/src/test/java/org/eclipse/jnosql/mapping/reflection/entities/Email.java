package org.eclipse.jnosql.mapping.reflection.entities;

import java.util.Objects;
import java.util.function.Supplier;

public record Email(String value) implements Supplier<String> {

    public Email{
        Objects.requireNonNull(value, "Email value cannot be null");
        if(!value.contains("@")) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }
    @Override
    public String get() {
        return value;
    }
}
