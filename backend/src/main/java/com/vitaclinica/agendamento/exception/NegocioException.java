package com.vitaclinica.agendamento.exception;

import org.springframework.http.HttpStatus;

/** Exceção para violações de regra de negócio (HTTP 422) */
public class NegocioException extends RuntimeException {

    private final String codigo;
    private final HttpStatus httpStatus;

    public NegocioException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
        this.httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
    }

    public NegocioException(String codigo, String mensagem, HttpStatus httpStatus) {
        super(mensagem);
        this.codigo = codigo;
        this.httpStatus = httpStatus;
    }

    public String getCodigo() { return codigo; }
    public HttpStatus getHttpStatus() { return httpStatus; }
}
