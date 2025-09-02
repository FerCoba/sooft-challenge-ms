package com.sooft.challenge.application.service;

import com.sooft.challenge.domain.exception.EmpresaNotFoundException;
import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.domain.exception.CuitDuplicadoException;
import com.sooft.challenge.domain.exception.FechaAdhesionException;
import com.sooft.challenge.domain.port.in.*;
import com.sooft.challenge.domain.port.out.EmpresaRepositoryPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmpresaService implements AdherirEmpresaUseCase, EmpresasAdheridasUltimoMesUseCase,
        EmpresasConTransferenciasRecientesUseCase, BuscarEmpresaPorIdUseCase, BuscarTodasLasEmpresasUseCase {

    private final EmpresaRepositoryPort empresaRepositoryPort;

    @Override
    @Transactional
    public Empresa adherirEmpresa(Empresa empresa) {
        empresaRepositoryPort.findByCuit(empresa.getCuit()).ifPresent(emp -> {
            throw new CuitDuplicadoException(emp.getCuit());
        });

        if (empresa.getFechaAdhesion().isAfter(LocalDate.now())) {
            throw new FechaAdhesionException("La fecha de adhesión no puede ser posterior a la fecha actual.");
        }

        empresa.setCodigo(generadorCodigosRandom(6));
        empresa.setNumeroCuenta(generadorCodigosRandom(15));

        return empresaRepositoryPort.save(empresa);
    }

    @Override
    public Page<Empresa> findEmpresasAdheridasRecientemente(Pageable pageable) {
        return empresaRepositoryPort.findEmpresasAdheridasEnElUltimoMes(pageable);
    }

    @Override
    public Page<Empresa> findEmpresasConTransferenciasRecientes(Pageable pageable) {
        return empresaRepositoryPort.findEmpresasConTransferenciasEnElUltimoMes(pageable);
    }

    @Override
    public Optional<Empresa> findById(String id) {
        return Optional.of(empresaRepositoryPort.findByCodigo(id)
                .orElseThrow(() -> new EmpresaNotFoundException(id)));
    }

    private String generadorCodigosRandom(final int max) {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, max).toUpperCase();
    }

    @Override
    public Page<Empresa> findAll(Pageable pageable) {
        return empresaRepositoryPort.findAll(pageable);
    }
}