# 🏥 Sistema de Agendamento de Consultas — Clínica Multi-Especialidades
## Documento Mestre de Arquitetura e Planejamento

> **Versão:** 1.0 | **Autor:** Arquitetura & Engenharia Sênior  
> **Stack:** Java 21 + Spring Boot 3 · PostgreSQL · Next.js 15 / TypeScript

---

## 1. VISÃO GERAL DA SOLUÇÃO

### 1.1 Contexto e Decisões Estratégicas

O sistema atende **dois perfis de usuário com acesso unificado**: pacientes (auto-cadastro, agendamento online) e funcionários (gestão completa da clínica). A autenticação é única — CPF + senha — e o sistema deriva permissões do papel (role) detectado no momento do login.

**Decisões técnicas principais:**

| Decisão | Escolha | Justificativa |
|---|---|---|
| Linguagem backend | Java 21 (LTS) | Requisito do teste; LTS garante suporte estendido |
| Framework backend | Spring Boot 3.3.x | Ecossistema maduro, suporte nativo a Jakarta EE |
| Banco de dados | PostgreSQL 16 | Open-source, robusto, compatível com SQL padrão Oracle |
| ORM | Spring Data JPA + Hibernate | Produtividade + controle fino de queries |
| Migrations | Flyway | Versionamento de schema rastreável em Git |
| Mapping DTOs | MapStruct | Zero reflection em runtime, performance máxima |
| Segurança | Spring Security + JWT (JJWT) | Stateless, escalável, padrão de mercado |
| Email | Spring Mail (Resend via SMTP) | Simples, confiável, sem SDK adicional |
| Documentação API | Springdoc OpenAPI 3 (Swagger UI) | Geração automática, testável em browser |
| Testes | JUnit 5 + Mockito + Testcontainers | Pirâmide completa: unitário + integração |
| Frontend | Next.js 15 + TypeScript + Tailwind | React maduro, SSR/SSG, tipagem forte |
| CEP | ViaCEP API (pública) | Gratuita, sem autenticação, cobertura nacional |
| Build | Maven | Padrão Java enterprise, CI/CD simples |
| Container local | Docker Compose | Ambiente idêntico entre devs e produção |

### 1.2 Compatibilidade com Oracle

Para garantir compatibilidade (diferencial do teste), toda a camada SQL seguirá práticas agnósticas ao banco:
- Uso de `VARCHAR2`-compatível (mapeado como `VARCHAR` no PostgreSQL)
- Sequences explícitas ao invés de `SERIAL`
- Queries HQL/JPQL sempre que possível
- `application-oracle.yml` com configuração alternativa para Oracle XE

---

## 2. ARQUITETURA DO SISTEMA

### 2.1 Diagrama de Camadas

```
┌─────────────────────────────────────────────────────┐
│                   FRONTEND (Next.js 15)              │
│         Vercel / NGINX · TypeScript · Tailwind       │
└────────────────────────┬────────────────────────────┘
                         │ HTTPS (JSON/REST)
┌────────────────────────▼────────────────────────────┐
│              GATEWAY / CORS FILTER                   │
│         Spring Security · JWT Bearer Token           │
├──────────────────────────────────────────────────────┤
│  CONTROLLERS (REST Layer)                            │
│  AuthController · PacienteController                 │
│  FuncionarioController · ProfissionalController      │
│  AgendamentoController · ClinicaController           │
│  PlanoSaudeController · IndisponibilidadeController  │
│  CepController                                       │
├──────────────────────────────────────────────────────┤
│  SERVICES (Business Logic Layer)                     │
│  AgendamentoService · HorarioService                 │
│  AuthService · NotificacaoEmailService               │
│  PacienteService · CepService                        │
├──────────────────────────────────────────────────────┤
│  REPOSITORIES (Data Access Layer)                    │
│  Spring Data JPA Repositories + Custom Queries       │
├──────────────────────────────────────────────────────┤
│  DATABASE                                            │
│  PostgreSQL 16 (dev) · Oracle XE (compatível)        │
└──────────────────────────────────────────────────────┘
```

### 2.2 Estrutura de Pacotes Backend

