package com.vitaclinica.agendamento.service;

import com.vitaclinica.agendamento.dto.request.CadastrarPacienteRequest;
import com.vitaclinica.agendamento.exception.NegocioException;
import com.vitaclinica.agendamento.mapper.PacienteMapper;
import com.vitaclinica.agendamento.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PacienteService — Validações de cadastro")
class PacienteServiceTest {

    @Mock PacienteRepository   pacienteRepo;
    @Mock PlanoSaudeRepository planoRepo;
    @Mock PacienteMapper       mapper;
    @InjectMocks PacienteService service;

    @BeforeEach
    void setup() {
        service = new PacienteService(pacienteRepo, planoRepo, mapper, new BCryptPasswordEncoder(4));
    }

    @Test
    @DisplayName("Deve lançar exceção para CPF duplicado")
    void deveLancar_CpfDuplicado() {
        when(pacienteRepo.existsByCpf(any())).thenReturn(true);
        assertThatThrownBy(() -> service.cadastrar(buildRequest()))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("CPF");
    }

    @Test
    @DisplayName("Deve lançar exceção para email duplicado")
    void deveLancar_EmailDuplicado() {
        when(pacienteRepo.existsByCpf(any())).thenReturn(false);
        when(pacienteRepo.existsByEmail(any())).thenReturn(true);
        assertThatThrownBy(() -> service.cadastrar(buildRequest()))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("email");
    }

    private CadastrarPacienteRequest buildRequest() {
        var req = new CadastrarPacienteRequest();
        req.setCpf("111.111.111-11");
        req.setEmail("test@email.com");
        req.setSenha("Vita@2025#");
        req.setNomeCompleto("Teste Unitário");
        req.setTelefone("(81) 99999-9999");
        return req;
    }
}
