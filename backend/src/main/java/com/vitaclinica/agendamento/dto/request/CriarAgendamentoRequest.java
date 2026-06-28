package com.vitaclinica.agendamento.dto.request;

import com.vitaclinica.agendamento.domain.enums.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CriarAgendamentoRequest {

    @NotNull(message = "Paciente é obrigatório")
    private Long pacienteId;

    @NotNull(message = "Profissional é obrigatório")
    private Long profissionalId;

    @NotNull(message = "Data da consulta é obrigatória")
    @FutureOrPresent(message = "Data não pode ser no passado")
    private LocalDate dataConsulta;

    @NotNull(message = "Hora da consulta é obrigatória")
    private LocalTime horaConsulta;

    @NotNull(message = "Tipo de atendimento é obrigatório")
    private TipoAtendimento tipoAtendimento;

    @NotNull(message = "Tipo de consulta é obrigatório")
    private TipoConsulta tipoConsulta;

    @NotNull(message = "Forma de pagamento é obrigatória")
    private FormaPagamento formaPagamento;

    private Long planoSaudeId;

    private Boolean necessitaNf = false;
    private Boolean nfEnviarEmail = false;

    @Size(max = 500)
    private String observacao;
}
