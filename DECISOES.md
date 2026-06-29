# 📐 Decisões Técnicas — Vita Clínica

> Este documento responde às 3 perguntas obrigatórias do teste técnico MV Sistemas.

---

## 1. Qual sua abordagem arquitetural para o controle de horários e prevenção de conflitos?

### Abordagem: Geração Dinâmica de Slots + Validação em Camadas

Em vez de persistir slots de disponibilidade no banco (o que exigiria geração prévia de milhares de registros e sincronização complexa), optei por uma **abordagem de cálculo sob demanda** no `HorarioService`.

#### Como funciona

```
GET /agendamentos/horarios-disponiveis?profissionalId=1&data=2025-07-15
```

O `HorarioService.calcularDisponibilidade()` executa:

1. **Valida a data** → rejeita passado e domingo
2. **Determina o expediente** → Seg-Sex (07:00-20:00) ou Sáb (08:00-13:00)
3. **Gera todos os slots** possíveis a cada 40min (30min consulta + 10min intervalo)
4. **Consulta o banco uma vez** → `findHorasOcupadasByProfissionalAndData()` — retorna apenas `LocalTime[]`, não entidades completas
5. **Verifica indisponibilidades** → bloqueios pontuais cadastrados pela clínica
6. **Marca slots passados** (para agendamentos no dia atual)
7. **Retorna a lista** com `disponivel: true/false` para cada slot

#### Prevenção de conflitos (dupla garantia)

A prevenção de conflitos opera em **duas camadas independentes**:

**Camada 1 — Validação de negócio (HorarioService.validarSlot):**
```java
// Verifica conflito de profissional
agendamentoRepo.existsByProfissionalIdAndDataConsultaAndHoraConsultaAndStatusNot(
    profissionalId, data, hora, StatusAgendamento.CANCELADO)

// Verifica conflito de paciente (mesmo paciente não pode ter duas consultas simultâneas)
agendamentoRepo.existsByPacienteIdAndDataConsultaAndHoraConsultaAndStatusNot(
    pacienteId, data, hora, StatusAgendamento.CANCELADO)
```

**Camada 2 — Restrição no banco (Flyway V7):**
```sql
-- Previne duplicatas mesmo em acessos concorrentes
CREATE UNIQUE INDEX uq_agendamento_profissional_horario_ativo
    ON agendamento (profissional_id, data_consulta, hora_consulta)
    WHERE status NOT IN ('CANCELADO');
```

A **partial unique index** (não um UNIQUE constraint convencional) é a decisão técnica mais relevante aqui: ela garante que agendamentos cancelados não bloqueam o slot para reagendamento, mas dois agendamentos ativos nunca colidem, mesmo sob carga concorrente.

#### Vantagens desta abordagem
- Sem necessidade de job de geração de slots
- Sem risco de dessincronização de estado
- Slots calculados sempre refletem o estado atual do banco
- Uma única query para buscar horários ocupados (eficiência)

---

## 2. Como você implementou a autenticação e autorização?

### Autenticação: JWT Stateless com Login Unificado

A clínica tem três tipos de usuário (Paciente, Funcionário, Admin) em **tabelas separadas** (`paciente` e `funcionario`), mas o sistema oferece **um único endpoint de login** (`POST /auth/login`).

#### Fluxo do AuthService.login()

```
CPF + Senha
    ↓
1. Busca em funcionario WHERE cpf = ? AND ativo = true
2. Se encontrar → valida senha BCrypt
3. Se não encontrar → busca em paciente WHERE cpf = ? AND ativo = true
4. Se não encontrar em nenhum → BadCredentialsException
    ↓
Gera JWT com: sub=CPF, role=ROLE_*, userId, nome
```

**Por que funcionário primeiro?** Para que um CPF cadastrado como funcionário/admin não possa fazer login com a role de paciente caso exista nas duas tabelas.

#### Conteúdo do Token JWT
```json
{
  "sub": "000.000.000-01",
  "role": "ROLE_ADMIN",
  "userId": 1,
  "nome": "Administrador",
  "iat": 1700000000,
  "exp": 1700086400
}
```

#### JwtAuthFilter
Intercepta todas as requisições, extrai o token do header `Authorization: Bearer <token>` e injeta um `UsuarioAutenticado` no `SecurityContext` — sem consultar o banco a cada request (stateless puro).