```
src/
├── main/
│   ├── java/com/clinica/agendamento/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   ├── MailConfig.java
│   │   │   ├── OpenApiConfig.java
│   │   │   └── AppProperties.java
│   │   │
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── ClinicaController.java
│   │   │   ├── PacienteController.java
│   │   │   ├── FuncionarioController.java
│   │   │   ├── ProfissionalController.java
│   │   │   ├── AgendamentoController.java
│   │   │   ├── PlanoSaudeController.java
│   │   │   ├── IndisponibilidadeController.java
│   │   │   └── CepController.java
│   │   │
│   │   ├── domain/                         ← Entidades JPA
│   │   │   ├── Clinica.java
│   │   │   ├── Paciente.java
│   │   │   ├── Funcionario.java
│   │   │   ├── Profissional.java
│   │   │   ├── Especialidade.java
│   │   │   ├── PlanoSaude.java
│   │   │   ├── Agendamento.java
│   │   │   ├── CancelamentoAgendamento.java
│   │   │   ├── Indisponibilidade.java
│   │   │   └── enums/
│   │   │       ├── TipoAtendimento.java    (PRESENCIAL, VIRTUAL)
│   │   │       ├── TipoConsulta.java       (CONSULTA, RETORNO, ENCAIXE)
│   │   │       ├── StatusAgendamento.java  (AGENDADO, CONFIRMADO, CANCELADO,
│   │   │       │                            CONCLUIDO, NAO_COMPARECEU)
│   │   │       ├── FormaPagamento.java     (PLANO, PARTICULAR)
│   │   │       └── RoleUsuario.java        (PACIENTE, FUNCIONARIO, ADMIN)
│   │   │
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── CadastrarPacienteRequest.java
│   │   │   │   ├── CadastrarFuncionarioRequest.java
│   │   │   │   ├── CadastrarProfissionalRequest.java
│   │   │   │   ├── CriarAgendamentoRequest.java
│   │   │   │   └── CancelarAgendamentoRequest.java
│   │   │   └── response/
│   │   │       ├── TokenResponse.java
│   │   │       ├── PacienteResponse.java
│   │   │       ├── AgendamentoResponse.java
│   │   │       ├── HorariosDisponiveisResponse.java
│   │   │       └── ErroResponse.java
│   │   │
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── NegocioException.java
│   │   │   ├── RecursoNaoEncontradoException.java
│   │   │   ├── HorarioIndisponivelException.java
│   │   │   └── AutenticacaoException.java
│   │   │
│   │   ├── mapper/                         ← MapStruct
│   │   │   ├── PacienteMapper.java
│   │   │   ├── AgendamentoMapper.java
│   │   │   └── ProfissionalMapper.java
│   │   │
│   │   ├── repository/
│   │   │   ├── PacienteRepository.java
│   │   │   ├── AgendamentoRepository.java
│   │   │   ├── ProfissionalRepository.java
│   │   │   ├── FuncionarioRepository.java
│   │   │   └── IndisponibilidadeRepository.java
│   │   │
│   │   ├── security/
│   │   │   ├── JwtService.java
│   │   │   ├── JwtAuthFilter.java
│   │   │   └── UserDetailsServiceImpl.java
│   │   │
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── AgendamentoService.java
│   │   │   ├── HorarioService.java          ← Lógica de slots
│   │   │   ├── PacienteService.java
│   │   │   ├── FuncionarioService.java
│   │   │   ├── ProfissionalService.java
│   │   │   ├── CepService.java
│   │   │   └── NotificacaoEmailService.java
│   │   │
│   │   └── validation/                     ← Validators customizados
│   │       ├── CpfValidator.java
│   │       ├── CrmValidator.java
│   │       ├── TelefoneValidator.java
│   │       └── SenhaValidator.java
│   │
│   └── resources/
│       ├── db/migration/                   ← Flyway scripts
│       │   ├── V1__create_clinica.sql
│       │   ├── V2__create_especialidade.sql
│       │   ├── V3__create_plano_saude.sql
│       │   ├── V4__create_profissional.sql
│       │   ├── V5__create_funcionario.sql
│       │   ├── V6__create_paciente.sql
│       │   ├── V7__create_agendamento.sql
│       │   ├── V8__create_cancelamento.sql
│       │   ├── V9__create_indisponibilidade.sql
│       │   ├── V10__seed_especialidades.sql
│       │   ├── V11__seed_profissionais.sql
│       │   ├── V12__seed_funcionarios.sql
│       │   └── V13__seed_planos_saude.sql
│       ├── templates/email/
│       │   ├── confirmacao-agendamento.html
│       │   └── cancelamento-agendamento.html
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-oracle.yml
│
└── test/
    ├── unit/service/
    │   ├── AgendamentoServiceTest.java
    │   ├── HorarioServiceTest.java
    │   └── PacienteServiceTest.java
    └── integration/
        ├── AgendamentoControllerIT.java
        └── AuthControllerIT.java
```

---

## 3. MODELAGEM DO BANCO DE DADOS

### 3.1 Diagrama de Entidades (ERD Simplificado)

```
clinica (1)
    │
    └── configuração global do sistema

plano_saude (N) ─────────────────── paciente (N)
                                         │
especialidade (1) ── profissional (N)    │
                          │              │
                          └──── agendamento ────┘
                                     │
                          funcionario (N, quem agendou)
                                     │
                          cancelamento_agendamento (0..1)

indisponibilidade ── profissional (nullable)
```

### 3.2 Scripts DDL — Entidades Principais

