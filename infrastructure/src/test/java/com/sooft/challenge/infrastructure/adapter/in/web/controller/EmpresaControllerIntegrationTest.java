package com.sooft.challenge.infrastructure.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sooft.challenge.infrastructure.adapter.in.web.dto.CrearEmpresaRequest;
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
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmpresaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmpresaJpaRepository empresaRepository;

    @Autowired
    private TransferenciaJpaRepository transferenciaRepository;

    private EmpresaEntity empresaExistente;

    @BeforeEach
    void setUp() {
        transferenciaRepository.deleteAll();
        empresaRepository.deleteAll();

        empresaExistente = new EmpresaEntity();
        empresaExistente.setId(String.valueOf(UUID.randomUUID()));
        empresaExistente.setCodigo("EMP-EXIST");
        empresaExistente.setRazonSocial("Empresa Ya Existente");
        empresaExistente.setCuit("30111111111");
        empresaExistente.setNumeroCuenta("12345-1");
        empresaExistente.setSaldo(new BigDecimal("1000.00"));
        empresaExistente.setFechaAdhesion(LocalDate.now().minusMonths(2));
        empresaRepository.save(empresaExistente);
    }

    @Test
    @DisplayName("POST /empresas - Debe crear una nueva empresa y devolver 201 Created")
    void debeCrearNuevaEmpresa() throws Exception {
        CrearEmpresaRequest request = new CrearEmpresaRequest(
                "30222222222",
                "Empresa Nueva",
                LocalDate.now(),
                new BigDecimal("500.00")
        );

        mockMvc.perform(post("/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.cuit", is("30222222222")))
                .andExpect(jsonPath("$.razonSocial", is("Empresa Nueva")));

        assertEquals(2, empresaRepository.count());
    }

    @Test
    @DisplayName("POST /empresas - Debe fallar si el CUIT ya existe y devolver 409 Conflict")
    void debeFallarAlCrearEmpresaConCuitDuplicado() throws Exception {
        CrearEmpresaRequest request = new CrearEmpresaRequest(
                empresaExistente.getCuit(),
                "Intento de Duplicado",
                LocalDate.now(),
                new BigDecimal("100.00")
        );

        mockMvc.perform(post("/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", is("Ya existe una empresa registrada con el CUIT: " + empresaExistente.getCuit())));

        assertEquals(1, empresaRepository.count());
    }

    @Test
    @DisplayName("GET /empresas/{id} - Debe devolver los datos de una empresa existente")
    void debeDevolverEmpresaExistente() throws Exception {
        String codigoExistente = empresaExistente.getCodigo();

       mockMvc.perform(get("/empresas/" + codigoExistente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(codigoExistente)))
                .andExpect(jsonPath("$.cuit", is(empresaExistente.getCuit())));
    }

    @Test
    @DisplayName("GET /empresas/{id} - Debe devolver 404 si la empresa no existe")
    void debeFallarSiEmpresaNoExiste() throws Exception {
        String idNoExistente = "CODIGO-FAKE";

        mockMvc.perform(get("/empresas/" + idNoExistente))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /empresas/reportes/adheridas-ultimo-mes - Debe devolver solo empresas adheridas recientemente")
    void debeDevolverReporteDeEmpresasRecientes() throws Exception {
        EmpresaEntity empresaReciente = new EmpresaEntity();
        empresaReciente.setId(String.valueOf(UUID.randomUUID()));
        empresaReciente.setCodigo("EMP-RECENT");
        empresaReciente.setRazonSocial("Empresa Reciente");
        empresaReciente.setCuit("30333333333");
        empresaReciente.setNumeroCuenta("54321-9");
        empresaReciente.setSaldo(new BigDecimal("200.00"));
        empresaReciente.setFechaAdhesion(LocalDate.now().minusDays(15));
        empresaRepository.save(empresaReciente);

        mockMvc.perform(get("/empresas/reportes/adheridas-ultimo-mes?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].cuit", is(empresaReciente.getCuit())));
    }
}