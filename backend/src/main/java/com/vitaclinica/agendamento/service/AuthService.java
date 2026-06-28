package com.vitaclinica.agendamento.service;

import com.vitaclinica.agendamento.domain.enums.RoleUsuario;
import com.vitaclinica.agendamento.dto.request.LoginRequest;
import com.vitaclinica.agendamento.dto.response.TokenResponse;
import com.vitaclinica.agendamento.repository.*;
import com.vitaclinica.agendamento.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final FuncionarioRepository funcionarioRepo;
    private final PacienteRepository    pacienteRepo;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    public TokenResponse login(LoginRequest req) {
        String cpf = req.getCpf().replaceAll("[^0-9]", "");
        cpf = formatarCpf(cpf);

        // Tenta como Funcionário primeiro (inclui ADMIN)
        var funcionario = funcionarioRepo.findByCpf(cpf).orElse(null);
        if (funcionario != null && funcionario.getAtivo()) {
            validarSenha(req.getSenha(), funcionario.getSenhaHash());
            return gerarToken(funcionario.getCpf(), funcionario.getRole(),
                    funcionario.getId(), funcionario.getNome());
        }

        // Tenta como Paciente
        var paciente = pacienteRepo.findByCpf(cpf).orElse(null);
        if (paciente != null && paciente.getAtivo()) {
            validarSenha(req.getSenha(), paciente.getSenhaHash());
            return gerarToken(paciente.getCpf(), RoleUsuario.PACIENTE,
                    paciente.getId(), paciente.getNomeCompleto());
        }

        throw new BadCredentialsException("CPF ou senha incorretos");
    }

    private void validarSenha(String senhaInformada, String hash) {
        if (!passwordEncoder.matches(senhaInformada, hash)) {
            throw new BadCredentialsException("CPF ou senha incorretos");
        }
    }

    private TokenResponse gerarToken(String cpf, RoleUsuario role, Long userId, String nome) {
        String token = jwtService.gerarToken(
                Map.of("role", role.getAuthority(), "userId", userId, "nome", nome),
                cpf
        );
        return TokenResponse.builder()
                .token(token)
                .tipo("Bearer")
                .role(role.getAuthority())
                .userId(userId)
                .nome(nome)
                .cpf(cpf)
                .expiresIn(expiration / 1000)
                .build();
    }

    private String formatarCpf(String numeros) {
        if (numeros.length() != 11) return numeros;
        return numeros.substring(0,3)+"."+numeros.substring(3,6)+"."+
               numeros.substring(6,9)+"-"+numeros.substring(9);
    }
}
