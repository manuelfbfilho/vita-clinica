-- ═══════════════════════════════════════════════════════════════
-- V4: Tabela PROFISSIONAL — médicos e outros profissionais
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE profissional (
    id               BIGSERIAL      PRIMARY KEY,
    nome             VARCHAR(150)   NOT NULL,
    crm              VARCHAR(20)    NOT NULL,
    especialidade_id BIGINT         NOT NULL REFERENCES especialidade(id),
    valor_consulta   NUMERIC(10,2),
    ativo            BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_profissional_crm UNIQUE (crm)
);

CREATE INDEX idx_profissional_especialidade ON profissional(especialidade_id);
CREATE INDEX idx_profissional_ativo         ON profissional(ativo);

COMMENT ON TABLE  profissional               IS 'Profissionais de saúde da clínica';
COMMENT ON COLUMN profissional.crm           IS 'Registro profissional no formato CRM/UF-NNNNN';
COMMENT ON COLUMN profissional.valor_consulta IS 'Valor particular da consulta em reais';
