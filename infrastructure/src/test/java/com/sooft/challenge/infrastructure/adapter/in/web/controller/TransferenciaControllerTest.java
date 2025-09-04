package com.sooft.challenge.infrastructure.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sooft.challenge.domain.port.in.RealizarTransferenciaUseCase;
import com.sooft.challenge.infrastructure.adapter.in.web.dto.RealizarTransferenciaRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferenciaController.class)
class TransferenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RealizarTransferenciaUseCase realizarTransferenciaUseCase;

    @Test
    @DisplayName("Debe procesar una transferencia válida y devolver 201 Created")
    void postConUnaTransferenciaValida_retorna201() throws Exception {

        RealizarTransferenciaRequest request = new RealizarTransferenciaRequest(
                "11111-1",
                "EMP-B",
                "22222-2",
                new BigDecimal("150.50")
        );

        when(realizarTransferenciaUseCase.realizarTransferencia(
                request.cuentaDebito(),
                request.idEmpresa(),
                request.cuentaCredito(),
                request.importe()
        )).thenReturn(null);

        mockMvc.perform(post("/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(realizarTransferenciaUseCase).realizarTransferencia(
                request.cuentaDebito(),
                request.idEmpresa(),
                request.cuentaCredito(),
                request.importe()
        );
    }

    @Test
    @DisplayName("Debe rechazar una transferencia con importe negativo y devolver 400 Bad Request")
    void postTransferenciaConMontoNegativo_retorna400() throws Exception {

        RealizarTransferenciaRequest request = new RealizarTransferenciaRequest(
                "11111-1",
                "EMP-B",
                "22222-2",
                new BigDecimal("-100.00")
        );

        mockMvc.perform(post("/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debe rechazar una transferencia con cuenta de débito nula y devolver 400 Bad Request")
    void postTransferenciaConCuentaNull_retorna400() throws Exception {

        RealizarTransferenciaRequest request = new RealizarTransferenciaRequest(
                null,
                "EMP-B",
                "22222-2",
                new BigDecimal("150.50")
        );

        mockMvc.perform(post("/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}