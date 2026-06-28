-- ═══════════════════════════════════════════════════════════════
-- V8: Tabela CANCELAMENTO_AGENDAMENTO — registros de cancelamento
--
-- Regra: cancelamento mantém o registro no banco (soft operation).
-- O agendamento pai tem status = CANCELADO.
-- ═══════════════════════════════════════════════════════════════
CREATE TABLE cancelamento_agendamento (
    id                 BIGSERIAL    PRIMARY KEY,
    agendamento_id     BIGINT       NOT NULL REFERENCES agendamento(id),
    motivo             VARCHAR(500) NOT NULL,
    cancelado_por_tipo VARCHAR(20)  NOT NULL,  -- PACIENTE | FUNCIONARIO | PROFISSIONAL
    cancelado_por_id   BIGINT       NOT NULL,  -- ID do paciente ou funcionário
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_cancelador_tipo CHECK (cancelado_por_tipo IN ('PACIENTE','FUNCIONARIO','PROFISSIONAL')),
    CONSTRAINT uq_cancelamento_agendamento UNIQUE (agendamento_id)  -- 1:1 com agendamento
);

CREATE INDEX idx_cancelamento_agendamento ON cancelamento_agendamento(agendamento_id);

COMMENT ON TABLE  cancelamento_agendamento                  IS 'Detalhes de cancelamento de agendamentos';
COMMENT ON COLUMN cancelamento_agendamento.motivo           IS 'Motivo obrigatório, mínimo 10 caracteres (validado na aplicação)';
COMMENT ON COLUMN cancelamento_agendamento.cancelado_por_id IS 'ID do usuário que cancelou (paciente_id ou funcionario_id)';
