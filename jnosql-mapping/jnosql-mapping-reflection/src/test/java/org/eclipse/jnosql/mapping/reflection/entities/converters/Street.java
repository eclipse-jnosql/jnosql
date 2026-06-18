package org.eclipse.jnosql.mapping.reflection.entities.converters;

import jakarta.nosql.Column;
import jakarta.nosql.Convert;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.UUID;

@Entity
public record Street(@Id UUID id, @Column String name, @Column @Convert(UUIDConverter.class) UUID number) {
}
