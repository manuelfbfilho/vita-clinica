package com.vitaclinica.agendamento.domain.enums;

public enum FormaPagamento {
    PARTICULAR("Particular"),
    PLANO("Plano de Saúde");

    private final String descricao;

    FormaPagamento(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
