# 🏥 Vita Clínica — Sistema de Agendamento de Consultas

> **Sistema completo de agendamento médico multi-especialidades**  
> Java 21 + Spring Boot 3.3 · PostgreSQL 16 · Next.js 15 · Railway + Vercel

---

## 📋 Sobre o Projeto

A **Vita Clínica** é um sistema web completo para agendamento de consultas médicas. O sistema permite que pacientes agendem consultas online e que funcionários gerenciem a agenda, com regras de negócio robustas e notificações por email automáticas.

---

## 🏗️ Estrutura do Monorepo

```
vita-clinica/
├── backend/           → API REST (Java 21 + Spring Boot 3.3)
├── frontend/          → Interface web (Next.js 15 + TypeScript)
├── docker-compose.yml → Banco local para desenvolvimento
├── .env.example       → Template de variáveis de ambiente
├── README.md          → Este arquivo
└── DECISOES.md        → Decisões técnicas do projeto
```

---

## ⚙️ Pré-requisitos

| Ferramenta | Versão | Download |
|---|---|---|
| Java JDK | 21 LTS | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org |
| Docker Desktop | 24+ | https://docker.com |
| Node.js | 18+ | https://nodejs.org |

---

## 🚀 Início Rápido

### 1. Clone e configure
```bash
git clone https://github.com/manuelfbfilho/vita-clinica.git
cd vita-clinica
cp .env.example .env
```

### 2. Suba o banco de dados
```bash
docker-compose up -d postgres
```

### 3. Inicie o backend
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# API disponível em http://localhost:8080/api/v1
# Swagger UI: http://localhost:8080/api/v1/swagger-ui.html
```

### 4. Inicie o frontend
```bash
cd frontend
npm install
cp .env.local.example .env.local
npm run dev
# App disponível em http://localhost:3000
```

---

## 🔐 Credenciais de Demonstração

| Perfil | CPF | Senha |
|---|---|---|
| Administrador | `000.000.000-01` | `Vita@2025#` |
| Funcionário | `000.000.000-02` | `Vita@2025#` |
| Paciente 1 | `111.111.111-11` | `Vita@2025#` |
| Paciente 2 | `222.222.222-22` | `Vita@2025#` |

---

## 📡 Endpoints da API

### Autenticação
| Método | Endpoint | Auth |
|---|---|---|
| POST | `/auth/login` | Público |

### Agendamentos
| Método | Endpoint | Auth |
|---|---|---|
| GET | `/agendamentos/horarios-disponiveis?profissionalId=&data=` | Público |
| POST | `/agendamentos` | JWT |
| GET | `/agendamentos/meus` | PACIENTE |
| GET | `/agendamentos` | FUNCIONARIO |
| GET | `/agendamentos/agenda-dia?data=` | FUNCIONARIO |
| PATCH | `/agendamentos/{id}/cancelar` | JWT |
| PATCH | `/agendamentos/{id}/confirmar` | FUNCIONARIO |
| PATCH | `/agendamentos/{id}/concluir` | FUNCIONARIO |

### Pacientes
| Método | Endpoint | Auth |
|---|---|---|
| POST | `/pacientes` | Público |
| GET | `/pacientes` | FUNCIONARIO |
| GET | `/pacientes/{id}` | JWT |
| PUT | `/pacientes/{id}` | JWT |

### Utilitários
| Endpoint | Auth |
|---|---|
| GET `/profissionais?especialidadeId=` | Público |
| GET `/especialidades` | Público |
| GET `/planos-saude` | Público |
| GET `/cep/{cep}` | Público |
| GET `/clinica` | Público |

---

## 📋 Regras de Negócio

### Horários de Atendimento
- **Segunda a Sexta:** 07:00 – 20:00
- **Sábado:** 08:00 – 13:00
- **Domingo:** Fechado

### Slots (40 min por bloco: 30 consulta + 10 intervalo)
- Segunda-Sexta: **19 slots/dia** por profissional
- Sábado: **7 slots/dia** por profissional

### Conflitos Bloqueados
- Mesmo profissional + mesma data + mesmo horário
- Mesmo paciente + mesma data + mesmo horário
- Agendamentos cancelados NÃO bloqueam o slot (partial unique index)

### Senha
- Mínimo 10 caracteres · 1 maiúscula · 1 minúscula · 1 número · 1 especial

---

## 🛠️ Stack Tecnológica

### Backend
| Tecnologia | Versão |
|---|---|
| Java | 21 LTS |
| Spring Boot | 3.3.5 |
| Spring Security 6 | JWT stateless |
| JJWT | 0.12.6 |
| Flyway | 10.x |
| MapStruct | 1.6.2 |
| Springdoc OpenAPI | 2.6.0 |
| BCrypt | Força 12 |

### Frontend
| Tecnologia | Versão |
|---|---|
| Next.js | 15.1 |
| React | 19.0 |
| TypeScript | 5.x |
| Tailwind CSS | 3.4 |
| Zustand | 5.0 |

---

## 🌐 Compatibilidade Oracle

O projeto inclui `application-oracle.yml` com Oracle Dialect e driver ojdbc11.  
Para ativar: `--spring.profiles.active=oracle`

---

## 🧪 Testes

```bash
cd backend && mvn test
```

- `HorarioServiceTest` — 6 casos (slots, domingo, passado, bloqueio)
- `AgendamentoServiceTest` — 4 casos (conflitos, cancelamento, permissões)
- `PacienteServiceTest` — 2 casos (CPF e email duplicado)

---

## 🔮 Planejado / Não Implementado

As seguintes funcionalidades foram projetadas mas não entregues nesta versão:

- Impressão de NF/Recibo em PDF
- Recuperação de senha por email
- Gestão de profissionais e funcionários via frontend (marcado como "Em breve")
- Dashboard com métricas e relatórios
- Configuração de feriados

---

## 👨‍💻 Desenvolvedor

**Manuel Fernandes Bittencourt Filho**  
manuelfbfilho@gmail.com · github.com/manuelfbfilho
