package com.sooft.challenge.infrastructure.adapter.out.persistence.mapper;

import com.sooft.challenge.domain.model.Transferencia;
import com.sooft.challenge.infrastructure.adapter.out.persistence.entity.TransferenciaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = EmpresaMapper.class)
public interface TransferenciaMapper {

    @Mapping(source = "empresa.id", target = "idEmpresa")
    Transferencia toDomain(TransferenciaEntity entity);

    @Mapping(source = "idEmpresa", target = "empresa.id")
    TransferenciaEntity toEntity(Transferencia domain);
}
