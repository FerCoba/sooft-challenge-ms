package com.sooft.challenge.application.service;

import com.sooft.challenge.domain.exception.EmpresaNotFoundException;
import com.sooft.challenge.domain.exception.TransferenciaException;
import com.sooft.challenge.domain.model.NumeroCuenta;
import com.sooft.challenge.domain.model.Transferencia;
import com.sooft.challenge.domain.port.in.RealizarTransferenciaUseCase;
import com.sooft.challenge.domain.port.out.EmpresaRepositoryPort;
import com.sooft.challenge.domain.port.out.TransferenciaRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferenciaService implements RealizarTransferenciaUseCase {

    private final EmpresaRepositoryPort empresaRepositoryPort;
    private final TransferenciaRepositoryPort transferenciaRepositoryPort;

    @Override
    @Transactional
    public Transferencia realizarTransferencia(String cuentaDebito, String idEmpresaCredito, String cuentaCredito, BigDecimal importe) {
        log.info("Iniciando transferencia de {} desde cuenta {} hacia cuenta {} (Empresa Crédito: {})",
                importe, cuentaDebito, cuentaCredito, idEmpresaCredito);

        log.debug("Buscando empresa de débito por número de cuenta: {}", cuentaDebito);
        var empresaDebito = empresaRepositoryPort.findByNumeroCuenta(NumeroCuenta.of(cuentaDebito))
                .orElseThrow(() -> {
                    log.warn("No se encontró empresa con cuenta de débito: {}", cuentaDebito);
                    return new EmpresaNotFoundException("La cuenta de débito " + cuentaDebito + " no existe.");
                });
        log.info("Empresa de débito encontrada: '{}' (ID: {})", empresaDebito.getRazonSocial(), empresaDebito.getId());

        log.debug("Buscando empresa de crédito por código: {}", idEmpresaCredito);
        var empresaCredito = empresaRepositoryPort.findByCodigo(idEmpresaCredito)
                .orElseThrow(() -> {
                    log.warn("No se encontró empresa de crédito con código: {}", idEmpresaCredito);
                    return new EmpresaNotFoundException("La empresa de crédito con código " + idEmpresaCredito + " no existe.");
                });
        log.info("Empresa de crédito encontrada: '{}' (ID: {})", empresaCredito.getRazonSocial(), empresaCredito.getId());

        if (empresaDebito.getId().equals(empresaCredito.getId())) {
            log.warn("Validación fallida: La cuenta de débito y crédito pertenecen a la misma empresa (ID: {})", empresaDebito.getId());
            throw new TransferenciaException("La cuenta de débito y crédito no pueden pertenecer a la misma empresa.");
        }

        if (!empresaCredito.getNumeroCuenta().getValor().equals(cuentaCredito)) {
            log.warn("Validación fallida: La cuenta de crédito {} no pertenece a la empresa '{}' (Código: {})",
                    cuentaCredito, empresaCredito.getRazonSocial(), idEmpresaCredito);
            throw new TransferenciaException("La cuenta de crédito " + cuentaCredito + " no pertenece a la empresa '" + empresaCredito.getRazonSocial() + "'.");
        }

        log.info("Realizando débito de {} a la empresa '{}'. Saldo anterior: {}", importe, empresaDebito.getRazonSocial(), empresaDebito.getSaldo());
        empresaDebito.debitar(importe);
        log.info("Saldo actualizado empresa débito: {}", empresaDebito.getSaldo());

        log.info("Realizando crédito de {} a la empresa '{}'. Saldo anterior: {}", importe, empresaCredito.getRazonSocial(), empresaCredito.getSaldo());
        empresaCredito.acreditar(importe);
        log.info("Saldo actualizado empresa crédito: {}", empresaCredito.getSaldo());

        empresaRepositoryPort.save(empresaDebito);
        empresaRepositoryPort.save(empresaCredito);
        log.debug("Saldos de las empresas actualizados en la base de datos.");

        var transferenciaRecord = Transferencia.builder()
                .id(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE))
                .cuentaCredito(cuentaCredito)
                .cuentaDebito(cuentaDebito)
                .idEmpresa(empresaDebito.getId())
                .importe(importe)
                .fecha(LocalDate.now())
                .build();

        var transferenciaGuardada = transferenciaRepositoryPort.save(transferenciaRecord);
        log.info("Transferencia completada y registrada con ID: {}", transferenciaGuardada.getId());

        return transferenciaGuardada;
    }
}