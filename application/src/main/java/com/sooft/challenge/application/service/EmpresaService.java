package com.sooft.challenge.application.service;

import com.sooft.challenge.domain.exception.EmpresaNotFoundException;
import com.sooft.challenge.domain.model.Cuit;
import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.domain.exception.CuitDuplicadoException;
import com.sooft.challenge.domain.exception.FechaAdhesionException;
import com.sooft.challenge.domain.model.NumeroCuenta;
import com.sooft.challenge.domain.port.in.AdherirEmpresaUseCase;
import com.sooft.challenge.domain.port.in.EmpresasAdheridasUltimoMesUseCase;
import com.sooft.challenge.domain.port.in.EmpresasConTransferenciasRecientesUseCase;
import com.sooft.challenge.domain.port.in.BuscarEmpresaPorIdUseCase;
import com.sooft.challenge.domain.port.in.BuscarTodasLasEmpresasUseCase;
import com.sooft.challenge.domain.port.out.EmpresaRepositoryPort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpresaService implements AdherirEmpresaUseCase, EmpresasAdheridasUltimoMesUseCase,
        EmpresasConTransferenciasRecientesUseCase, BuscarEmpresaPorIdUseCase, BuscarTodasLasEmpresasUseCase {

    private final EmpresaRepositoryPort empresaRepositoryPort;
    private final Clock clock;

    @Override
    public Empresa adherirEmpresa(Empresa empresa) {

        empresaRepositoryPort.findByCuit(Cuit.of(empresa.getCuit().getValor())).ifPresent(exception -> {
            throw new CuitDuplicadoException(empresa.getCuit().getValor());
        });

        if (empresa.getFechaAdhesion().isAfter(LocalDate.now(clock))) {
            throw new FechaAdhesionException("La fecha de adhesión no puede ser posterior a la fecha actual.");
        }

        var empresaFinal = Empresa.builder()
                .id(UUID.randomUUID().toString())
                .cuit(empresa.getCuit())
                .razonSocial(empresa.getRazonSocial())
                .fechaAdhesion(empresa.getFechaAdhesion())
                .saldo(empresa.getSaldo())
                .codigo(generadorCodigosRandom(8))
                .numeroCuenta(NumeroCuenta.of(generadorCodigosRandom(15)))
                .build();

        return empresaRepositoryPort.save(empresaFinal);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Empresa> findEmpresasAdheridasRecientemente(Pageable pageable) {
        return empresaRepositoryPort.findEmpresasAdheridasEnElUltimoMes(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Empresa> findEmpresasConTransferenciasRecientes(Pageable pageable) {
        return empresaRepositoryPort.findEmpresasConTransferenciasEnElUltimoMes(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Empresa> findById(String id) {
        return Optional.of(empresaRepositoryPort.findByCodigo(id)
                .orElseThrow(() -> new EmpresaNotFoundException(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Empresa> findAll(Pageable pageable) {
        return empresaRepositoryPort.findAll(pageable);
    }

    private String generadorCodigosRandom(final int max) {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, max).toUpperCase();
    }
}