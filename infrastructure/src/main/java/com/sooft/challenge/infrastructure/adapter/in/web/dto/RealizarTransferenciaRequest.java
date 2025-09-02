package com.sooft.challenge.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RealizarTransferenciaRequest(

        @NotBlank(message = "La cuenta de crédito no puede estar vacía.")
        String cuentaCredito,

        @JsonProperty("idEmpresaCredito")
        @NotBlank(message = "El ID de la empresa no puede estar vacío.")
        String idEmpresa,

        @NotBlank(message = "La cuenta de débito no puede estar vacía.")
        String cuentaDebito,

        @NotNull(message = "El importe no puede ser nulo.")
        @Positive(message = "El importe debe ser mayor que cero.")
        BigDecimal importe
) {}
