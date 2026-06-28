package com.vitaclinica.agendamento.dto.request;

import com.vitaclinica.agendamento.validation.ValidCrm;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CadastrarProfissionalRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 150)
    private String nome;

    @NotBlank(message = "CRM é obrigatório")
    @ValidCrm
    private String crm;

    @NotNull(message = "Especialidade é obrigatória")
    private Long especialidadeId;

    @DecimalMin(value = "0.0", message = "Valor da consulta não pode ser negativo")
    private BigDecimal valorConsulta;
}
