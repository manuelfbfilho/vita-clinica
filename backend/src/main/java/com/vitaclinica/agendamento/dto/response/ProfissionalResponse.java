package com.vitaclinica.agendamento.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProfissionalResponse {
    private Long id;
    private String nome;
    private String crm;
    private Long especialidadeId;
    private String especialidadeNome;
    private BigDecimal valorConsulta;
    private Boolean ativo;
}
