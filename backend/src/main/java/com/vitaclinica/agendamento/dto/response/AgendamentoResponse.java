package com.vitaclinica.agendamento.dto.response;

import com.vitaclinica.agendamento.domain.enums.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class AgendamentoResponse {
    private Long id;
    private Long pacienteId;
    private String pacienteNome;
    private String pacienteCpf;
    private Long profissionalId;
    private String profissionalNome;
    private String profissionalCrm;
    private String especialidadeNome;
    private Long funcionarioId;
    private String funcionarioNome;
    private LocalDate dataConsulta;
    private LocalTime horaConsulta;
    private TipoAtendimento tipoAtendimento;
    private TipoConsulta tipoConsulta;
    private StatusAgendamento status;
    private FormaPagamento formaPagamento;
    private String planoSaudeNome;
    private Boolean necessitaNf;
    private Boolean nfEnviarEmail;
    private String observacao;
    private String motivoCancelamento;
    private LocalDateTime createdAt;
}
