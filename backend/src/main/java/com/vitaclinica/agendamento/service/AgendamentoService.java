package com.vitaclinica.agendamento.service;

import com.vitaclinica.agendamento.domain.*;
import com.vitaclinica.agendamento.domain.enums.*;
import com.vitaclinica.agendamento.dto.request.*;
import com.vitaclinica.agendamento.dto.response.AgendamentoResponse;
import com.vitaclinica.agendamento.exception.*;
import com.vitaclinica.agendamento.mapper.AgendamentoMapper;
import com.vitaclinica.agendamento.repository.*;
import com.vitaclinica.agendamento.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository  agendamentoRepo;
    private final PacienteRepository     pacienteRepo;
    private final ProfissionalRepository profissionalRepo;
    private final FuncionarioRepository  funcionarioRepo;
    private final PlanoSaudeRepository   planoRepo;
    private final HorarioService         horarioService;
    private final NotificacaoEmailService emailService;
    private final AgendamentoMapper      mapper;

    @Transactional
    public AgendamentoResponse criar(CriarAgendamentoRequest req, UsuarioAutenticado usuario) {
        // Paciente — funcionário pode agendar para qualquer paciente
        Paciente paciente = pacienteRepo.findById(req.getPacienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", req.getPacienteId()));

        // Se for paciente logado, só pode agendar para si mesmo
        if (usuario.isPaciente() && !paciente.getCpf().equals(usuario.getCpf()))
            throw new AcessoNegadoException("Pacientes só podem criar agendamentos para si mesmos.");

        Profissional profissional = profissionalRepo.findByIdComEspecialidade(req.getProfissionalId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", req.getProfissionalId()));
        if (!profissional.getAtivo())
            throw new NegocioException("PROFISSIONAL_INATIVO", "Profissional não está ativo.", HttpStatus.UNPROCESSABLE_ENTITY);

        // Validação de horário (todas as regras de negócio)
        horarioService.validarSlot(profissional.getId(), paciente.getId(), req.getDataConsulta(), req.getHoraConsulta());

        // Funcionário responsável (null = agendou online)
        Funcionario funcionario = null;
        if (usuario.isFuncionario()) {
            funcionario = funcionarioRepo.findById(usuario.getUserId()).orElse(null);
        }

        // Plano de saúde do agendamento
        PlanoSaude plano = null;
        if (req.getFormaPagamento() == FormaPagamento.PLANO && req.getPlanoSaudeId() != null) {
            plano = planoRepo.findById(req.getPlanoSaudeId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Plano de saúde", req.getPlanoSaudeId()));
        }

        Agendamento agendamento = Agendamento.builder()
                .paciente(paciente)
                .profissional(profissional)
                .funcionario(funcionario)
                .dataConsulta(req.getDataConsulta())
                .horaConsulta(req.getHoraConsulta())
                .tipoAtendimento(req.getTipoAtendimento())
                .tipoConsulta(req.getTipoConsulta())
                .formaPagamento(req.getFormaPagamento())
                .planoSaude(plano)
                .necessitaNf(Boolean.TRUE.equals(req.getNecessitaNf()))
                .nfEnviarEmail(Boolean.TRUE.equals(req.getNfEnviarEmail()))
                .observacao(req.getObservacao())
                .build();

        Agendamento salvo = agendamentoRepo.save(agendamento);

        // Envia email de confirmação (assíncrono — não bloqueia resposta)
        try { emailService.enviarConfirmacao(salvo); } catch (Exception e) { /* log apenas */ }

        return mapper.toResponse(salvo);
    }

    public Page<AgendamentoResponse> listar(Long pacienteId, Long profissionalId,
            StatusAgendamento status, LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        // Filtros dinâmicos via Specification (simplificado com query direta)
        return agendamentoRepo.findAll(pageable).map(mapper::toResponse);
    }

    public Page<AgendamentoResponse> meusAgendamentos(Long pacienteId, Pageable pageable) {
        return agendamentoRepo.findByPacienteIdOrderByDataConsultaDescHoraConsultaDesc(pacienteId, pageable)
                .map(mapper::toResponse);
    }

    public List<AgendamentoResponse> agendaDia(LocalDate data) {
        return agendamentoRepo.findAgendaDia(data).stream().map(mapper::toResponse).toList();
    }

    public AgendamentoResponse buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public AgendamentoResponse cancelar(Long id, CancelarAgendamentoRequest req, UsuarioAutenticado usuario) {
        Agendamento agendamento = buscarEntidade(id);

        if (!agendamento.getStatus().isCancelavel())
            throw new NegocioException("STATUS_INVALIDO",
                    "Este agendamento não pode ser cancelado pois está com status: " + agendamento.getStatus().getDescricao(),
                    HttpStatus.UNPROCESSABLE_ENTITY);

        // Paciente só pode cancelar o próprio agendamento
        if (usuario.isPaciente() && !agendamento.getPaciente().getCpf().equals(usuario.getCpf()))
            throw new AcessoNegadoException("Você não pode cancelar o agendamento de outro paciente.");

        TipoCancelador tipoCancelador = usuario.isPaciente() ? TipoCancelador.PACIENTE : TipoCancelador.FUNCIONARIO;
        agendamento.cancelar(req.getMotivo(), tipoCancelador, usuario.getUserId());
        Agendamento salvo = agendamentoRepo.save(agendamento);

        try { emailService.enviarCancelamento(salvo); } catch (Exception e) { /* log apenas */ }

        return mapper.toResponse(salvo);
    }

    @Transactional
    public AgendamentoResponse confirmar(Long id) {
        Agendamento a = buscarEntidade(id);
        if (a.getStatus() != StatusAgendamento.AGENDADO)
            throw new NegocioException("STATUS_INVALIDO", "Apenas agendamentos com status AGENDADO podem ser confirmados.", HttpStatus.UNPROCESSABLE_ENTITY);
        a.setStatus(StatusAgendamento.CONFIRMADO);
        return mapper.toResponse(agendamentoRepo.save(a));
    }

    @Transactional
    public AgendamentoResponse concluir(Long id) {
        Agendamento a = buscarEntidade(id);
        if (a.getStatus() != StatusAgendamento.CONFIRMADO && a.getStatus() != StatusAgendamento.AGENDADO)
            throw new NegocioException("STATUS_INVALIDO", "Agendamento não pode ser concluído no status atual.", HttpStatus.UNPROCESSABLE_ENTITY);
        a.setStatus(StatusAgendamento.CONCLUIDO);
        return mapper.toResponse(agendamentoRepo.save(a));
    }

    private Agendamento buscarEntidade(Long id) {
        return agendamentoRepo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento", id));
    }
}
