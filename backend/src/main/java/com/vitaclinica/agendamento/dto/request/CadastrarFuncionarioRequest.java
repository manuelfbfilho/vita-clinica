package com.vitaclinica.agendamento.dto.request;

import com.vitaclinica.agendamento.domain.enums.RoleUsuario;
import com.vitaclinica.agendamento.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CadastrarFuncionarioRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 150)
    private String nome;

    @NotBlank(message = "CPF é obrigatório")
    @ValidCpf
    private String cpf;

    @Email(message = "Email inválido")
    private String email;

    private String telefone;

    @NotBlank(message = "Senha é obrigatória")
    @ValidSenha
    private String senha;

    private RoleUsuario role = RoleUsuario.FUNCIONARIO;
}
