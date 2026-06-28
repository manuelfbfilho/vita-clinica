package com.vitaclinica.agendamento.service;

import com.vitaclinica.agendamento.config.AppProperties;
import com.vitaclinica.agendamento.dto.response.SlotHorarioResponse;
import com.vitaclinica.agendamento.exception.NegocioException;
import com.vitaclinica.agendamento.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.time.*;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("HorarioService — Regras de disponibilidade")
class HorarioServiceTest {

    @Mock AgendamentoRepository      agendamentoRepo;
    @Mock IndisponibilidadeRepository indispRepo;

    @InjectMocks
    HorarioService service;

    @BeforeEach
    void setup() {
        AppProperties props = new AppProperties();
        service = new HorarioService(agendamentoRepo, indispRepo, props);
        when(agendamentoRepo.findHorasOcupadasByProfissionalAndData(anyLong(), any())).thenReturn(List.of());
        when(indispRepo.findByDataAndProfissional(any(), anyLong())).thenReturn(List.of());
    }

    @Test
    @DisplayName("Deve retornar slots corretos para segunda-feira")
    void deveRetornarSlotsPara_SegundaFeira() {
        LocalDate proxima = proximaSegunda();
        List<SlotHorarioResponse> slots = service.calcularDisponibilidade(1L, proxima);
        assertThat(slots).isNotEmpty();
        assertThat(slots.get(0).getHora()).isEqualTo(LocalTime.of(7, 0));
        assertThat(slots).allMatch(s -> !s.getHora().isBefore(LocalTime.of(7, 0)));
        assertThat(slots).allMatch(s -> s.getHora().plusMinutes(30).compareTo(LocalTime.of(20, 0)) <= 0);
    }

    @Test
    @DisplayName("Deve retornar slots corretos para sábado (08:00-13:00)")
    void deveRetornarSlotsPara_Sabado() {
        LocalDate sabado = proximaSabado();
        List<SlotHorarioResponse> slots = service.calcularDisponibilidade(1L, sabado);
        assertThat(slots.get(0).getHora()).isEqualTo(LocalTime.of(8, 0));
        assertThat(slots).allMatch(s -> !s.getHora().isBefore(LocalTime.of(8, 0)));
        assertThat(slots).allMatch(s -> s.getHora().plusMinutes(30).compareTo(LocalTime.of(13, 0)) <= 0);
        assertThat(slots.size()).isLessThanOrEqualTo(7);
    }

    @Test
    @DisplayName("Deve lançar exceção para domingo")
    void deveLancarExcecao_ParaDomingo() {
        LocalDate domingo = proximoDomingo();
        assertThatThrownBy(() -> service.calcularDisponibilidade(1L, domingo))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("domingo");
    }

    @Test
    @DisplayName("Deve lançar exceção para data no passado")
    void deveLancarExcecao_DataNoPassado() {
        assertThatThrownBy(() -> service.calcularDisponibilidade(1L, LocalDate.now().minusDays(1)))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("passadas");
    }

    @Test
    @DisplayName("Slot ocupado deve aparecer como indisponível")
    void deveMarcarSlotOcupado_ComoIndisponivel() {
        LocalDate proxima = proximaSegunda();
        LocalTime hora = LocalTime.of(9, 0);
        when(agendamentoRepo.findHorasOcupadasByProfissionalAndData(1L, proxima))
                .thenReturn(List.of(hora));
        List<SlotHorarioResponse> slots = service.calcularDisponibilidade(1L, proxima);
        SlotHorarioResponse slotNove = slots.stream()
                .filter(s -> s.getHora().equals(hora)).findFirst().orElseThrow();
        assertThat(slotNove.isDisponivel()).isFalse();
    }

    @Test
    @DisplayName("Todos os slots disponíveis quando profissional está livre")
    void todosSlots_Disponiveis_QuandoLivre() {
        LocalDate proxima = proximaSegunda();
        List<SlotHorarioResponse> slots = service.calcularDisponibilidade(1L, proxima);
        long indisponiveis = slots.stream().filter(s -> !s.isDisponivel()).count();
        assertThat(indisponiveis).isZero();
    }

    // ─── helpers ────────────────────────────────────────────────
    private LocalDate proximaSegunda() {
        LocalDate d = LocalDate.now().plusDays(1);
        while (d.getDayOfWeek() != DayOfWeek.MONDAY) d = d.plusDays(1);
        return d;
    }
    private LocalDate proximaSabado() {
        LocalDate d = LocalDate.now().plusDays(1);
        while (d.getDayOfWeek() != DayOfWeek.SATURDAY) d = d.plusDays(1);
        return d;
    }
    private LocalDate proximoDomingo() {
        LocalDate d = LocalDate.now().plusDays(1);
        while (d.getDayOfWeek() != DayOfWeek.SUNDAY) d = d.plusDays(1);
        return d;
    }
}
