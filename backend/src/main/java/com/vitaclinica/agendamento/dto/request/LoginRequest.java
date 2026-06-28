package com.vitaclinica.agendamento.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Credenciais de acesso")
public class LoginRequest {
    @NotBlank(message = "CPF é obrigatório")
    @Schema(example = "000.000.000-01")
    private String cpf;

    @NotBlank(message = "Senha é obrigatória")
    @Schema(example = "Vita@2025#")
    private String senha;
}
