package com.vitaclinica.agendamento.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PacienteResponse {
    private Long id;
    private String nomeCompleto;
    private String cpf;
    private LocalDate dataNascimento;
    private String email;
    private String telefone;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private Long planoSaudeId;
    private String planoSaudeNome;
    private Boolean ativo;
    private LocalDateTime createdAt;
}
