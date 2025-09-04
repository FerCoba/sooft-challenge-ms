package com.sooft.challenge.infrastructure.adapter.out.persistence.repository;

import com.sooft.challenge.domain.model.Cuit;
import com.sooft.challenge.domain.model.NumeroCuenta;
import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.EmpresaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EmpresaJpaRepository extends JpaRepository<EmpresaEntity, String> {

    Optional<EmpresaEntity> findByCuit(Cuit cuit);

    Optional<EmpresaEntity> findByCodigo(String codigo);

    @Query("SELECT e FROM EmpresaEntity e WHERE e.fechaAdhesion >= :fechaDesde ORDER BY e.fechaAdhesion DESC")
    Page<EmpresaEntity> findEmpresasAdheridasDesde(@Param("fechaDesde") LocalDate fechaDesde, Pageable pageable);

    @Query("SELECT DISTINCT t.empresa FROM TransferenciaEntity t WHERE t.fecha >= :fechaDesde ORDER BY t.empresa.razonSocial ASC")
    Page<EmpresaEntity> findEmpresasConTransferenciasDesde(@Param("fechaDesde") LocalDate fechaDesde, Pageable pageable);

    Optional<EmpresaEntity> findByNumeroCuenta(NumeroCuenta numeroCuenta);
}
