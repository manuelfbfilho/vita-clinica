package com.vitaclinica.agendamento.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsuarioAutenticado {
    private final String cpf;
    private final Long userId;
    private final String role;

    public boolean isAdmin()       { return "ROLE_ADMIN".equals(role); }
    public boolean isFuncionario() { return "ROLE_FUNCIONARIO".equals(role) || isAdmin(); }
    public boolean isPaciente()    { return "ROLE_PACIENTE".equals(role); }
}