```java
// Principal injetado nos controllers via @AuthenticationPrincipal
public record UsuarioAutenticado(String cpf, Long userId, String role) {
    public boolean isAdmin()       { return "ROLE_ADMIN".equals(role); }
    public boolean isFuncionario() { return "ROLE_FUNCIONARIO".equals(role) || isAdmin(); }
    public boolean isPaciente()    { return "ROLE_PACIENTE".equals(role); }
}
```

#### Autorização

Configurada em duas camadas:

**SecurityConfig (nível de rota):**
```java
.requestMatchers(HttpMethod.POST, "/funcionarios").hasRole("ADMIN")
.requestMatchers(HttpMethod.GET,  "/agendamentos").hasAnyRole("FUNCIONARIO", "ADMIN")
.requestMatchers(HttpMethod.POST, "/pacientes").permitAll()
```

**Nos Services (nível de recurso):**
```java
// Paciente só pode ver/editar o próprio agendamento
if (usuario.isPaciente() && !agendamento.getPaciente().getCpf().equals(usuario.getCpf()))
    throw new AcessoNegadoException("...");
```

Esta separação permite que regras de negócio finas (ex: paciente só acessa próprio recurso) sejam aplicadas mesmo quando o Spring Security já aprovou a requisição pela role.

#### Senhas
- Algoritmo BCrypt, **força 12** (recomendação OWASP para sistemas de saúde)
- Nunca armazenadas em plaintext; o DataSeeder hasheia em runtime
- Validação: ≥10 chars, 1 maiúscula, 1 minúscula, 1 número, 1 especial (`@ValidSenha`)

---

## 3. Quais foram os principais desafios técnicos e como você os resolveu?

### Principal: 

### Desafio 1: Regras de conflito com cancelamento/reagendamento

**Problema:** Um `UNIQUE CONSTRAINT` convencional em `(profissional_id, data_consulta, hora_consulta)` impediria o reagendamento após cancelamento — o registro cancelado ainda existiria na tabela (mantemos para auditoria).

**Solução:** **Partial Unique Index** no PostgreSQL:
```sql
CREATE UNIQUE INDEX uq_agendamento_profissional_horario_ativo
    ON agendamento (profissional_id, data_consulta, hora_consulta)
    WHERE status NOT IN ('CANCELADO');
```
Isso resolve o problema de forma elegante: o banco de dados garante unicidade apenas para registros ativos, permitindo múltiplos cancelamentos para o mesmo horário e posterior reagendamento — sem nenhuma lógica extra na aplicação.

### Desafio 2: JJWT 0.12.x tem API incompatível com versões anteriores

**Problema:** A maioria dos tutoriais e exemplos usa JJWT 0.11.x (`Jwts.parserBuilder()`, `.setSigningKey()`, `.parseClaimsJws()`). O JJWT 0.12.x mudou completamente a API.

**Solução:** Uso exclusivo da nova API:
```java
// JJWT 0.12.x — API correta
Jwts.parser()
    .verifyWith(getKey())          // não mais setSigningKey()
    .build()
    .parseSignedClaims(token)      // não mais parseClaimsJws()
    .getPayload();
```

### Desafio 3: Circular Dependency com BCryptPasswordEncoder

**Problema:** `DataSeeder` precisa de `PasswordEncoder`. `SecurityConfig` define o bean de `PasswordEncoder`. `SecurityConfig` faz `@Import` implícito de outros beans, criando um ciclo potencial.

**Solução:** O bean `PasswordEncoder` foi declarado diretamente no `SecurityConfig` (não em `AppConfig`), tornando a dependência unidirecional: `DataSeeder → PasswordEncoder ← SecurityConfig`.

### Desafio 4: Autenticação dual (Paciente + Funcionário em tabelas separadas)

**Problema:** Spring Security espera uma única `UserDetailsService` que retorna `UserDetails`. Mas o sistema tem dois domínios de usuário em tabelas distintas.

**Solução:** Em vez de tentar encaixar no modelo padrão do Spring Security (que exigiria uma tabela unificada ou view), optei por **não usar UserDetailsService**. O `AuthService` faz a verificação manualmente com `PasswordEncoder.matches()`, gera o JWT e retorna o token. O `JwtAuthFilter` popula o contexto a partir do JWT sem consultar o banco. Isso elimina a complexidade, mantém a solução stateless e evita acoplamento desnecessário com a abstração do Spring.

---

*Manuel Fernandes Baptista Filho — manuelfbfilho@gmail.com*
