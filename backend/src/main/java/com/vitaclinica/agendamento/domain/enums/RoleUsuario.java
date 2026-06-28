package com.vitaclinica.agendamento.domain.enums;

public enum RoleUsuario {
    PACIENTE("ROLE_PACIENTE"),
    FUNCIONARIO("ROLE_FUNCIONARIO"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    RoleUsuario(String authority) { this.authority = authority; }
    public String getAuthority() { return authority; }
}
