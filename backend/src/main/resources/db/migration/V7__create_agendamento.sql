-- ═══════════════════════════════════════════════════════════════
-- V7: Tabela AGENDAMENTO — consultas agendadas
--
-- DECISÃO DE DESIGN:
--   data_consulta DATE + hora_consulta TIME separados conforme
--   requisito, facilitando queries de disponibilidade e filtros.
--
-- UNICIDADE:
--   Partial indexes garantem que profissional e paciente não
--   possam ter dois agendamentos ATIVOS no mesmo horário.
--   Agendamentos CANCELADOS não bloqueiam o slot.
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE agendamento (
    id                BIGSERIAL    PRIMARY KEY,
    -- Partes envolvidas
    paciente_id       BIGINT       NOT NULL REFERENCES paciente(id),
    profissional_id   BIGINT       NOT NULL REFERENCES profissional(id),
    funcionario_id    BIGINT       REFERENCES funcionario(id),  -- NULL = paciente agendou online
    -- Data e hora (salvos separados, conforme requisito)
    data_consulta     DATE         NOT NULL,
    hora_consulta     TIME         NOT NULL,
    -- Classificação
    tipo_atendimento  VARCHAR(20)  NOT NULL DEFAULT 'PRESENCIAL',
    tipo_consulta     VARCHAR(20)  NOT NULL DEFAULT 'CONSULTA',
    status            VARCHAR(25)  NOT NULL DEFAULT 'AGENDADO',
    -- Financeiro
    forma_pagamento   VARCHAR(20)  NOT NULL DEFAULT 'PARTICULAR',
    plano_saude_id    BIGINT       REFERENCES plano_saude(id),
    necessita_nf      BOOLEAN      NOT NULL DEFAULT FALSE,
    nf_enviar_email   BOOLEAN      NOT NULL DEFAULT FALSE,
    -- Observações
    observacao        VARCHAR(500),
    -- Auditoria
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    -- Validações de domínio no banco
    CONSTRAINT ck_tipo_atendimento  CHECK (tipo_atendimento IN ('PRESENCIAL','VIRTUAL')),
    CONSTRAINT ck_tipo_consulta     CHECK (tipo_consulta    IN ('CONSULTA','RETORNO','ENCAIXE')),
    CONSTRAINT ck_status            CHECK (status           IN ('AGENDADO','CONFIRMADO','CANCELADO','CONCLUIDO','NAO_COMPARECEU')),
    CONSTRAINT ck_forma_pagamento   CHECK (forma_pagamento  IN ('PLANO','PARTICULAR'))
);

-- ─────────────────────────────────────────────────────────────
-- PARTIAL UNIQUE INDEXES: conflito apenas em agendamentos ativos
-- Um profissional/paciente pode ter agendamentos cancelados no
-- mesmo horário sem bloquear novos agendamentos.
-- ─────────────────────────────────────────────────────────────
CREATE UNIQUE INDEX uq_profissional_horario_ativo
    ON agendamento(profissional_id, data_consulta, hora_consulta)
    WHERE status NOT IN ('CANCELADO');

CREATE UNIQUE INDEX uq_paciente_horario_ativo
    ON agendamento(paciente_id, data_consulta, hora_consulta)
    WHERE status NOT IN ('CANCELADO');

-- Indexes de performance para queries frequentes
CREATE INDEX idx_agendamento_data          ON agendamento(data_consulta);
CREATE INDEX idx_agendamento_status        ON agendamento(status);
CREATE INDEX idx_agendamento_paciente      ON agendamento(paciente_id);
CREATE INDEX idx_agendamento_profissional  ON agendamento(profissional_id);
CREATE INDEX idx_agendamento_data_prof     ON agendamento(data_consulta, profissional_id);

COMMENT ON TABLE  agendamento                IS 'Consultas agendadas na clínica';
COMMENT ON COLUMN agendamento.funcionario_id IS 'NULL quando o próprio paciente agendou online';
COMMENT ON COLUMN agendamento.data_consulta  IS 'Data da consulta — separada da hora para facilitar filtros';
COMMENT ON COLUMN agendamento.hora_consulta  IS 'Hora da consulta — separada da data para facilitar filtros';
