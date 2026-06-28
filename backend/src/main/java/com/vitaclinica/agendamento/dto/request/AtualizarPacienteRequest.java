package com.vitaclinica.agendamento.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AtualizarPacienteRequest {
    @Size(min = 3, max = 200)
    private String nomeCompleto;

    private LocalDate dataNascimento;

    @Email(message = "Email inválido")
    private String email;

    @Pattern(regexp = "\\(?\\d{2}\\)?[\\s-]?9?\\d{4}[-\\s]?\\d{4}",
             message = "Telefone inválido")
    private String telefone;

    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;
    private Long planoSaudeId;
}
