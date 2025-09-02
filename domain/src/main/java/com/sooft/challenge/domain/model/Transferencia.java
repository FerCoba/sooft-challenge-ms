package com.sooft.challenge.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transferencia {

    private Long id;
    private BigDecimal importe;
    private String idEmpresa;
    private String cuentaDebito;
    private String cuentaCredito;
    private LocalDate fecha;
}
