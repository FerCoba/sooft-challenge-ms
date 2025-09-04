package com.sooft.challenge.infrastructure.adapter.out.persistence.converter;

import com.sooft.challenge.domain.model.Cuit;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CuitConverter implements AttributeConverter<Cuit, String> {

    @Override
    public String convertToDatabaseColumn(Cuit cuit) {
        return cuit == null ? null : cuit.getValor();
    }

    @Override
    public Cuit convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Cuit.of(dbData);
    }
}