```sql
-- V1: Clínica
CREATE TABLE clinica (
    id          BIGSERIAL PRIMARY KEY,
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
    uf          VARCHAR(2),
    ativo       BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- V2: Especialidade
CREATE TABLE especialidade (
    id        BIGSERIAL PRIMARY KEY,
    nome      VARCHAR(100) NOT NULL UNIQUE,
    descricao VARCHAR(300),
    ativo     BOOLEAN DEFAULT TRUE
);

-- V3: Plano de Saúde
CREATE TABLE plano_saude (
    id     BIGSERIAL PRIMARY KEY,
    nome   VARCHAR(150) NOT NULL,
    codigo VARCHAR(50),
    ativo  BOOLEAN DEFAULT TRUE
);

-- V4: Profissional
CREATE TABLE profissional (
    id               BIGSERIAL PRIMARY KEY,
    nome             VARCHAR(150) NOT NULL,
    crm              VARCHAR(20)  NOT NULL UNIQUE,
    especialidade_id BIGINT       NOT NULL REFERENCES especialidade(id),
    valor_consulta   NUMERIC(10,2),
    ativo            BOOLEAN DEFAULT TRUE,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

-- V5: Funcionário
CREATE TABLE funcionario (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    cpf         VARCHAR(14)  NOT NULL UNIQUE,
    email       VARCHAR(150),
    telefone    VARCHAR(20),
    senha_hash  VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL DEFAULT 'FUNCIONARIO', -- FUNCIONARIO | ADMIN
    ativo       BOOLEAN DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- V6: Paciente
CREATE TABLE paciente (
    id               BIGSERIAL PRIMARY KEY,
    nome_completo    VARCHAR(200) NOT NULL,
    cpf              VARCHAR(14)  NOT NULL UNIQUE,
    data_nascimento  DATE,
    email            VARCHAR(150) NOT NULL,
    telefone         VARCHAR(20)  NOT NULL,
    senha_hash       VARCHAR(255) NOT NULL,
    cep              VARCHAR(10),
    logradouro       VARCHAR(200),
    numero           VARCHAR(20),
    complemento      VARCHAR(100),
    bairro           VARCHAR(100),
    cidade           VARCHAR(100),
    uf               VARCHAR(2),
    plano_saude_id   BIGINT REFERENCES plano_saude(id),
    ativo            BOOLEAN DEFAULT TRUE,
    created_at       TIMESTAMP DEFAULT NOW(),
    updated_at       TIMESTAMP DEFAULT NOW()
);

-- V7: Agendamento
CREATE TABLE agendamento (
    id                BIGSERIAL PRIMARY KEY,
    paciente_id       BIGINT NOT NULL REFERENCES paciente(id),
    profissional_id   BIGINT NOT NULL REFERENCES profissional(id),
    funcionario_id    BIGINT REFERENCES funcionario(id),        -- quem agendou (null = próprio paciente online)
    data_consulta     DATE   NOT NULL,
    hora_consulta     TIME   NOT NULL,
    tipo_atendimento  VARCHAR(20) NOT NULL,                     -- PRESENCIAL | VIRTUAL
    tipo_consulta     VARCHAR(20) NOT NULL DEFAULT 'CONSULTA',  -- CONSULTA | RETORNO | ENCAIXE
    status            VARCHAR(20) NOT NULL DEFAULT 'AGENDADO',
    forma_pagamento   VARCHAR(20) NOT NULL DEFAULT 'PARTICULAR',
    plano_saude_id    BIGINT REFERENCES plano_saude(id),
    necessita_nf      BOOLEAN DEFAULT FALSE,
    nf_enviar_email   BOOLEAN DEFAULT FALSE,
    observacao        VARCHAR(500),
    created_at        TIMESTAMP DEFAULT NOW(),
    updated_at        TIMESTAMP DEFAULT NOW(),
    -- Constraint: profissional livre no horário
    CONSTRAINT uq_profissional_horario UNIQUE (profissional_id, data_consulta, hora_consulta),
    -- Constraint: paciente livre no horário
    CONSTRAINT uq_paciente_horario UNIQUE (paciente_id, data_consulta, hora_consulta)
);

-- Index de performance
CREATE INDEX idx_agendamento_data ON agendamento(data_consulta);
CREATE INDEX idx_agendamento_paciente ON agendamento(paciente_id);
CREATE INDEX idx_agendamento_profissional ON agendamento(profissional_id);
CREATE INDEX idx_agendamento_status ON agendamento(status);

-- V8: Cancelamento
CREATE TABLE cancelamento_agendamento (
    id                    BIGSERIAL PRIMARY KEY,
    agendamento_id        BIGINT NOT NULL REFERENCES agendamento(id),
    motivo                VARCHAR(500) NOT NULL,
    cancelado_por_tipo    VARCHAR(20) NOT NULL,  -- PACIENTE | FUNCIONARIO | PROFISSIONAL
    cancelado_por_id      BIGINT NOT NULL,
    created_at            TIMESTAMP DEFAULT NOW()
);

-- V9: Indisponibilidade
CREATE TABLE indisponibilidade (
    id                   BIGSERIAL PRIMARY KEY,
    profissional_id      BIGINT REFERENCES profissional(id),  -- NULL = clínica toda
    data                 DATE NOT NULL,
    hora_inicio          TIME,                                 -- NULL = dia inteiro
    hora_fim             TIME,
    motivo               VARCHAR(300) NOT NULL,
    registrado_por_tipo  VARCHAR(20) NOT NULL,  -- FUNCIONARIO | ADMIN
    registrado_por_id    BIGINT NOT NULL,
    created_at           TIMESTAMP DEFAULT NOW()
);
```

### 3.3 Dados Seed (Exemplos Fictícios)

