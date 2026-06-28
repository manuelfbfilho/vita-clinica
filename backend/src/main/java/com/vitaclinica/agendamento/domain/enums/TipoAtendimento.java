package com.vitaclinica.agendamento.domain.enums;

public enum TipoAtendimento {
    PRESENCIAL("Presencial"),
    VIRTUAL("Virtual / Teleconsulta");

    private final String descricao;

    TipoAtendimento(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
