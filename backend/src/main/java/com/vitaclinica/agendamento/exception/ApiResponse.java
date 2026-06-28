package com.vitaclinica.agendamento.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Envelope padrão para TODAS as respostas da API.
 * Sucesso:  { success: true,  data: {...} }
 * Erro:     { success: false, erro: "...", mensagem: "..." }
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    private boolean success = true;

    private T data;

    private String erro;
    private String mensagem;
    private Integer status;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private java.util.Map<String, String> campos;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /** Factory: resposta de sucesso com dados */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /** Factory: resposta de sucesso sem dados (ex: delete, cancelamento) */
    public static <T> ApiResponse<T> ok(String mensagem) {
        return ApiResponse.<T>builder()
                .success(true)
                .mensagem(mensagem)
                .build();
    }

    /** Factory: resposta de erro de negócio */
    public static <T> ApiResponse<T> erro(int status, String codigo, String mensagem) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .erro(codigo)
                .mensagem(mensagem)
                .build();
    }
}
