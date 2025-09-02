package com.sooft.challenge.infrastructure.adapter.out.persistence.mapper;

import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.EmpresaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Pruebas Unitarias para EmpresaMapper")
class EmpresaMapperTest {

    private final EmpresaMapper mapper = Mappers.getMapper(EmpresaMapper.class);

    @Test
    @DisplayName("Debe mapear correctamente de EmpresaEntity a modelo de Dominio")
    void debeMapearDeEntityADominio() {
        EmpresaEntity entity = new EmpresaEntity();
        entity.setId("ENTITY-001");
        entity.setRazonSocial("Razón Social Entity");
        entity.setCuit("30-11111111-1");
        entity.setFechaAdhesion(LocalDate.of(2024, 1, 15));
        entity.setSaldo(new BigDecimal("2500.50"));
        entity.setNumeroCuenta("ACC-ENTITY-123");

        Empresa domainModel = mapper.toDomain(entity);

        assertNotNull(domainModel);
        assertEquals(entity.getId(), domainModel.getId());
        assertEquals(entity.getRazonSocial(), domainModel.getRazonSocial());
        assertEquals(entity.getCuit(), domainModel.getCuit());
        assertEquals(entity.getFechaAdhesion(), domainModel.getFechaAdhesion());
        assertEquals(0, entity.getSaldo().compareTo(domainModel.getSaldo()));
        assertEquals(entity.getNumeroCuenta(), domainModel.getNumeroCuenta());
    }

    @Test
    @DisplayName("Debe mapear correctamente de modelo de Dominio a EmpresaEntity")
    void debeMapearDeDominioAEntity() {
        Empresa domainModel = Empresa.builder()
                .id("DOMAIN-002")
                .razonSocial("Razón Social Dominio")
                .cuit("30-22222222-2")
                .fechaAdhesion(LocalDate.of(2023, 5, 20))
                .saldo(new BigDecimal("999.99"))
                .numeroCuenta("ACC-DOMAIN-456")
                .build();

        EmpresaEntity entity = mapper.toEntity(domainModel);

        assertNotNull(entity);
        assertEquals(domainModel.getId(), entity.getId());
        assertEquals(domainModel.getRazonSocial(), entity.getRazonSocial());
        assertEquals(domainModel.getCuit(), entity.getCuit());
        assertEquals(domainModel.getFechaAdhesion(), entity.getFechaAdhesion());
        assertEquals(0, domainModel.getSaldo().compareTo(entity.getSaldo()));
        assertEquals(domainModel.getNumeroCuenta(), entity.getNumeroCuenta());
    }
}