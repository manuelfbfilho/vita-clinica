package com.vitaclinica.agendamento.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CriarIndisponibilidadeRequest {

    /** NULL = bloqueia todos os profissionais */
    private Long profissionalId;

    @NotNull(message = "Data é obrigatória")
    private LocalDate data;

    /** NULL junto com horaFim = dia inteiro bloqueado */
    private LocalTime horaInicio;
    private LocalTime horaFim;

    @NotBlank(message = "Motivo é obrigatório")
    @Size(min = 5, max = 300)
    private String motivo;
}