```sql
-- V10: Especialidades
INSERT INTO especialidade (nome, descricao) VALUES
  ('Clínica Geral',     'Atendimento geral de saúde'),
  ('Cardiologia',       'Especialista em coração e sistema cardiovascular'),
  ('Dermatologia',      'Especialista em pele, cabelos e unhas'),
  ('Ortopedia',         'Especialista em sistema músculo-esquelético'),
  ('Pediatria',         'Atendimento médico a crianças e adolescentes'),
  ('Ginecologia',       'Saúde da mulher'),
  ('Neurologia',        'Especialista em sistema nervoso'),
  ('Psiquiatria',       'Saúde mental e transtornos psiquiátricos'),
  ('Oftalmologia',      'Especialista em olhos e visão'),
  ('Endocrinologia',    'Especialista em hormônios e metabolismo');

-- V11: Profissionais
INSERT INTO profissional (nome, crm, especialidade_id, valor_consulta) VALUES
  ('Dr. Carlos Alberto Mendes',    'CRM/PE-12345', 1,  180.00),
  ('Dra. Ana Beatriz Fontes',      'CRM/PE-23456', 2,  350.00),
  ('Dr. Roberto Alencar',          'CRM/PE-34567', 3,  280.00),
  ('Dra. Fernanda Cavalcante',     'CRM/PE-45678', 4,  300.00),
  ('Dr. Paulo Sérgio Lira',        'CRM/PE-56789', 5,  220.00),
  ('Dra. Mariana Nogueira Costa',  'CRM/PE-67890', 6,  250.00),
  ('Dr. Antônio Henrique Bastos',  'CRM/PE-78901', 7,  400.00),
  ('Dra. Luciana Freire',          'CRM/PE-89012', 8,  380.00),
  ('Dr. Marcelo Tenório',          'CRM/PE-90123', 9,  260.00),
  ('Dra. Tatiana Barros',          'CRM/PE-01234', 10, 320.00);

-- V12: Funcionários
INSERT INTO funcionario (nome, cpf, email, senha_hash, role) VALUES
  ('Maria das Graças Silva',  '123.456.789-01', 'mgraças@clinica.com.br',  '$2a$12$hash...', 'ADMIN'),
  ('João Pedro Alves',        '234.567.890-12', 'joaopedro@clinica.com.br', '$2a$12$hash...', 'FUNCIONARIO'),
  ('Carla Cristina Rocha',    '345.678.901-23', 'carlarocha@clinica.com.br','$2a$12$hash...', 'FUNCIONARIO');

-- V13: Planos de Saúde
INSERT INTO plano_saude (nome, codigo) VALUES
  ('Unimed Recife',        'UNI-PE-001'),
  ('Amil',                 'AMIL-001'),
  ('SulAmérica',           'SULA-001'),
  ('Bradesco Saúde',       'BRAD-001'),
  ('Porto Seguro Saúde',   'PORTO-001'),
  ('Hapvida',              'HAPV-001'),
  ('NotreDame Intermédica', 'NDIM-001'),
  ('Particular',           null);
```

---

## 4. REGRAS DE NEGÓCIO

### 4.1 Cálculo de Slots de Horário

```
Segunda a Sexta: 07:00 → 20:00
Sábado:          08:00 → 13:00
Domingo/Feriado: INDISPONÍVEL

Consulta:  30 minutos
Intervalo: 10 minutos entre consultas
Bloco:     40 minutos por slot

Slots Segunda-Sexta (26 por dia por profissional):
07:00 | 07:40 | 08:20 | 09:00 | 09:40 | 10:20 | 11:00 | 11:40
12:20 | 13:00 | 13:40 | 14:20 | 15:00 | 15:40 | 16:20 | 17:00
17:40 | 18:20 | 19:00 | 19:40

Slots Sábado (8 por dia por profissional):
08:00 | 08:40 | 09:20 | 10:00 | 10:40 | 11:20 | 12:00 | 12:40
```

**Algoritmo HorarioService:**
1. Verificar se a data é dia útil (não domingo, não feriado)
2. Gerar todos os slots do dia conforme regra
3. Filtrar slots que já têm agendamento ativo para aquele profissional
4. Filtrar slots com indisponibilidade cadastrada
5. Filtrar slots no passado (data+hora < agora)
6. Retornar lista de slots disponíveis com flag `disponivel: true/false`

### 4.2 Validações de Negócio

```
AGENDAMENTO:
✓ Data futura obrigatória
✓ Dentro do horário da clínica
✓ Profissional sem conflito de horário
✓ Paciente sem conflito de horário
✓ Slot não bloqueado por indisponibilidade
✓ Profissional ativo no sistema

CANCELAMENTO:
✓ Motivo obrigatório (mínimo 10 caracteres)
✓ Registrar quem cancelou (paciente ou funcionário)
✓ Status → CANCELADO
✓ Slot liberado automaticamente (constraint UNIQUE liberada ao mudar status)
✓ Email de confirmação ao paciente

SENHA:
✓ Mínimo 10 caracteres
✓ Pelo menos 1 letra maiúscula
✓ Pelo menos 1 letra minúscula
✓ Pelo menos 1 número
✓ Pelo menos 1 caractere especial (!@#$%^&*)
✓ Verificação de unicidade no banco (não pode repetir senha de outro usuário — conforme enunciado)

CPF:
✓ Algoritmo de validação dos dígitos verificadores
✓ Formato: 000.000.000-00

CRM:
✓ Formato: CRM/UF-NNNNN

Email:
✓ Regex padrão RFC 5322

Telefone:
✓ Formato: (XX) NNNNN-NNNN ou (XX) NNNN-NNNN
```

### 4.3 Matriz de Permissões (RBAC)

