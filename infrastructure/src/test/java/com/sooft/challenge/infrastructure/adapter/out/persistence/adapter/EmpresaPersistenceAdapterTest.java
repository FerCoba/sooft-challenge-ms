package com.sooft.challenge.infrastructure.adapter.out.persistence.adapter;

import com.sooft.challenge.domain.model.Cuit;
import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.domain.model.NumeroCuenta;
import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.EmpresaEntity;
import com.sooft.challenge.infrastructure.adapter.out.persistence.mapper.EmpresaMapperImpl;
import com.sooft.challenge.infrastructure.config.TestClockConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({EmpresaPersistenceAdapter.class, EmpresaMapperImpl.class, TestClockConfiguration.class})
class EmpresaPersistenceAdapterTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmpresaPersistenceAdapter empresaPersistenceAdapter;

    @Test
    void guardarEmpresa_retornaEmpresaGuardada() {

        var empresa = Empresa.builder()
                .id(UUID.randomUUID().toString())
                .codigo("EMP-TEST")
                .razonSocial("Test Corp")
                .cuit(Cuit.of("30-99999999-7"))
                .fechaAdhesion(LocalDate.now())
                .saldo(BigDecimal.ZERO)
                .numeroCuenta(NumeroCuenta.of("987654321"))
                .build();

        var empresaGuardada = empresaPersistenceAdapter.save(empresa);

        var encontrada = entityManager.find(EmpresaEntity.class, empresaGuardada.getId());
        assertThat(encontrada).isNotNull();
        assertThat(encontrada.getCodigo()).isEqualTo("EMP-TEST");
        assertThat(encontrada.getCuit().getValor()).isEqualTo("30-99999999-7");
    }

    @Test
    void buscarEmpresaPorCuit_retornaEmpresa() {

        var entity = new EmpresaEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setCodigo("EMP-FIND");
        entity.setRazonSocial("Find Corp");
        entity.setCuit(Cuit.of("30-88888888-8"));
        entity.setFechaAdhesion(LocalDate.now());
        entity.setSaldo(BigDecimal.ZERO);
        entity.setNumeroCuenta(NumeroCuenta.of("123456789"));
        entityManager.merge(entity);

        var resultado = empresaPersistenceAdapter.findByCuit(Cuit.of("30-88888888-8"));

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getCodigo()).isEqualTo("EMP-FIND");
    }
}