package org.eclipse.jnosql.mapping.reflection.entities.converters;

import jakarta.nosql.AttributeConverter;
import org.eclipse.jnosql.mapping.reflection.entities.Email;

public class EmailConverter implements AttributeConverter<Email, String> {

    @Override
    public String convertToDatabaseColumn(Email attribute) {
        return attribute.value();
    }

    @Override
    public Email convertToEntityAttribute(String dbData) {
        return new Email(dbData);
    }
}
