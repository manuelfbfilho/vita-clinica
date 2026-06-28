-- ═══════════════════════════════════════════════════════════════
-- V6: Tabela PACIENTE — pacientes cadastrados
-- Login via CPF + senha. Plano de saúde é opcional.
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE paciente (
    id               BIGSERIAL    PRIMARY KEY,
    nome_completo    VARCHAR(200) NOT NULL,
    cpf              VARCHAR(14)  NOT NULL,
    data_nascimento  DATE,
    email            VARCHAR(150) NOT NULL,
    telefone         VARCHAR(20)  NOT NULL,
    senha_hash       VARCHAR(255) NOT NULL,
    -- Endereço
    cep              VARCHAR(10),
    logradouro       VARCHAR(200),
    numero           VARCHAR(20),
    complemento      VARCHAR(100),
    bairro           VARCHAR(100),
    cidade           VARCHAR(100),
    uf               CHAR(2),
    -- Relacionamentos
    plano_saude_id   BIGINT       REFERENCES plano_saude(id),
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_paciente_cpf   UNIQUE (cpf),
    CONSTRAINT uq_paciente_email UNIQUE (email)
);

CREATE INDEX idx_paciente_cpf          ON paciente(cpf);
CREATE INDEX idx_paciente_nome         ON paciente(nome_completo);
CREATE INDEX idx_paciente_plano_saude  ON paciente(plano_saude_id);
CREATE INDEX idx_paciente_ativo        ON paciente(ativo);

COMMENT ON TABLE  paciente                IS 'Pacientes da clínica. Login = CPF';
COMMENT ON COLUMN paciente.cpf            IS 'CPF no formato 000.000.000-00 — usado como login';
COMMENT ON COLUMN paciente.senha_hash     IS 'Senha codificada com BCrypt (força 12)';
COMMENT ON COLUMN paciente.plano_saude_id IS 'Plano principal do paciente (pode ser sobrescrito no agendamento)';
