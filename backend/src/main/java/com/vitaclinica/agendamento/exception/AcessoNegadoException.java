package com.vitaclinica.agendamento.exception;

/** Lançada quando o usuário tenta acessar um recurso sem permissão (HTTP 403) */
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException() {
        super("Você não tem permissão para realizar esta operação.");
    }
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
