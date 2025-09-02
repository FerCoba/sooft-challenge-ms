package com.sooft.challenge.domain.port.out;

import com.sooft.challenge.domain.model.Transferencia;

public interface TransferenciaRepositoryPort {

    Transferencia save(Transferencia transferencia);

}