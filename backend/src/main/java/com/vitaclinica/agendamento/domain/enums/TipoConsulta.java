package com.vitaclinica.agendamento.domain.enums;

public enum TipoConsulta {
    CONSULTA("Consulta"),
    RETORNO("Retorno"),
    ENCAIXE("Encaixe");

    private final String descricao;

    TipoConsulta(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
