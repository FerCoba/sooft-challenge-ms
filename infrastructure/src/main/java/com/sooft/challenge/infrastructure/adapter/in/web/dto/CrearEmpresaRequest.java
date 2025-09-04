package com.sooft.challenge.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrearEmpresaRequest {

    @NotEmpty(message = "El CUIT no puede ser vacío")
    @Size(min = 11, max = 11, message = "El CUIT debe tener 11 caracteres sin guiones.")
    private String cuit;

    @NotEmpty(message = "La razón social no puede ser vacía")
    private String razonSocial;

    @NotNull(message = "La fecha de adhesión no puede ser nula")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate fechaAdhesion;

    @NotNull(message = "El saldo no puede ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "El saldo debe ser mayor que cero")
    private BigDecimal saldo;
}