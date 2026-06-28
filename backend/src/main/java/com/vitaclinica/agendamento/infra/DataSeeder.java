package com.vitaclinica.agendamento.infra;

import com.vitaclinica.agendamento.domain.*;
import com.vitaclinica.agendamento.domain.enums.RoleUsuario;
import com.vitaclinica.agendamento.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Popula usuários iniciais na primeira execução.
 * Flyway cria o schema e dados estruturais (especialidades, profissionais, planos).
 * Este seeder cria as contas de acesso com senhas BCrypt codificadas em runtime.
 *
 * SENHA PADRÃO DE DEMONSTRAÇÃO: Vita@2025#
 * (Troque em produção via variável de ambiente SENHA_DEMO ou removendo este seeder)
 */
@Slf4j
@Component
@org.springframework.context.annotation.Profile("!test")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final FuncionarioRepository funcionarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PlanoSaudeRepository planoSaudeRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String SENHA_DEMO = "Vita@2025#";

    @Override
    public void run(String... args) {
        seedFuncionarios();
        seedPacientes();
        log.info("✅ DataSeeder concluído. Acesso demo → CPF: 000.000.000-01 | Senha: {}", SENHA_DEMO);
    }

    // ─────────────────────────────────────────────────────────────
    // FUNCIONÁRIOS
    // ─────────────────────────────────────────────────────────────
    private void seedFuncionarios() {
        if (funcionarioRepository.count() > 0) {
            log.debug("Funcionários já existem. Seed ignorado.");
            return;
        }

        String hash = passwordEncoder.encode(SENHA_DEMO);

        funcionarioRepository.save(Funcionario.builder()
                .nome("Maria das Graças Silva")
                .cpf("000.000.000-01")
                .email("maria.graca@vitaclinica.com.br")
                .telefone("(81) 91111-0001")
                .senhaHash(hash)
                .role(RoleUsuario.ADMIN)
                .build());

        funcionarioRepository.save(Funcionario.builder()
                .nome("João Pedro Alves")
                .cpf("000.000.000-02")
                .email("joao.pedro@vitaclinica.com.br")
                .telefone("(81) 91111-0002")
                .senhaHash(hash)
                .role(RoleUsuario.FUNCIONARIO)
                .build());

        funcionarioRepository.save(Funcionario.builder()
                .nome("Carla Cristina Rocha")
                .cpf("000.000.000-03")
                .email("carla.rocha@vitaclinica.com.br")
                .telefone("(81) 91111-0003")
                .senhaHash(hash)
                .role(RoleUsuario.FUNCIONARIO)
                .build());

        log.info("✅ 3 funcionários criados (admin + 2 funcionários)");
    }

    // ─────────────────────────────────────────────────────────────
    // PACIENTES
    // ─────────────────────────────────────────────────────────────
    private void seedPacientes() {
        if (pacienteRepository.count() > 0) {
            log.debug("Pacientes já existem. Seed ignorado.");
            return;
        }

        String hash = passwordEncoder.encode(SENHA_DEMO);

        // Busca plano Unimed para associar ao paciente de demo
        PlanoSaude unimed = planoSaudeRepository.findByNomeContainingIgnoreCase("Unimed")
                .stream().findFirst().orElse(null);

        pacienteRepository.save(Paciente.builder()
                .nomeCompleto("João da Silva Santos")
                .cpf("111.111.111-11")
                .email("joao.silva@email.com")
                .telefone("(81) 98888-0001")
                .senhaHash(hash)
                .cep("50.050-000")
                .logradouro("Rua das Flores")
                .numero("123")
                .bairro("Boa Vista")
                .cidade("Recife")
                .uf("PE")
                .planoSaude(unimed)
                .build());

        pacienteRepository.save(Paciente.builder()
                .nomeCompleto("Maria Aparecida Ferreira")
                .cpf("222.222.222-22")
                .email("maria.aparecida@email.com")
                .telefone("(81) 98888-0002")
                .senhaHash(hash)
                .cep("51.020-000")
                .logradouro("Av. Boa Viagem")
                .numero("500")
                .bairro("Boa Viagem")
                .cidade("Recife")
                .uf("PE")
                .build());

        pacienteRepository.save(Paciente.builder()
                .nomeCompleto("Carlos Eduardo Lima")
                .cpf("333.333.333-33")
                .email("carlos.lima@email.com")
                .telefone("(81) 98888-0003")
                .senhaHash(hash)
                .cep("52.010-000")
                .logradouro("Rua do Futuro")
                .numero("77")
                .bairro("Casa Amarela")
                .cidade("Recife")
                .uf("PE")
                .build());

        log.info("✅ 3 pacientes de demonstração criados");
    }
}
