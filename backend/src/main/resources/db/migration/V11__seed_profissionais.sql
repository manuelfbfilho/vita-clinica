-- ═══════════════════════════════════════════════════════════════
-- V11: SEED — Profissionais fictícios para demonstração
-- Todos os CRMs e dados são fictícios.
-- ═══════════════════════════════════════════════════════════════
INSERT INTO profissional (nome, crm, especialidade_id, valor_consulta) VALUES
    -- Clínica Geral (id=1)
    ('Dr. Carlos Alberto Mendes',     'CRM/PE-12345', 1,  180.00),
    ('Dra. Patrícia Lima Vieira',     'CRM/PE-12346', 1,  180.00),
    -- Cardiologia (id=2)
    ('Dra. Ana Beatriz Fontes',       'CRM/PE-23456', 2,  350.00),
    ('Dr. Ricardo Albuquerque Neto',  'CRM/PE-23457', 2,  380.00),
    -- Dermatologia (id=3)
    ('Dr. Roberto Alencar Costa',     'CRM/PE-34567', 3,  280.00),
    -- Ortopedia (id=4)
    ('Dra. Fernanda Cavalcante',      'CRM/PE-45678', 4,  300.00),
    ('Dr. Gustavo Henrique Lopes',    'CRM/PE-45679', 4,  320.00),
    -- Pediatria (id=5)
    ('Dr. Paulo Sérgio Lira',         'CRM/PE-56789', 5,  220.00),
    ('Dra. Juliana Barros Melo',      'CRM/PE-56790', 5,  220.00),
    -- Ginecologia (id=6)
    ('Dra. Mariana Nogueira Costa',   'CRM/PE-67890', 6,  250.00),
    ('Dra. Renata Figueiredo',        'CRM/PE-67891', 6,  270.00),
    -- Neurologia (id=7)
    ('Dr. Antônio Henrique Bastos',   'CRM/PE-78901', 7,  400.00),
    -- Psiquiatria (id=8)
    ('Dra. Luciana Freire Sampaio',   'CRM/PE-89012', 8,  380.00),
    -- Oftalmologia (id=9)
    ('Dr. Marcelo Tenório Souza',     'CRM/PE-90123', 9,  260.00),
    -- Endocrinologia (id=10)
    ('Dra. Tatiana Barros Araújo',    'CRM/PE-01234', 10, 320.00);
