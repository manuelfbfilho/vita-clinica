package com.vitaclinica.agendamento.mapper;

import com.vitaclinica.agendamento.domain.Paciente;
import com.vitaclinica.agendamento.dto.request.CadastrarPacienteRequest;
import com.vitaclinica.agendamento.dto.response.PacienteResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PacienteMapper {

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "senhaHash",  ignore = true)
    @Mapping(target = "planoSaude", ignore = true)
    @Mapping(target = "ativo",      ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    Paciente toEntity(CadastrarPacienteRequest request);

    @Mapping(target = "planoSaudeId",   source = "planoSaude.id")
    @Mapping(target = "planoSaudeNome", source = "planoSaude.nome")
    PacienteResponse toResponse(Paciente paciente);
}
