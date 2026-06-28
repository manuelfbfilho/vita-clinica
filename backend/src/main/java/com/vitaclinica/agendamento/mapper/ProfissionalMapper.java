package com.vitaclinica.agendamento.mapper;

import com.vitaclinica.agendamento.domain.Profissional;
import com.vitaclinica.agendamento.dto.response.ProfissionalResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProfissionalMapper {

    @Mapping(target = "especialidadeId",   source = "especialidade.id")
    @Mapping(target = "especialidadeNome", source = "especialidade.nome")
    ProfissionalResponse toResponse(Profissional profissional);
}
