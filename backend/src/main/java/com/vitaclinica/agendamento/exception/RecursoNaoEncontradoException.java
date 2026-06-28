package com.vitaclinica.agendamento.exception;

/** Lançada quando um recurso solicitado não existe no banco (HTTP 404) */
public class RecursoNaoEncontradoException extends RuntimeException {

    private final String recurso;
    private final Object id;

    public RecursoNaoEncontradoException(String recurso, Object id) {
        super(String.format("%s com id '%s' não encontrado.", recurso, id));
        this.recurso = recurso;
        this.id = id;
    }

    public String getRecurso() { return recurso; }
    public Object getId() { return id; }
}