| Funcionalidade | PACIENTE | FUNCIONARIO | ADMIN |
|---|:---:|:---:|:---:|
| Ver/editar próprio perfil | ✅ | ✅ | ✅ |
| Criar agendamento (próprio) | ✅ | ✅ | ✅ |
| Criar agendamento (para terceiros) | ❌ | ✅ | ✅ |
| Cancelar próprio agendamento | ✅ | ✅ | ✅ |
| Cancelar agendamento de paciente | ❌ | ✅ | ✅ |
| Listar todos agendamentos | ❌ | ✅ | ✅ |
| Ver próprios agendamentos | ✅ | ✅ | ✅ |
| Cadastrar paciente | ❌ | ✅ | ✅ |
| Listar pacientes | ❌ | ✅ | ✅ |
| Cadastrar funcionário | ❌ | ❌ | ✅ |
| Gerenciar profissionais | ❌ | ❌ | ✅ |
| Gerenciar planos de saúde | ❌ | ❌ | ✅ |
| Gerenciar clínica | ❌ | ❌ | ✅ |
| Bloquear horários | ❌ | ✅ | ✅ |
| Enviar NF/Recibo por email | ❌ | ✅ | ✅ |

---

## 5. API REST — ENDPOINTS

### 5.1 Autenticação

```
POST   /api/v1/auth/login              → TokenResponse (JWT + role + nome)
POST   /api/v1/auth/recuperar-senha    → Envia email com token de reset
POST   /api/v1/auth/redefinir-senha    → Nova senha com token válido
GET    /api/v1/auth/me                 → Dados do usuário logado
```

### 5.2 Pacientes

```
POST   /api/v1/pacientes               → Cadastrar (público)
GET    /api/v1/pacientes               → Listar com paginação e busca [FUNC]
GET    /api/v1/pacientes/{id}          → Buscar por ID [FUNC ou próprio]
PUT    /api/v1/pacientes/{id}          → Atualizar [FUNC ou próprio]
PATCH  /api/v1/pacientes/{id}/ativar   → Ativar/Desativar [ADMIN]
```

### 5.3 Funcionários

```
POST   /api/v1/funcionarios            → Cadastrar [ADMIN]
GET    /api/v1/funcionarios            → Listar [ADMIN]
GET    /api/v1/funcionarios/{id}       → Buscar [ADMIN]
PUT    /api/v1/funcionarios/{id}       → Atualizar [ADMIN]
DELETE /api/v1/funcionarios/{id}       → Desativar logicamente [ADMIN]
```

### 5.4 Profissionais

```
POST   /api/v1/profissionais           → Cadastrar [ADMIN]
GET    /api/v1/profissionais           → Listar (público, para agendamento)
GET    /api/v1/profissionais/{id}      → Buscar
PUT    /api/v1/profissionais/{id}      → Atualizar [ADMIN]
DELETE /api/v1/profissionais/{id}      → Desativar [ADMIN]
```

### 5.5 Agendamentos (Core)

```
GET    /api/v1/agendamentos/horarios-disponiveis
         ?profissionalId=&data=YYYY-MM-DD    → Lista slots livres

POST   /api/v1/agendamentos                  → Criar agendamento
GET    /api/v1/agendamentos                  → Listar (com filtros) [FUNC]
GET    /api/v1/agendamentos/meus             → Listar do paciente logado
GET    /api/v1/agendamentos/{id}             → Detalhe
PATCH  /api/v1/agendamentos/{id}/cancelar    → Cancelar com motivo
PATCH  /api/v1/agendamentos/{id}/confirmar   → Confirmar [FUNC]
PATCH  /api/v1/agendamentos/{id}/concluir    → Concluir [FUNC]
POST   /api/v1/agendamentos/{id}/enviar-nf   → Enviar NF por email [FUNC]

Filtros em GET /agendamentos:
?pacienteId=&profissionalId=&status=&dataInicio=&dataFim=
&tipoConsulta=&tipoAtendimento=&page=&size=&sort=
```

### 5.6 Indisponibilidades

```
POST   /api/v1/indisponibilidades          → Bloquear horário [FUNC]
GET    /api/v1/indisponibilidades          → Listar bloqueios [FUNC]
DELETE /api/v1/indisponibilidades/{id}     → Remover bloqueio [FUNC]
```

### 5.7 Utilitários

```
GET    /api/v1/cep/{cep}                   → Consultar ViaCEP (proxy)
GET    /api/v1/clinica                     → Dados da clínica
PUT    /api/v1/clinica                     → Atualizar clínica [ADMIN]
GET    /api/v1/planos-saude                → Listar planos (público)
POST   /api/v1/planos-saude               → Criar [ADMIN]
PUT    /api/v1/planos-saude/{id}           → Atualizar [ADMIN]
```

### 5.8 Formato Padrão de Resposta

```json
// Sucesso
{
  "success": true,
  "data": { ... },
  "timestamp": "2025-06-27T14:30:00"
}

// Erro de validação
{
  "success": false,
  "status": 422,
  "erro": "VALIDACAO",
  "mensagem": "Dados inválidos",
  "campos": {
    "cpf": "CPF inválido",
    "email": "Email é obrigatório"
  },
  "timestamp": "2025-06-27T14:30:00"
}

// Erro de negócio
{
  "success": false,
  "status": 409,
  "erro": "HORARIO_INDISPONIVEL",
  "mensagem": "O horário 14:00 do dia 28/06/2025 já está ocupado para este profissional.",
  "timestamp": "2025-06-27T14:30:00"
}
```

