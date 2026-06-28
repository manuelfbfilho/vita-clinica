-- ═══════════════════════════════════════════════════════════════
-- V3: Tabela PLANO_SAUDE — planos aceitos pela clínica
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE plano_saude (
    id     BIGSERIAL    PRIMARY KEY,
    nome   VARCHAR(150) NOT NULL,
    codigo VARCHAR(50),
    ativo  BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_plano_nome UNIQUE (nome)
);

COMMENT ON TABLE  plano_saude        IS 'Planos de saúde conveniados com a clínica';
COMMENT ON COLUMN plano_saude.codigo IS 'Código interno de identificação do plano';
