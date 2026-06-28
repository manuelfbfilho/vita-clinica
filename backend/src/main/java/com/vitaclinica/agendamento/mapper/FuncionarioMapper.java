package com.vitaclinica.agendamento.mapper;

import com.vitaclinica.agendamento.domain.Funcionario;
import com.vitaclinica.agendamento.dto.response.FuncionarioResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FuncionarioMapper {
    FuncionarioResponse toResponse(Funcionario funcionario);
}
