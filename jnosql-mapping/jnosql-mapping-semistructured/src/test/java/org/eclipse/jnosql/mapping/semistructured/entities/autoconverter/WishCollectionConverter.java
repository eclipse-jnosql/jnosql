package org.eclipse.jnosql.mapping.semistructured.entities.autoconverter;

import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Converter;


@Converter(autoApply = true)
public class WishCollectionConverter implements AttributeConverter<WishCollection, String> {

    @Override
    public String convertToDatabaseColumn(WishCollection attribute) {
        return attribute.toString();
    }

    @Override
    public WishCollection convertToEntityAttribute(String dbData) {
        return WishCollection.parse(dbData);
    }
}
