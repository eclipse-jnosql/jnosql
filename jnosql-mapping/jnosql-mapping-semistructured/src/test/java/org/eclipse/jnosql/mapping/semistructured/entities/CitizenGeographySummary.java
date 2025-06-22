package org.eclipse.jnosql.mapping.semistructured.entities;

import jakarta.data.repository.Select;
import org.eclipse.jnosql.mapping.Projection;

@Projection
public record CitizenGeographySummary(City city, @Select("city.name") String name) {
}
