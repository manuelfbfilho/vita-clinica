-- ═══════════════════════════════════════════════════════════════
-- V9: Tabela INDISPONIBILIDADE — bloqueio de horários
--
-- Permite bloquear:
--   - Um profissional específico em uma data/horário (profissional_id NOT NULL)
--   - A clínica inteira em uma data/horário (profissional_id IS NULL)
--   - O dia inteiro (hora_inicio IS NULL, hora_fim IS NULL)
--   - Um período do dia (hora_inicio e hora_fim preenchidos)
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE indisponibilidade (
    id                   BIGSERIAL    PRIMARY KEY,
    profissional_id      BIGINT       REFERENCES profissional(id),  -- NULL = clínica toda
    data                 DATE         NOT NULL,
    hora_inicio          TIME,          -- NULL = dia inteiro bloqueado
    hora_fim             TIME,          -- NULL = dia inteiro bloqueado
    motivo               VARCHAR(300)  NOT NULL,
    registrado_por_tipo  VARCHAR(20)   NOT NULL,  -- FUNCIONARIO | ADMIN
    registrado_por_id    BIGINT        NOT NULL,
    created_at           TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_registrador_tipo CHECK (registrado_por_tipo IN ('FUNCIONARIO','ADMIN')),
    CONSTRAINT ck_hora_consistente CHECK (
        (hora_inicio IS NULL AND hora_fim IS NULL) OR
        (hora_inicio IS NOT NULL AND hora_fim IS NOT NULL AND hora_fim > hora_inicio)
    )
);

CREATE INDEX idx_indisponibilidade_data         ON indisponibilidade(data);
CREATE INDEX idx_indisponibilidade_profissional ON indisponibilidade(profissional_id);

COMMENT ON TABLE  indisponibilidade                   IS 'Bloqueio de horários — profissional específico ou clínica toda';
COMMENT ON COLUMN indisponibilidade.profissional_id   IS 'NULL = bloqueia todos os profissionais naquela data/hora';
COMMENT ON COLUMN indisponibilidade.hora_inicio       IS 'NULL junto com hora_fim = bloqueia o dia inteiro';