---

## 6. SEGURANÇA — JWT

### 6.1 Fluxo de Autenticação

```
1. POST /auth/login { cpf, senha }
2. Backend identifica se é PACIENTE ou FUNCIONARIO (ou ADMIN)
3. Valida senha com BCrypt
4. Gera JWT com: sub=cpf, role=ROLE_PACIENTE|ROLE_FUNCIONARIO|ROLE_ADMIN,
                 userId=id, nome=nome, exp=24h
5. Frontend armazena token em cookie httpOnly ou localStorage
6. Todas requisições: Authorization: Bearer <token>
7. JwtAuthFilter valida token e injeta Authentication no SecurityContext
```

### 6.2 Configuração Spring Security

```java
// Rotas públicas (sem token):
/api/v1/auth/**
POST /api/v1/pacientes
GET  /api/v1/profissionais
GET  /api/v1/planos-saude
GET  /api/v1/agendamentos/horarios-disponiveis

// Requer PACIENTE ou superior:
GET/PUT /api/v1/pacientes/{id} (com verificação de propriedade)
POST /api/v1/agendamentos
GET  /api/v1/agendamentos/meus

// Requer FUNCIONARIO ou superior:
GET  /api/v1/agendamentos
GET  /api/v1/pacientes
POST /api/v1/indisponibilidades

// Requer ADMIN apenas:
POST /api/v1/funcionarios
POST /api/v1/profissionais
PUT  /api/v1/clinica
```

---

## 7. TESTES AUTOMATIZADOS

### 7.1 Estratégia

```
Pirâmide de Testes:

    /\          Integration Tests (Controller → DB)
   /  \         Testcontainers + PostgreSQL real
  /────\
 /      \       Service Tests (Unit)
/────────\      JUnit 5 + Mockito
          
Unitários:      ~30 testes  (regras de negócio puras)
Integração:     ~15 testes  (fluxos completos HTTP)
```

### 7.2 Casos de Teste Prioritários

```java
// HorarioServiceTest.java
✓ deveRetornarSlotsCorretosPara_SegundaAsSete()
✓ deveRetornarSlotsCorretosPara_Sabado()
✓ naoDeveRetornarSlotJaAgendado()
✓ naoDeveRetornarSlotComIndisponibilidade()
✓ naoDeveRetornarSlotNoPassado()
✓ naoDeveRetornarSlots_ParaDomingo()

// AgendamentoServiceTest.java
✓ deveCriarAgendamentoComSucesso()
✓ deveLancarExcecao_QuandoProfissionalOcupado()
✓ deveLancarExcecao_QuandoPacienteOcupado()
✓ deveLancarExcecao_QuandoDataNoPassado()
✓ deveLancarExcecao_QuandoForaDoHorario()
✓ deveCancelarAgendamentoComMotivo()
✓ deveLancarExcecao_CancelarSemMotivo()
✓ deveMudarStatusParaCancelado_AoCancelar()
✓ deveLiberarSlot_AoCancelar()

// PacienteServiceTest.java
✓ deveCadastrarPacienteComSucesso()
✓ deveLancarExcecao_CpfDuplicado()
✓ deveLancarExcecao_SenhaFraca()
✓ deveLancarExcecao_CpfInvalido()

// AuthServiceTest.java
✓ deveAutenticarPacienteComSucesso()
✓ deveAutenticarFuncionarioComSucesso()
✓ deveLancarExcecao_SenhaIncorreta()
✓ deveGerarJwtComRoleCorreta()
```

---

## 8. FRONTEND — NEXT.JS 15

### 8.1 Estrutura de Páginas

```
app/
├── (public)/
│   ├── login/              → Tela de login CPF + senha
│   ├── cadastro/           → Cadastro de novo paciente
│   └── recuperar-senha/    → Fluxo de reset de senha
│
├── (paciente)/             → Layout com menu de paciente
│   ├── dashboard/          → Próximos agendamentos
│   ├── perfil/             → Ver/editar dados
│   ├── agendamentos/
│   │   ├── page.tsx        → Lista de agendamentos do paciente
│   │   ├── novo/           → Criar novo agendamento
│   │   └── [id]/           → Detalhe do agendamento
│   └── agendamentos/[id]/cancelar/
│
└── (funcionario)/          → Layout com menu completo
    ├── dashboard/           → Visão geral + agenda do dia
    ├── agendamentos/
    │   ├── page.tsx         → Lista com filtros avançados
    │   ├── novo/
    │   └── [id]/
    ├── pacientes/
    │   ├── page.tsx
    │   └── [id]/
    ├── profissionais/
    ├── funcionarios/        → Apenas ADMIN
    ├── planos-saude/
    ├── clinica/
    └── indisponibilidades/
```

### 8.2 Design System — Clínica Médica

**Paleta de Cores:**
```css
--primary:        #1B4F8A   /* Azul médico profissional */
--primary-light:  #2D6CC0   /* Hover e variações */
--primary-dark:   #0F3260   /* Texto em fundo claro */
--secondary:      #00897B   /* Verde saúde — CTAs secundários */
--accent:         #E3F2FD   /* Background cards suaves */
--warning:        #F59E0B   /* Alertas */
--danger:         #DC2626   /* Erros e cancelamentos */
--success:        #16A34A   /* Confirmações */
--neutral-50:     #F8FAFC   /* Background páginas */
--neutral-100:    #F1F5F9   /* Background cards */
--neutral-300:    #CBD5E1   /* Bordas */
--neutral-600:    #475569   /* Texto secundário */
--neutral-900:    #0F172A   /* Texto principal */
```

