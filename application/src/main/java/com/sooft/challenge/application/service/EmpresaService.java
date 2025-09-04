package com.sooft.challenge.application.service;

import com.sooft.challenge.domain.exception.EmpresaNotFoundException;
import com.sooft.challenge.domain.exception.IdempotentRequestException;
import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.domain.exception.CuitDuplicadoException;
import com.sooft.challenge.domain.exception.FechaAdhesionException;
import com.sooft.challenge.domain.model.IdempotencyRecord;
import com.sooft.challenge.domain.model.NumeroCuenta;
import com.sooft.challenge.domain.port.in.AdherirEmpresaUseCase;
import com.sooft.challenge.domain.port.in.EmpresasAdheridasUltimoMesUseCase;
import com.sooft.challenge.domain.port.in.EmpresasConTransferenciasRecientesUseCase;
import com.sooft.challenge.domain.port.in.BuscarEmpresaPorIdUseCase;
import com.sooft.challenge.domain.port.in.BuscarTodasLasEmpresasUseCase;
import com.sooft.challenge.domain.port.out.EmpresaRepositoryPort;
import com.sooft.challenge.domain.port.out.IdempotencyKeyPort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmpresaService implements AdherirEmpresaUseCase, EmpresasAdheridasUltimoMesUseCase,
        EmpresasConTransferenciasRecientesUseCase, BuscarEmpresaPorIdUseCase, BuscarTodasLasEmpresasUseCase {

    private final EmpresaRepositoryPort empresaRepositoryPort;
    private final Clock clock;
    private final IdempotencyKeyPort idempotencyKeyPort;

    @Override
    @Transactional
    public Empresa adherirEmpresa(Empresa empresa, String idempotencyKey, Function<Empresa, String> responseSerializer) {
        log.info("Iniciando proceso de adhesión para empresa con CUIT: {}", empresa.getCuit().getValor());

        Optional<IdempotencyRecord> existingRecord = idempotencyKeyPort.findById(idempotencyKey);
        if (existingRecord.isPresent()) {
            log.warn("[IDEMPOTENCY-KEY:{}] Petición duplicada detectada. Devolviendo respuesta guardada.", idempotencyKey);
            throw new IdempotentRequestException(
                    existingRecord.get().getResponseBody(),
                    existingRecord.get().getResponseStatus()
            );
        }

        log.debug("[IDEMPOTENCY-KEY:{}] Realizando validaciones de negocio para CUIT: {}", idempotencyKey, empresa.getCuit().getValor());
        empresaRepositoryPort.findByCuit(empresa.getCuit()).ifPresent(e -> {
            log.warn("[IDEMPOTENCY-KEY:{}] Intento de adhesión con CUIT duplicado: {}", idempotencyKey, empresa.getCuit().getValor());
            throw new CuitDuplicadoException(empresa.getCuit().getValor());
        });

        if (empresa.getFechaAdhesion().isAfter(LocalDate.now(clock))) {
            log.warn("[IDEMPOTENCY-KEY:{}] Intento de adhesión con fecha futura: {}", idempotencyKey, empresa.getFechaAdhesion());
            throw new FechaAdhesionException("La fecha de adhesión no puede ser posterior a la fecha actual.");
        }

        String codigoGenerado = generadorCodigosRandom(8);
        String numeroCuentaGenerado = generadorCodigosRandom(15);
        log.info("[IDEMPOTENCY-KEY:{}] Generando nuevos identificadores. Código: {}, Número de Cuenta: {}", idempotencyKey, codigoGenerado, numeroCuentaGenerado);

        var empresaFinal = Empresa.builder()
                .id(UUID.randomUUID().toString())
                .cuit(empresa.getCuit())
                .razonSocial(empresa.getRazonSocial())
                .fechaAdhesion(empresa.getFechaAdhesion())
                .saldo(empresa.getSaldo())
                .codigo(codigoGenerado)
                .numeroCuenta(NumeroCuenta.of(numeroCuentaGenerado))
                .build();

        var nuevaEmpresa = empresaRepositoryPort.save(empresaFinal);
        log.info("[IDEMPOTENCY-KEY:{}] Empresa con CUIT {} guardada exitosamente con ID: {}", idempotencyKey, nuevaEmpresa.getCuit().getValor(), nuevaEmpresa.getId());

        try {
            var responseBody = responseSerializer.apply(nuevaEmpresa);
            var recordToSave = IdempotencyRecord.builder()
                    .idempotencyKey(idempotencyKey)
                    .responseBody(responseBody)
                    .responseStatus(201) // HTTP 201 Created
                    .createdAt(LocalDateTime.now(clock))
                    .build();

            idempotencyKeyPort.save(recordToSave);
            log.info("[IDEMPOTENCY-KEY:{}] Registro de idempotencia guardado correctamente.", idempotencyKey);
        } catch (Exception e) {
            log.error("[IDEMPOTENCY-KEY:{}] ¡CRÍTICO! La empresa fue creada pero falló el guardado de la clave de idempotencia. Esto puede llevar a procesamientos duplicados.", idempotencyKey, e);
        }

        return nuevaEmpresa;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Empresa> findEmpresasAdheridasRecientemente(Pageable pageable) {
        log.info("Buscando empresas adheridas en el último mes. Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
        return empresaRepositoryPort.findEmpresasAdheridasEnElUltimoMes(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Empresa> findEmpresasConTransferenciasRecientes(Pageable pageable) {
        log.info("Buscando empresas con transferencias recientes. Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
        return empresaRepositoryPort.findEmpresasConTransferenciasEnElUltimoMes(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Empresa> findById(String id) {
        log.info("Buscando empresa por código: {}", id);
        return Optional.of(empresaRepositoryPort.findByCodigo(id)
                .orElseThrow(() -> {
                    log.warn("No se encontró ninguna empresa con el código: {}", id);
                    return new EmpresaNotFoundException(id);
                }));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Empresa> findAll(Pageable pageable) {
        log.info("Buscando todas las empresas. Página: {}, Tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
        return empresaRepositoryPort.findAll(pageable);
    }

    private String generadorCodigosRandom(final int max) {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, max).toUpperCase();
    }
}