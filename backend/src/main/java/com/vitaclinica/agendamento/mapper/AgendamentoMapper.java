package com.vitaclinica.agendamento.mapper;

import com.vitaclinica.agendamento.domain.Agendamento;
import com.vitaclinica.agendamento.dto.response.AgendamentoResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AgendamentoMapper {

    @Mapping(target = "pacienteId",       source = "paciente.id")
    @Mapping(target = "pacienteNome",     source = "paciente.nomeCompleto")
    @Mapping(target = "pacienteCpf",      source = "paciente.cpf")
    @Mapping(target = "profissionalId",   source = "profissional.id")
    @Mapping(target = "profissionalNome", source = "profissional.nome")
    @Mapping(target = "profissionalCrm",  source = "profissional.crm")
    @Mapping(target = "especialidadeNome",source = "profissional.especialidade.nome")
    @Mapping(target = "funcionarioId",    source = "funcionario.id")
    @Mapping(target = "funcionarioNome",  source = "funcionario.nome")
    @Mapping(target = "planoSaudeNome",   source = "planoSaude.nome")
    @Mapping(target = "motivoCancelamento", expression = "java(agendamento.getCancelamento() != null ? agendamento.getCancelamento().getMotivo() : null)")
    AgendamentoResponse toResponse(Agendamento agendamento);
}
