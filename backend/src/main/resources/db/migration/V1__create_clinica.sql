-- ═══════════════════════════════════════════════════════════════
-- V1: Tabela CLINICA — dados cadastrais da clínica
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE clinica (
    id          BIGSERIAL    PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    cnpj        VARCHAR(18)  UNIQUE,
    email       VARCHAR(150) NOT NULL,
    telefone    VARCHAR(20),
    whatsapp    VARCHAR(20),
    logo_url    VARCHAR(500),
    cep         VARCHAR(10),
    logradouro  VARCHAR(200),
    numero      VARCHAR(20),
    complemento VARCHAR(100),
    bairro      VARCHAR(100),
    cidade      VARCHAR(100),
    uf          CHAR(2),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  clinica            IS 'Dados cadastrais da clínica médica';
COMMENT ON COLUMN clinica.cnpj       IS 'CNPJ no formato 00.000.000/0000-00';
COMMENT ON COLUMN clinica.logo_url   IS 'URL pública da logomarca no storage (ex: Cloudflare R2)';
