package org.eclipse.jnosql.mapping.semistructured.entities.autoconverter;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.UUID;

@Entity
public record BirthdayWishList(@Id UUID uuid, @Column WishCollection wishCollection) {
}
