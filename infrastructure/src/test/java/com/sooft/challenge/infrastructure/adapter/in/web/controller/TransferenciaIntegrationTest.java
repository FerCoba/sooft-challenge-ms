package com.sooft.challenge.infrastructure.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sooft.challenge.domain.model.Cuit;
import com.sooft.challenge.domain.model.NumeroCuenta;
import com.sooft.challenge.infrastructure.adapter.in.web.dto.RealizarTransferenciaRequest;
import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.EmpresaEntity;
import com.sooft.challenge.infrastructure.adapter.out.persistence.repository.EmpresaJpaRepository;
import com.sooft.challenge.infrastructure.adapter.out.persistence.repository.TransferenciaJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransferenciaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpresaJpaRepository empresaRepository;

    @Autowired
    private TransferenciaJpaRepository transferenciaRepository;

    private static final String CUENTA_ORIGEN = "11111-1";
    private static final String CUENTA_DESTINO = "22222-2";

    @BeforeEach
    void setUp() {
        transferenciaRepository.deleteAll();
        empresaRepository.deleteAll();

        EmpresaEntity empresaOrigen = new EmpresaEntity();
        empresaOrigen.setId(String.valueOf(UUID.randomUUID()));
        empresaOrigen.setCodigo("EMP-A");
        empresaOrigen.setRazonSocial("Empresa Origen");
        empresaOrigen.setCuit(Cuit.of("30111111111"));
        empresaOrigen.setNumeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN));
        empresaOrigen.setSaldo(new BigDecimal("1000.00"));
        empresaOrigen.setFechaAdhesion(LocalDate.now());

        EmpresaEntity empresaDestino = new EmpresaEntity();
        empresaDestino.setId(String.valueOf(UUID.randomUUID()));
        empresaDestino.setCodigo("EMP-B");
        empresaDestino.setRazonSocial("Empresa Destino");
        empresaDestino.setCuit(Cuit.of("30222222222"));
        empresaDestino.setNumeroCuenta(NumeroCuenta.of(CUENTA_DESTINO));
        empresaDestino.setSaldo(new BigDecimal("500.00"));
        empresaDestino.setFechaAdhesion(LocalDate.now());

        empresaRepository.save(empresaOrigen);
        empresaRepository.save(empresaDestino);
    }

    @Test
    @DisplayName("Debe realizar una transferencia completa y actualizar los saldos en la BD")
    void debeRealizarTransferenciaYActualizarSaldosEnBD() throws Exception {

        RealizarTransferenciaRequest request = new RealizarTransferenciaRequest(
                CUENTA_DESTINO,
                "EMP-B",
                CUENTA_ORIGEN,
                new BigDecimal("250.50")
        );

        mockMvc.perform(post("/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        EmpresaEntity origenActualizada = empresaRepository.findByNumeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN)).get();
        EmpresaEntity destinoActualizada = empresaRepository.findByNumeroCuenta(NumeroCuenta.of(CUENTA_DESTINO)).get();

        assertEquals(0, new BigDecimal("749.50").compareTo(origenActualizada.getSaldo()));
        assertEquals(0, new BigDecimal("750.50").compareTo(destinoActualizada.getSaldo()));
        assertEquals(1, transferenciaRepository.count());
    }

    @Test
    @DisplayName("Debe fallar por fondos insuficientes y no modificar la BD")
    void debeFallarPorFondosInsuficientesYNoTocarBD() throws Exception {

        RealizarTransferenciaRequest request = new RealizarTransferenciaRequest(
                CUENTA_DESTINO,
                "EMP-B",
                CUENTA_ORIGEN,
                new BigDecimal("2000.00")
        );

        mockMvc.perform(post("/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Fondos insuficientes en la cuenta " + CUENTA_ORIGEN)));

        EmpresaEntity origenSinCambios = empresaRepository.findByNumeroCuenta(NumeroCuenta.of(CUENTA_ORIGEN)).get();
        EmpresaEntity destinoSinCambios = empresaRepository.findByNumeroCuenta(NumeroCuenta.of(CUENTA_DESTINO)).get();

        assertEquals(0, new BigDecimal("1000.00").compareTo(origenSinCambios.getSaldo()));
        assertEquals(0, new BigDecimal("500.00").compareTo(destinoSinCambios.getSaldo()));
        assertEquals(0, transferenciaRepository.count());
    }
}