**Typography:**
```css
/* Display / Headings */
font-family: 'Plus Jakarta Sans', sans-serif;

/* Body */
font-family: 'Inter', sans-serif;

/* Dados clínicos / tabelas */
font-family: 'IBM Plex Mono', monospace;
```

**Componentes-chave:**
- `<SlotPicker />` — grade visual de horários disponíveis com status por cor
- `<AgendamentoCard />` — card de agendamento com status colorido
- `<CpfInput />` — máscara automática + validação em tempo real
- `<CepInput />` — busca ViaCEP ao completar 8 dígitos
- `<SenhaForce />` — barra de força de senha em tempo real
- `<StatusBadge />` — badge colorido por status de agendamento

### 8.3 Fluxo de Agendamento (UX)

```
Etapa 1: Selecionar Especialidade
         ↓
Etapa 2: Selecionar Profissional
         (card com foto, nome, CRM, valor da consulta)
         ↓
Etapa 3: Selecionar Data (calendário)
         (dias sem disponibilidade ficam cinza/bloqueados)
         ↓
Etapa 4: Selecionar Horário
         (grid visual — disponível: azul | ocupado: cinza)
         ↓
Etapa 5: Confirmar dados
         (Tipo: Consulta/Retorno | Atendimento: Presencial/Virtual)
         (Pagamento: Plano/Particular | NF: Sim/Não | Como: Imprimir/Email)
         ↓
Etapa 6: Sucesso
         (card de confirmação + email disparado automaticamente)
```

---

## 9. EMAIL NOTIFICATIONS

### 9.1 Template HTML — Confirmação de Agendamento

```
[LOGO DA CLÍNICA]

✅ Agendamento Confirmado!

Olá, [NOME DO PACIENTE],

Seu agendamento foi confirmado com sucesso. 
Confira os detalhes abaixo:

┌─────────────────────────────────────┐
│  📅 Data:        [DATA DD/MM/YYYY]  │
│  ⏰ Horário:     [HORA HH:MM]       │
│  👨‍⚕️ Profissional: [NOME]           │
│  🩺 Especialidade:[ESPECIALIDADE]   │
│  📍 Atendimento: [PRESENCIAL/VIRTUAL]│
│  💳 Pagamento:   [PLANO/PARTICULAR] │
└─────────────────────────────────────┘

Em caso de dúvidas, entre em contato:
📞 [TELEFONE] | 💬 [WHATSAPP] | ✉️ [EMAIL]

[NOME DA CLÍNICA]
[ENDEREÇO COMPLETO]
```

### 9.2 Template HTML — Cancelamento

```
[LOGO DA CLÍNICA]

❌ Agendamento Cancelado

Olá, [NOME DO PACIENTE],

Seu agendamento foi cancelado conforme solicitado.

DADOS DO AGENDAMENTO CANCELADO:
Data/Hora: [DATA] às [HORA]
Profissional: [NOME]
Motivo: [MOTIVO DO CANCELAMENTO]

Deseja reagendar? Acesse nosso sistema online:
[LINK DO SISTEMA]

[NOME DA CLÍNICA]
```

---

## 10. DOCKER E AMBIENTE DE DESENVOLVIMENTO

### 10.1 docker-compose.yml

```yaml
version: '3.9'
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: clinica_agendamento
      POSTGRES_USER: clinica_user
      POSTGRES_PASSWORD: clinica_pass
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U clinica_user"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/clinica_agendamento
      SPRING_DATASOURCE_USERNAME: clinica_user
      SPRING_DATASOURCE_PASSWORD: clinica_pass
      JWT_SECRET: sua-chave-secreta-minimo-256-bits
      MAIL_HOST: smtp.resend.com
      MAIL_PORT: 465
      MAIL_USERNAME: resend
      MAIL_PASSWORD: re_xxxxxxxxxxxx
    depends_on:
      postgres:
        condition: service_healthy

  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080/api/v1
    depends_on:
      - backend

volumes:
  pgdata:
```

---

## 11. CRONOGRAMA DE DESENVOLVIMENTO

### Fase 1 — Foundation (Dias 1–2)
```
✅ Setup Spring Boot 3 + Maven
✅ application.yml + docker-compose
✅ Flyway migrations V1–V9
✅ Entities JPA + Enums
✅ Flyway seed data V10–V13
✅ GlobalExceptionHandler
✅ OpenAPI (Swagger UI) config
```

### Fase 2 — Auth & Usuários (Dias 3–4)
```
✅ Spring Security + JWT
✅ POST /auth/login (paciente + funcionário)
✅ POST /pacientes (cadastro público)
✅ GET/PUT /pacientes/{id}
✅ POST/GET/PUT/DELETE /funcionarios (ADMIN)
✅ Validadores: CPF, senha, email, telefone
✅ GET /cep/{cep} → proxy ViaCEP
```

