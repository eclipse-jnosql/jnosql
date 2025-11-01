package org.eclipse.jnosql.mapping.semistructured.entities;

import jakarta.nosql.Projection;

@Projection(from = Person.class)
public record PersonProjection(String name, int age) {
}
