-- ═══════════════════════════════════════════════════════════════
-- V13: SEED — Dados iniciais da Vita Clínica
-- ═══════════════════════════════════════════════════════════════
INSERT INTO clinica (nome, email, telefone, whatsapp, cep, logradouro, numero, bairro, cidade, uf) VALUES
    (
        'Vita Clínica',
        'contato@vitaclinica.com.br',
        '(81) 3333-4444',
        '(81) 99999-0000',
        '50.050-000',
        'Rua do Riachuelo',
        '100',
        'Boa Vista',
        'Recife',
        'PE'
    );

-- NOTA: Funcionários e pacientes são criados pelo DataSeeder.java
-- usando BCryptPasswordEncoder para garantir hashes corretos.
-- Senha padrão para contas de demonstração: Vita@2025#
-- (Gerada em tempo de execução pela aplicação)
