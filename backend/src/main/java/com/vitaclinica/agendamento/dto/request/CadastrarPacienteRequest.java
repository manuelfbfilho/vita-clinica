package com.vitaclinica.agendamento.dto.request;

import com.vitaclinica.agendamento.validation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "Dados para cadastro de novo paciente")
public class CadastrarPacienteRequest {

    @NotBlank(message = "Nome completo é obrigatório")
    @Size(min = 3, max = 200, message = "Nome deve ter entre 3 e 200 caracteres")
    private String nomeCompleto;

    @NotBlank(message = "CPF é obrigatório")
    @ValidCpf
    private String cpf;

    private LocalDate dataNascimento;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "\\(?\\d{2}\\)?[\\s-]?9?\\d{4}[-\\s]?\\d{4}",
             message = "Telefone inválido. Formato: (81) 99999-9999")
    private String telefone;

    @NotBlank(message = "Senha é obrigatória")
    @ValidSenha
    private String senha;

    // Endereço (todos opcionais — paciente pode preencher depois)
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String uf;

    private Long planoSaudeId;
}
