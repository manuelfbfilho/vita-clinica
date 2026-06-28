-- ═══════════════════════════════════════════════════════════════
-- V2: Tabela ESPECIALIDADE — especialidades médicas
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE especialidade (
    id        BIGSERIAL    PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL,
    descricao VARCHAR(300),
    ativo     BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_especialidade_nome UNIQUE (nome)
);

COMMENT ON TABLE especialidade IS 'Especialidades médicas oferecidas pela clínica';
