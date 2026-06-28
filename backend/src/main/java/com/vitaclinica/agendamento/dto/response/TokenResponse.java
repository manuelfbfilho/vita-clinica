package com.vitaclinica.agendamento.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class TokenResponse {
    private String token;
    private String tipo;
    private String role;
    private Long userId;
    private String nome;
    private String cpf;
    private long expiresIn;
}
