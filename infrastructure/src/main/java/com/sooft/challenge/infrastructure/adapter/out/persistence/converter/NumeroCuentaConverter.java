package com.sooft.challenge.infrastructure.adapter.out.persistence.converter;

import com.sooft.challenge.domain.model.NumeroCuenta;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class NumeroCuentaConverter implements AttributeConverter<NumeroCuenta, String> {

    @Override
    public String convertToDatabaseColumn(NumeroCuenta numeroCuenta) {
        return numeroCuenta == null ? null : numeroCuenta.getValor();
    }

    @Override
    public NumeroCuenta convertToEntityAttribute(String dbData) {
        return dbData == null ? null : NumeroCuenta.of(dbData);
    }
}