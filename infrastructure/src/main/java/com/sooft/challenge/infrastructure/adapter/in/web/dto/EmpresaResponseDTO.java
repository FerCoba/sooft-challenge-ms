package com.sooft.challenge.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaResponseDTO {

    private String id;

    private String razonSocial;

    private String cuit;

    private LocalDate fechaAdhesion;

    private BigDecimal saldo;

    private String numeroCuenta;
}
