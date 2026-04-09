package com.coupons.auth.domain.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Aceita valores em BD nulos, vazios ou inválidos (ex.: linhas antigas antes da coluna {@code role})
 * e trata-os como {@link UserRole#USER}.
 */
@Converter(autoApply = false)
public class UserRoleJpaConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        if (attribute == null) {
            return UserRole.USER.name();
        }
        return attribute.name();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return UserRole.USER;
        }
        String s = dbData.trim();
        if (s.isEmpty()) {
            return UserRole.USER;
        }
        try {
            return UserRole.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return UserRole.USER;
        }
    }
}
