package com.vitaclinica.agendamento.service;

import com.vitaclinica.agendamento.domain.*;
import com.vitaclinica.agendamento.domain.enums.*;
import com.vitaclinica.agendamento.dto.request.*;
import com.vitaclinica.agendamento.exception.*;
import com.vitaclinica.agendamento.mapper.AgendamentoMapper;
import com.vitaclinica.agendamento.repository.*;
import com.vitaclinica.agendamento.security.UsuarioAutenticado;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AgendamentoService — Regras de negócio")
class AgendamentoServiceTest {

    @Mock AgendamentoRepository  agendamentoRepo;
    @Mock PacienteRepository     pacienteRepo;
    @Mock ProfissionalRepository profissionalRepo;
    @Mock FuncionarioRepository  funcionarioRepo;
    @Mock PlanoSaudeRepository   planoRepo;
    @Mock HorarioService         horarioService;
    @Mock NotificacaoEmailService emailService;
    @Mock AgendamentoMapper      mapper;

    @InjectMocks
    AgendamentoService service;

    private Paciente paciente;
    private Profissional profissional;
    private UsuarioAutenticado adminUser;

    @BeforeEach
    void setup() {
        var esp = new Especialidade(); esp.setId(1L); esp.setNome("Cardiologia");
        profissional = Profissional.builder().id(1L).nome("Dr. Carlos").crm("CRM/PE-12345")
                .especialidade(esp).ativo(true).build();
        paciente = Paciente.builder().id(1L).nomeCompleto("João Silva")
                .cpf("111.111.111-11").email("joao@email.com").ativo(true).build();
        adminUser = new UsuarioAutenticado("000.000.000-01", 1L, "ROLE_ADMIN");
    }

    @Test
    @DisplayName("Deve lançar exceção quando paciente não existe")
    void deveLancar_QuandoPacienteNaoExiste() {
        when(pacienteRepo.findById(99L)).thenReturn(Optional.empty());
        var req = buildRequest(99L, 1L);
        assertThatThrownBy(() -> service.criar(req, adminUser))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção quando profissional está inativo")
    void deveLancar_QuandoProfissionalInativo() {
        profissional.setAtivo(false);
        when(pacienteRepo.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepo.findById(1L)).thenReturn(Optional.of(profissional));
        var req = buildRequest(1L, 1L);
        assertThatThrownBy(() -> service.criar(req, adminUser))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("ativo");
    }

    @Test
    @DisplayName("Deve cancelar agendamento com motivo válido")
    void deveCancelarAgendamento_ComMotivo() {
        var agendamento = Agendamento.builder().id(1L).paciente(paciente)
                .profissional(profissional).status(StatusAgendamento.AGENDADO)
                .dataConsulta(LocalDate.now().plusDays(1))
                .horaConsulta(LocalTime.of(9, 0)).build();
        when(agendamentoRepo.findById(1L)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepo.save(any())).thenReturn(agendamento);
        when(mapper.toResponse(any())).thenReturn(null);

        var req = new CancelarAgendamentoRequest();
        req.setMotivo("Paciente viajou e não poderá comparecer");
        service.cancelar(1L, req, adminUser);

        assertThat(agendamento.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
        assertThat(agendamento.getCancelamento()).isNotNull();
        assertThat(agendamento.getCancelamento().getMotivo()).contains("viajou");
    }

    @Test
    @DisplayName("Não deve cancelar agendamento já cancelado")
    void naoDeveCancelar_AgendamentoCancelado() {
        var agendamento = Agendamento.builder().id(1L).paciente(paciente)
                .status(StatusAgendamento.CANCELADO).build();
        when(agendamentoRepo.findById(1L)).thenReturn(Optional.of(agendamento));
        var req = new CancelarAgendamentoRequest();
        req.setMotivo("Tentativa de re-cancelamento");
        assertThatThrownBy(() -> service.cancelar(1L, req, adminUser))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("status");
    }

    @Test
    @DisplayName("Paciente não deve cancelar agendamento de outro paciente")
    void pacienteNaoDeveCancelar_AgendamentoDeOutro() {
        var outroPaciente = Paciente.builder().id(2L).cpf("222.222.222-22").build();
        var agendamento = Agendamento.builder().id(1L).paciente(outroPaciente)
                .status(StatusAgendamento.AGENDADO).build();
        when(agendamentoRepo.findById(1L)).thenReturn(Optional.of(agendamento));

        var pacienteUser = new UsuarioAutenticado("111.111.111-11", 1L, "ROLE_PACIENTE");
        var req = new CancelarAgendamentoRequest();
        req.setMotivo("Tentativa de cancelar consulta alheia");
        assertThatThrownBy(() -> service.cancelar(1L, req, pacienteUser))
                .isInstanceOf(AcessoNegadoException.class);
    }

    private CriarAgendamentoRequest buildRequest(Long pacienteId, Long profissionalId) {
        var req = new CriarAgendamentoRequest();
        req.setPacienteId(pacienteId);
        req.setProfissionalId(profissionalId);
        req.setDataConsulta(LocalDate.now().plusDays(3));
        req.setHoraConsulta(LocalTime.of(9, 0));
        req.setTipoAtendimento(TipoAtendimento.PRESENCIAL);
        req.setTipoConsulta(TipoConsulta.CONSULTA);
        req.setFormaPagamento(FormaPagamento.PARTICULAR);
        return req;
    }
}
