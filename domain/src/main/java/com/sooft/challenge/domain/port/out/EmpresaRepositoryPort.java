package com.sooft.challenge.domain.port.out;

import com.sooft.challenge.domain.model.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EmpresaRepositoryPort {

    Empresa save(Empresa empresa);

    Optional<Empresa> findByCodigo(String codigo);

    Page<Empresa> findEmpresasAdheridasEnElUltimoMes(Pageable pageable);

    Page<Empresa> findEmpresasConTransferenciasEnElUltimoMes(Pageable pageable);

    Optional<Empresa> findByCuit(String cuit);

    Optional<Empresa> findByNumeroCuenta(String numeroCuenta);

    Page<Empresa> findAll(Pageable pageable);
}
