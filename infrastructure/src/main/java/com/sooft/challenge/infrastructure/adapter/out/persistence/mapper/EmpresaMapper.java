package com.sooft.challenge.infrastructure.adapter.out.persistence.mapper;

import com.sooft.challenge.domain.model.Empresa;
import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.EmpresaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    @Mapping(source = "saldo", target = "saldo")

    EmpresaEntity toEntity(Empresa empresa);

    Empresa toDomain(EmpresaEntity empresaEntity);
}