package com.vitaclinica.agendamento.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CancelarAgendamentoRequest {

    @NotBlank(message = "Motivo do cancelamento é obrigatório")
    @Size(min = 10, max = 500, message = "Motivo deve ter entre 10 e 500 caracteres")
    private String motivo;
}
