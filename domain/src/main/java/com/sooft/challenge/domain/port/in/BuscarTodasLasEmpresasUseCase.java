package com.sooft.challenge.domain.port.in;

import com.sooft.challenge.domain.model.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BuscarTodasLasEmpresasUseCase {
    Page<Empresa> findAll(Pageable pageable);
}