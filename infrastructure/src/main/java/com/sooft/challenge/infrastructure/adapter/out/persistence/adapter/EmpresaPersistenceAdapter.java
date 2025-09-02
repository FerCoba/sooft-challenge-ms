package com.sooft.challenge.infrastructure.adapter.out.persistence.adapter;

import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.domain.port.out.EmpresaRepositoryPort;
import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.EmpresaEntity;
import com.sooft.challenge.infrastructure.adapter.out.persistence.mapper.EmpresaMapper;
import com.sooft.challenge.infrastructure.adapter.out.persistence.repository.EmpresaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmpresaPersistenceAdapter implements EmpresaRepositoryPort {

    private final EmpresaJpaRepository empresaJpaRepository;
    private final EmpresaMapper empresaMapper;

    @Override
    public Empresa save(Empresa empresa) {
        EmpresaEntity empresaEntity = empresaMapper.toEntity(empresa);
        EmpresaEntity savedEntity = empresaJpaRepository.save(empresaEntity);
        return empresaMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Empresa> findByCuit(String cuit) {
        return empresaJpaRepository.findByCuit(cuit)
                .map(empresaMapper::toDomain);
    }

    @Override
    public Page<Empresa> findEmpresasAdheridasEnElUltimoMes(Pageable pageable) {
        LocalDate fechaDesde = LocalDate.now().minusMonths(1);
        Page<EmpresaEntity> entityPage = empresaJpaRepository.findEmpresasAdheridasDesde(fechaDesde, pageable);
        return entityPage.map(empresaMapper::toDomain);
    }

    @Override
    public Page<Empresa> findEmpresasConTransferenciasEnElUltimoMes(Pageable pageable) {
        LocalDate fechaDesde = LocalDate.now().minusMonths(1);
        Page<EmpresaEntity> entityPage = empresaJpaRepository.findEmpresasConTransferenciasDesde(fechaDesde, pageable);
        return entityPage.map(empresaMapper::toDomain);
    }

    @Override
    public Optional<Empresa> findByNumeroCuenta(String numeroCuenta) {
        return empresaJpaRepository.findByNumeroCuenta(numeroCuenta)
                .map(empresaMapper::toDomain);
    }

    @Override
    public Optional<Empresa> findByCodigo(String codigo) {
       return empresaJpaRepository.findByCodigo(codigo)
                .map(empresaMapper::toDomain);
    }

    @Override
    public Page<Empresa> findAll(Pageable pageable) {
        return empresaJpaRepository.findAll(pageable)
                .map(empresaMapper::toDomain);
    }
}