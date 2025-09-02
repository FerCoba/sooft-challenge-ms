package com.sooft.challenge.domain.model;

import com.sooft.challenge.domain.exception.FondosInsuficientesException;
import com.sooft.challenge.domain.exception.MontoNegativoException;
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
public class Empresa {

    private String id;
    private String codigo;
    private String cuit;
    private String razonSocial;
    private LocalDate fechaAdhesion;
    private BigDecimal saldo;
    private String numeroCuenta;


    public void debitar(BigDecimal monto) {

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontoNegativoException("El monto a debitar debe ser positivo.");
        }

        if (this.saldo == null || this.saldo.compareTo(monto) < 0) {
            throw new FondosInsuficientesException(this.numeroCuenta);
        }
        this.saldo = this.saldo.subtract(monto);
    }

    public void acreditar(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontoNegativoException("El monto a acreditar debe ser positivo.");
        }
        if (this.saldo == null) {
            this.saldo = BigDecimal.ZERO;
        }
        this.saldo = this.saldo.add(monto);
    }
}
