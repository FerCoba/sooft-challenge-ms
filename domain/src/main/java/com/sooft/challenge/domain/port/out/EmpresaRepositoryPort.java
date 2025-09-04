package com.sooft.challenge.domain.port.out;

import com.sooft.challenge.domain.model.Cuit;
import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.domain.model.NumeroCuenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface EmpresaRepositoryPort {

    Empresa save(Empresa empresa);

    Optional<Empresa> findByCodigo(String codigo);

    Page<Empresa> findEmpresasAdheridasEnElUltimoMes(Pageable pageable);

    Page<Empresa> findEmpresasConTransferenciasEnElUltimoMes(Pageable pageable);

    Optional<Empresa> findByCuit(Cuit cuit);

    Optional<Empresa> findByNumeroCuenta(NumeroCuenta numeroCuenta);

    Page<Empresa> findAll(Pageable pageable);
}
