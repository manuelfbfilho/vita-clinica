-- ═══════════════════════════════════════════════════════════════
-- V5: Tabela FUNCIONARIO — colaboradores da clínica
-- Login via CPF + senha. Role define nível de acesso.
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE funcionario (
    id         BIGSERIAL    PRIMARY KEY,
    nome       VARCHAR(150) NOT NULL,
    cpf        VARCHAR(14)  NOT NULL,
    email      VARCHAR(150),
    telefone   VARCHAR(20),
    senha_hash VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'FUNCIONARIO',
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_funcionario_cpf   UNIQUE (cpf),
    CONSTRAINT ck_funcionario_role  CHECK  (role IN ('FUNCIONARIO','ADMIN'))
);

CREATE INDEX idx_funcionario_cpf   ON funcionario(cpf);
CREATE INDEX idx_funcionario_ativo ON funcionario(ativo);

COMMENT ON TABLE  funcionario            IS 'Funcionários da clínica. Login = CPF';
COMMENT ON COLUMN funcionario.cpf        IS 'CPF no formato 000.000.000-00 — usado como login';
COMMENT ON COLUMN funcionario.senha_hash IS 'Senha codificada com BCrypt (força 12)';
COMMENT ON COLUMN funcionario.role       IS 'FUNCIONARIO = acesso operacional | ADMIN = acesso total';
