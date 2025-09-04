package com.sooft.challenge.infrastructure.adapter.out.persistence.mapper;

import com.sooft.challenge.domain.model.Cuit;
import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.domain.model.NumeroCuenta;
import com.sooft.challenge.infrastructure.adapter.in.web.dto.CrearEmpresaRequest;
import com.sooft.challenge.infrastructure.adapter.in.web.dto.EmpresaResponseDTO;
import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.EmpresaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {Cuit.class, NumeroCuenta.class})
public interface EmpresaMapper {

    @Mapping(target = "codigo", ignore = true)
    @Mapping(target = "numeroCuenta", ignore = true)
    @Mapping(target = "id", ignore = true)
    Empresa toDomain(CrearEmpresaRequest crearEmpresaRequest);

    @Mapping(source = "id", target = "id")
    @Mapping(target = "transferencias", ignore = true)
    EmpresaEntity toEntity(Empresa empresa);

    Empresa toDomain(EmpresaEntity empresaEntity);

    @Mapping(source = "codigo", target = "id")
    @Mapping(source = "cuit.valor", target = "cuit")
    @Mapping(source = "numeroCuenta.valor", target = "numeroCuenta")
    EmpresaResponseDTO toResponseDto(Empresa empresa);

}