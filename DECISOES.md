# 📐 Decisões Técnicas — Vita Clínica

> Este documento responde às 3 perguntas obrigatórias do teste técnico MV Sistemas.

---

## 1. Quais foram as principais decisões técnicas?

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

## 2. O que você priorizou e o que ficou de fora?

### Prioridades: 

De todo o projeto, com os acréscimos que idealizei para desenvolvimento desse sitema agenamento de consulta, o que foi colocado como prioridades as funcionalidades indicadas no documento enviado, onde indica **"O que deve ser desenvolvido"**.

Desta forma foi criado o login geral, onde após realização do login (do paciente ou funcionário ou Admin) já entra no dashboard/página principal, onde apresentará, para cada um:

* Pacientes
  - Acesso ao seu perfil, podendo atualizar seus dados
  - Criar e cancelar um novo agendamento, seguindo as regras impostas
  - Visualizar todos os agendamentos
  
* Funcionários
  - Visualizar todos os agendamentos futuros e programados para todos os médicos (profissionais)
  - Criar e cancelar um agendamento para um paciente já cadastrado
  
* Admin
  - Além de todas as funcionlidades dos anteriores, apenas o Admin pode cadastrar funcionário.


No README apresento com mais informação.

---

## 3. Quais foram os principais desafios técnicos e como você os resolveu? Se utilizou IA, em quais partes e como validou o resultado.

### Principal: Desenvolvimento em JAVA

**Problema:** Por ser a primeira vez que tento realmente desenvolver algo completo, utilizando JAVA.

**Solução:** Muito tempo pesquisando tutorias e consultas com IA (Claude e ChatGPT).

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


### Desafio 5: Utilização de IA

Sim, foi utilizado muita IA. Mesmo com pesquisas, o tempo ficou curto para entender o JAVA e seu funcionamento e até coniguração de minha máquina para conseguir trabalhar. Então utilizei desde ajuda para configuração, desenvolvimento e execução.

Após as criações eu tentava analisar e fazer as modificações, para tentar chegar ao que eu queria, ou o teste pedia. Sem utilizr a IA, com certeza naõ conseguiria concluir essa parte que foi feita.

---

*Manuel Fernandes Baptista Filho — manuelfbfilho@gmail.com*
