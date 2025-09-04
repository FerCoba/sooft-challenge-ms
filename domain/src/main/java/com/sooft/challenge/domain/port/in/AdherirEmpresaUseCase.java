package com.sooft.challenge.domain.port.in;

import com.sooft.challenge.domain.model.Empresa;

import java.util.function.Function;

public interface AdherirEmpresaUseCase {

    Empresa adherirEmpresa(Empresa empresa, String idempotencyKey, Function<Empresa, String> responseSerializer);
}
