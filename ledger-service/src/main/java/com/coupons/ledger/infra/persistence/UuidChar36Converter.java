package com.coupons.ledger.infra.persistence;

import java.util.UUID;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/** Persiste {@link UUID} no MySQL como {@code CHAR(36)} (texto); no Java continua {@link UUID}. */
@Converter(autoApply = false)
public class UuidChar36Converter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public UUID convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return UUID.fromString(dbData.trim());
    }
}
