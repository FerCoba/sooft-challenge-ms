package com.sooft.challenge.domain.port.in;

import com.sooft.challenge.domain.model.Empresa;
import java.util.Optional;

public interface BuscarEmpresaPorIdUseCase {

    Optional<Empresa> findById(String id);
}
