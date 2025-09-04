package com.sooft.challenge.application.service;

import com.sooft.challenge.domain.exception.EmpresaNotFoundException;
import com.sooft.challenge.domain.exception.TransferenciaException;
import com.sooft.challenge.domain.model.NumeroCuenta;
import com.sooft.challenge.domain.model.Transferencia;
import com.sooft.challenge.domain.port.in.RealizarTransferenciaUseCase;
import com.sooft.challenge.domain.port.out.EmpresaRepositoryPort;
import com.sooft.challenge.domain.port.out.TransferenciaRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class TransferenciaService implements RealizarTransferenciaUseCase {

    private final EmpresaRepositoryPort empresaRepositoryPort;
    private final TransferenciaRepositoryPort transferenciaRepositoryPort;

    @Override
    @Transactional
    public Transferencia realizarTransferencia(String cuentaDebito, String idEmpresaCredito, String cuentaCredito, BigDecimal importe) {

         var empresaDebito = empresaRepositoryPort.findByNumeroCuenta(NumeroCuenta.of(cuentaDebito))
                .orElseThrow(() -> new EmpresaNotFoundException("La cuenta de débito " + cuentaDebito + " no existe."));

         var empresaCredito = empresaRepositoryPort.findByCodigo(idEmpresaCredito)
                .orElseThrow(() -> new EmpresaNotFoundException("La empresa de crédito con código " + idEmpresaCredito + " no existe."));

        if (empresaDebito.getId().equals(empresaCredito.getId())) {
            throw new TransferenciaException("La cuenta de débito y crédito no pueden pertenecer a la misma empresa.");
        }

        if (!empresaCredito.getNumeroCuenta().getValor().equals(cuentaCredito)) {
            throw new TransferenciaException("La cuenta de crédito " + cuentaCredito + " no pertenece a la empresa '" + empresaCredito.getRazonSocial() + "'.");
        }

        empresaDebito.debitar(importe);
        empresaCredito.acreditar(importe);

        empresaRepositoryPort.save(empresaDebito);
        empresaRepositoryPort.save(empresaCredito);

        var transferenciaRecord = Transferencia.builder()
                .id(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE))
                .cuentaCredito(cuentaCredito)
                .cuentaDebito(cuentaDebito)
                .idEmpresa(empresaDebito.getId())
                .importe(importe)
                .fecha(LocalDate.now())
                .build();

        return transferenciaRepositoryPort.save(transferenciaRecord);
    }
}