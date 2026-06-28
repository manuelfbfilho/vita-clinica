package com.vitaclinica.agendamento.domain.enums;

public enum StatusAgendamento {
    AGENDADO("Agendado"),
    CONFIRMADO("Confirmado"),
    CANCELADO("Cancelado"),
    CONCLUIDO("Concluído"),
    NAO_COMPARECEU("Não Compareceu");

    private final String descricao;

    StatusAgendamento(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }

    /** Verifica se o agendamento está em estado que pode ser cancelado */
    public boolean isCancelavel() {
        return this == AGENDADO || this == CONFIRMADO;
    }
}
