package com.sooft.challenge.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "empresas")
@Data
public class EmpresaEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    @Column(unique = true, nullable = false, length = 10)
    private String codigo;

    @Column(name = "cuit", nullable = false, unique = true)
    private String cuit;

    @Column(name = "razon_social", nullable = false)
    private String razonSocial;

    @Column(name = "fecha_adhesion", nullable = false)
    private LocalDate fechaAdhesion;

    @Column(name = "saldo", nullable = false)
    private BigDecimal saldo;

    @Column(unique = true, nullable = false)
    private String numeroCuenta;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransferenciaEntity> transferencias;
}