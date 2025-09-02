package com.sooft.challenge.domain.port.in;

import com.sooft.challenge.domain.model.Transferencia;

import java.math.BigDecimal;

public interface RealizarTransferenciaUseCase {

    Transferencia realizarTransferencia(String cuentaDebito, String idEmpresaCredito, String cuentaCredito, BigDecimal importe);
}
