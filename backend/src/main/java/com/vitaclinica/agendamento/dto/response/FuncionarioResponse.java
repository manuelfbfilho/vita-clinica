package com.vitaclinica.agendamento.dto.response;

import com.vitaclinica.agendamento.domain.enums.RoleUsuario;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FuncionarioResponse {
    private Long id;
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private RoleUsuario role;
    private Boolean ativo;
    private LocalDateTime createdAt;
}
