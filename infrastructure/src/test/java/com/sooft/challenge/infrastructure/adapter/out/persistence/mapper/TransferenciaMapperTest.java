package com.sooft.challenge.infrastructure.adapter.out.persistence.mapper;

import com.sooft.challenge.domain.model.Transferencia;
import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.TransferenciaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Pruebas Unitarias para TransferenciaMapper")
class TransferenciaMapperTest {

    private final TransferenciaMapper mapper = Mappers.getMapper(TransferenciaMapper.class);

    @Test
    @DisplayName("Debe mapear correctamente de TransferenciaEntity a Transferencia de modelo de Dominio")
    void debeMapearDeEntityADominio() {

        TransferenciaEntity transferenciaEntity = new TransferenciaEntity();
        transferenciaEntity.setId(1L);
        transferenciaEntity.setCuentaDebito("ORIGEN-001");
        transferenciaEntity.setCuentaCredito("DESTINO-002");
        transferenciaEntity.setImporte(new BigDecimal("150.75"));
        transferenciaEntity.setFecha(LocalDate.now());

        Transferencia transferenciaModel = mapper.toDomain(transferenciaEntity);

        assertNotNull(transferenciaModel);
        assertEquals(transferenciaEntity.getId(), transferenciaModel.getId());
        assertEquals(transferenciaEntity.getCuentaDebito(), transferenciaModel.getCuentaDebito());
        assertEquals(transferenciaEntity.getCuentaCredito(), transferenciaModel.getCuentaCredito());
        assertEquals(0, transferenciaEntity.getImporte().compareTo(transferenciaModel.getImporte()));
        assertEquals(transferenciaEntity.getFecha(), transferenciaModel.getFecha());
    }

    @Test
    @DisplayName("Debe mapear correctamente de modelo Transferencia a TransferenciaEntity")
    void debeMapearDeDominioAEntity() {

        Transferencia transferenciaModel = Transferencia.builder()
                .id(2L)
                .cuentaDebito("DOMAIN-ORIGEN-003")
                .cuentaCredito("DOMAIN-DESTINO-004")
                .importe(new BigDecimal("88.88"))
                .fecha(LocalDate.now())
                .build();

        TransferenciaEntity transferenciaEntity = mapper.toEntity(transferenciaModel);

        assertNotNull(transferenciaEntity);
        assertEquals(transferenciaModel.getId(), transferenciaEntity.getId());
        assertEquals(transferenciaModel.getCuentaCredito(), transferenciaEntity.getCuentaCredito());
        assertEquals(transferenciaModel.getCuentaDebito(), transferenciaEntity.getCuentaDebito());
        assertEquals(0, transferenciaModel.getImporte().compareTo(transferenciaEntity.getImporte()));
        assertEquals(transferenciaModel.getFecha(), transferenciaEntity.getFecha());
    }

    @Test
    @DisplayName("Mapear un objeto nulo debe devolver nulo")
    void mapearNuloDebeDevolverNulo() {
        assertNull(mapper.toDomain(null));
        assertNull(mapper.toEntity(null));
    }
}