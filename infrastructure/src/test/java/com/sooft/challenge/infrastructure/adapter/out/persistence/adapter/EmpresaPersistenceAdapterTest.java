package com.sooft.challenge.infrastructure.adapter.out.persistence.adapter;

import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.EmpresaEntity;
import com.sooft.challenge.infrastructure.adapter.out.persistence.mapper.EmpresaMapperImpl;
import com.sooft.challenge.infrastructure.adapter.out.persistence.repository.EmpresaJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({EmpresaPersistenceAdapter.class, EmpresaMapperImpl.class})
class EmpresaPersistenceAdapterTest {

    @Autowired
    private EmpresaPersistenceAdapter empresaPersistenceAdapter;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmpresaJpaRepository empresaJpaRepository;

    @BeforeEach
    void setUp() {
        empresaJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("Debe guardar una empresa correctamente")
    void whenSaveEmpresa_thenEmpresaIsSaved() {
        Empresa empresa = Empresa.builder()
                .codigo(UUID.randomUUID().toString().substring(0, 10))
                .numeroCuenta(UUID.randomUUID().toString())
                .cuit("30-12345678-9")
                .razonSocial("Empresa Test")
                .fechaAdhesion(LocalDate.now())
                .saldo(new BigDecimal("1000.00"))
                .build();

        empresaPersistenceAdapter.save(empresa);

        EmpresaEntity found = empresaJpaRepository.findByCuit("30-12345678-9").orElse(null);

        assertNotNull(found);
        assertEquals("Empresa Test", found.getRazonSocial());
    }

    @Test
    @DisplayName("Debe encontrar una empresa por su CUIT")
    void whenFindByCuit_thenReturnsEmpresa() {
        EmpresaEntity empresaEntity = new EmpresaEntity();
        empresaEntity.setCodigo(UUID.randomUUID().toString().substring(0, 10));
        empresaEntity.setNumeroCuenta(UUID.randomUUID().toString());
        empresaEntity.setCuit("30-98765432-1");
        empresaEntity.setRazonSocial("Empresa Test");
        empresaEntity.setFechaAdhesion(LocalDate.now());
        empresaEntity.setSaldo(BigDecimal.ZERO);

        entityManager.persistAndFlush(empresaEntity);

        Optional<Empresa> foundEmpresa = empresaPersistenceAdapter.findByCuit("30-98765432-1");

        assertTrue(foundEmpresa.isPresent());
        assertEquals("Empresa Test", foundEmpresa.get().getRazonSocial());
    }
}