### Fase 3 — Core de Agendamento (Dias 5–7)
```
✅ HorarioService — cálculo de slots
✅ GET /agendamentos/horarios-disponiveis
✅ POST /agendamentos — criar com todas as validações
✅ PATCH /agendamentos/{id}/cancelar
✅ GET /agendamentos — listagem com filtros
✅ Indisponibilidades CRUD
✅ NotificacaoEmailService — templates HTML
```

### Fase 4 — Profissionais & Clínica (Dia 8)
```
✅ CRUD Profissionais (ADMIN)
✅ CRUD Planos de Saúde (ADMIN)
✅ GET/PUT Clínica (ADMIN)
✅ GET /especialidades
```

### Fase 5 — Testes (Dia 9)
```
✅ HorarioServiceTest (6 casos)
✅ AgendamentoServiceTest (9 casos)
✅ PacienteServiceTest (4 casos)
✅ AuthServiceTest (4 casos)
✅ AgendamentoControllerIT (integração)
✅ AuthControllerIT (integração)
```

### Fase 6 — Frontend (Dias 10–13)
```
✅ Setup Next.js 15 + Tailwind + Axios
✅ Contexto de autenticação (JWT + roles)
✅ Telas públicas: Login, Cadastro, Recuperar senha
✅ Fluxo de agendamento (5 etapas)
✅ Dashboard paciente
✅ Dashboard funcionário
✅ Listagem com filtros
✅ Cancelamento com modal de motivo
✅ Gerenciamento (profissionais, funcionários)
```

### Fase 7 — Documentação & Entrega (Dia 14)
```
✅ README.md completo (setup, execução, endpoints)
✅ DECISOES.md (3 perguntas respondidas)
✅ Revisão final + testes manuais
✅ Push GitHub com histórico de commits limpo
```

---

## 12. COMMITS — ESTRATÉGIA

```
feat: [scope] mensagem curta descritiva

Exemplos recomendados:
feat(auth): implement JWT authentication with role detection
feat(agendamento): add slot availability calculation service
feat(agendamento): implement conflict validation for scheduling
feat(email): add HTML confirmation email template
feat(paciente): add CPF and password validators
fix(horario): correct slot calculation for Saturday schedule
test(agendamento): add unit tests for conflict rules
docs: add complete README and DECISOES files
chore(docker): add docker-compose with postgres and backend
```

---

## 13. DECISOES.md — RASCUNHO

```markdown
# DECISOES.md

## 1. Principais Decisões Técnicas

**Autenticação por CPF único:**  
Um único endpoint `/auth/login` recebe CPF e senha. O sistema verifica primeiro 
na tabela de funcionários e depois em pacientes, determinando a role do token JWT.

**Separação data/hora:**  
`data_consulta DATE` e `hora_consulta TIME` separados no banco, conforme solicitado, 
facilitando queries de disponibilidade e formatação no frontend.

**Slots calculados dinamicamente:**  
Não armazenamos slots disponíveis; calculamos em runtime comparando regras de horário, 
agendamentos existentes e indisponibilidades registradas.

**Constraint UNIQUE no banco:**  
A regra de conflito de horário (profissional+data+hora e paciente+data+hora) 
é garantida tanto pela Service Layer quanto por constraints UNIQUE no banco, 
criando dupla proteção.

**Soft delete:**  
Nenhum dado é deletado fisicamente. Usamos `ativo = false` para desativações 
e o cancelamento mantém o registro com status `CANCELADO`.

## 2. O que priorizei e o que ficou de fora

**Priorizei:**
- Regras de negócio de agendamento (core do sistema)
- Segurança: JWT + BCrypt + validações de senha/CPF
- Cobertura de testes nas regras críticas
- Documentação clara e detalhada
- Seed data realista para demonstração

**Ficou de fora por tempo:**
- Impressão de NF/Recibo (tela de print)
- Recuperação de senha completa (apenas endpoint, sem frontend)
- Feriados nacionais (infraestrutura preparada, não populada)
- Upload de logomarca da clínica

## 3. Uso de IA

Utilizei IA para:
- Geração inicial dos templates HTML de email
- Sugestão de nomes fictícios para seed data
- Estruturação do documento de arquitetura

Validação: Todo código gerado foi revisado linha a linha, testado manualmente 
via Swagger UI e coberto por testes automatizados. As regras de negócio foram 
implementadas e validadas independentemente.
```

---

## 14. CHECKLIST FINAL DE ENTREGA

```
BACKEND:
[ ] Todos endpoints funcionando (testados via Swagger UI)
[ ] Todas validações ativas e retornando mensagens claras
[ ] Emails disparando em agendamento e cancelamento
[ ] Testes passando (mvn test)
[ ] Flyway migrations rodando na primeira inicialização
[ ] application.yml com variáveis de ambiente (não hardcoded)

FRONTEND:
[ ] Login funcionando para paciente e funcionário
[ ] Fluxo completo de agendamento testado
[ ] Cancelamento com motivo funcionando
[ ] Responsivo mobile e desktop

DOCUMENTAÇÃO:
[ ] README com: pré-requisitos, setup, execução, endpoints principais
[ ] DECISOES.md com as 3 respostas
[ ] Swagger UI acessível em /swagger-ui.html

GITHUB:
[ ] Repositório público com nome descritivo
[ ] .gitignore correto (sem target/, .env, *.class)
[ ] Histórico de commits limpo e descritivo
[ ] README exibindo corretamente na página do repositório
```
