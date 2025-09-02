package com.sooft.challenge.infrastructure.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.domain.port.in.*;
import com.sooft.challenge.infrastructure.adapter.in.web.dto.CrearEmpresaRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmpresaController.class)
class EmpresaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private AdherirEmpresaUseCase adherirEmpresaUseCase;
    @MockBean private EmpresasAdheridasUltimoMesUseCase empresasAdheridasUltimoMesUseCase;
    @MockBean private EmpresasConTransferenciasRecientesUseCase empresasConTransferenciasRecientesUseCase;
    @MockBean private BuscarEmpresaPorIdUseCase buscarEmpresaPorIdUseCase;
    @MockBean private BuscarTodasLasEmpresasUseCase buscarTodasLasEmpresasUseCase;

    @Test
    @DisplayName("Debe crear una empresa y devolver 201 Created")
    void whenPostValidEmpresa_thenReturns201() throws Exception {
        CrearEmpresaRequest request = new CrearEmpresaRequest("30123456785","Empresa Test",  LocalDate.now(), new BigDecimal("1000"));

        Empresa empresaCreada = Empresa.builder()
                .codigo("XYZ789")
                .razonSocial(request.getRazonSocial())
                .cuit(request.getCuit())
                .saldo(request.getSaldo())
                .numeroCuenta("123456789012345")
                .build();

        when(adherirEmpresaUseCase.adherirEmpresa(any(Empresa.class))).thenReturn(empresaCreada);

        mockMvc.perform(post("/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("XYZ789")))
                .andExpect(jsonPath("$.razonSocial", is("Empresa Test")));
    }

    @Test
    @DisplayName("Debe devolver 400 Bad Request si el CUIT es nulo")
    void whenPostInvalidEmpresa_thenReturns400() throws Exception {
        CrearEmpresaRequest request = new CrearEmpresaRequest(null, "Empresa Inválida",  LocalDate.now(), BigDecimal.ZERO);

        mockMvc.perform(post("/empresas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debe devolver una empresa y 200 OK cuando el ID existe")
    void whenGetByExistingId_thenReturnsEmpresa() throws Exception {
        String codigo = "ABC123";
        Empresa empresa = Empresa.builder().codigo(codigo).razonSocial("Mi Empresa").build();
        when(buscarEmpresaPorIdUseCase.findById(codigo)).thenReturn(Optional.of(empresa));

        mockMvc.perform(get("/empresas/{id}", codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(codigo)))
                .andExpect(jsonPath("$.razonSocial", is("Mi Empresa")));
    }

    @Test
    @DisplayName("Debe devolver una respuesta paginada para adheridas-ultimo-mes")
    void givenPagingParams_whenGetAdheridasUltimoMes_thenReturnsPagedResponse() throws Exception {
        Empresa empresa = Empresa.builder().codigo("ABC123").razonSocial("Test Corp").build();
        Page<Empresa> empresaPage = new PageImpl<>(Collections.singletonList(empresa), PageRequest.of(0, 5), 1);
        when(empresasAdheridasUltimoMesUseCase.findEmpresasAdheridasRecientemente(any(Pageable.class))).thenReturn(empresaPage);

        mockMvc.perform(get("/empresas/reportes/adheridas-ultimo-mes?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is("ABC123")))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Debe devolver una respuesta paginada para transferencias-ultimo-mes")
    void givenPagingParams_whenGetTransferencias_thenReturnsPagedResponse() throws Exception {
        Empresa empresa = Empresa.builder().codigo("DEF456").razonSocial("Transfer Corp").build();

        Pageable pageable = PageRequest.of(1, 10);
        List<Empresa> content = Collections.singletonList(empresa);
        long totalElements = 11;
        Page<Empresa> empresaPage = new PageImpl<>(content, pageable, totalElements);

        when(empresasConTransferenciasRecientesUseCase.findEmpresasConTransferenciasRecientes(any(Pageable.class))).thenReturn(empresaPage);

        mockMvc.perform(get("/empresas/reportes/transferencias-ultimo-mes?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is("DEF456")))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.totalElements", is(11)))
                .andExpect(jsonPath("$.number", is(1)));
    }
}