package com.vitaclinica.agendamento.service;

import com.vitaclinica.agendamento.config.AppProperties;
import com.vitaclinica.agendamento.dto.response.SlotHorarioResponse;
import com.vitaclinica.agendamento.exception.NegocioException;
import com.vitaclinica.agendamento.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Responsável por calcular disponibilidade de horários.
 *
 * REGRAS:
 *   - Seg-Sex: 07:00–20:00 | Sáb: 08:00–13:00 | Dom: fechado
 *   - Bloco: 30min consulta + 10min intervalo = 40min por slot
 *   - Slot inválido se: ocupado, bloqueado, ou no passado
 */
@Service
@RequiredArgsConstructor
public class HorarioService {

    private final AgendamentoRepository      agendamentoRepo;
    private final IndisponibilidadeRepository indispRepo;
    private final AppProperties              props;

    public List<SlotHorarioResponse> calcularDisponibilidade(Long profissionalId, LocalDate data) {
        validarData(data);

        DayOfWeek diaSemana = data.getDayOfWeek();
        if (diaSemana == DayOfWeek.SUNDAY)
            throw new NegocioException("DIA_FECHADO", "A clínica não realiza atendimentos aos domingos.", HttpStatus.UNPROCESSABLE_ENTITY);

        boolean isSabado  = diaSemana == DayOfWeek.SATURDAY;
        LocalTime abertura = isSabado ? props.getHorario().getSabado().getInicio()
                                      : props.getHorario().getSemana().getInicio();
        LocalTime fechamento = isSabado ? props.getHorario().getSabado().getFim()
                                        : props.getHorario().getSemana().getFim();
        int bloco = props.getConsulta().blocoMinutos();
        int duracao = props.getConsulta().getDuracaoMinutos();

        // Horários já ocupados por agendamentos ativos
        Set<LocalTime> ocupados = agendamentoRepo
                .findHorasOcupadasByProfissionalAndData(profissionalId, data)
                .stream().collect(Collectors.toSet());

        // Indisponibilidades do profissional e da clínica
        var indisponibilidades = indispRepo.findByDataAndProfissional(data, profissionalId);

        // Agora
        LocalDateTime agora = LocalDateTime.now();

        List<SlotHorarioResponse> slots = new ArrayList<>();
        LocalTime slot = abertura;

        while (!slot.plusMinutes(duracao).isAfter(fechamento)) {
            boolean disponivel = true;

            // Regra 1: slot no passado
            if (data.equals(agora.toLocalDate()) && slot.isBefore(agora.toLocalTime()))
                disponivel = false;

            // Regra 2: profissional já tem agendamento
            if (disponivel && ocupados.contains(slot))
                disponivel = false;

            // Regra 3: slot coberto por indisponibilidade
            if (disponivel) {
                LocalTime slotFinal = slot;
                boolean bloqueado = indisponibilidades.stream()
                        .anyMatch(i -> i.cobreHorario(slotFinal));
                if (bloqueado) disponivel = false;
            }

            slots.add(disponivel
                    ? SlotHorarioResponse.disponivel(slot)
                    : SlotHorarioResponse.ocupado(slot));

            slot = slot.plusMinutes(bloco);
        }

        return slots;
    }

    public void validarSlot(Long profissionalId, Long pacienteId, LocalDate data, LocalTime hora) {
        validarData(data);

        DayOfWeek dia = data.getDayOfWeek();
        if (dia == DayOfWeek.SUNDAY)
            throw new NegocioException("DIA_FECHADO", "A clínica não atende aos domingos.", HttpStatus.UNPROCESSABLE_ENTITY);

        boolean isSabado = dia == DayOfWeek.SATURDAY;
        LocalTime abertura   = isSabado ? props.getHorario().getSabado().getInicio() : props.getHorario().getSemana().getInicio();
        LocalTime fechamento = isSabado ? props.getHorario().getSabado().getFim()    : props.getHorario().getSemana().getFim();

        // Fora do horário
        if (hora.isBefore(abertura) || hora.plusMinutes(props.getConsulta().getDuracaoMinutos()).isAfter(fechamento))
            throw new NegocioException("HORARIO_FORA_EXPEDIENTE",
                    "Horário fora do expediente da clínica.", HttpStatus.UNPROCESSABLE_ENTITY);

        // Slot válido (múltiplo do bloco a partir da abertura)
        long minutos = Duration.between(abertura, hora).toMinutes();
        if (minutos % props.getConsulta().blocoMinutos() != 0)
            throw new NegocioException("HORARIO_INVALIDO",
                    "Horário inválido. Use os slots disponíveis no sistema.", HttpStatus.UNPROCESSABLE_ENTITY);

        // Conflito de profissional
        if (agendamentoRepo.existsByProfissionalIdAndDataConsultaAndHoraConsultaAndStatusNot(
                profissionalId, data, hora, com.vitaclinica.agendamento.domain.enums.StatusAgendamento.CANCELADO))
            throw new com.vitaclinica.agendamento.exception.HorarioIndisponivelException(
                    data, hora, "Profissional já possui consulta agendada neste horário.");

        // Conflito de paciente
        if (agendamentoRepo.existsByPacienteIdAndDataConsultaAndHoraConsultaAndStatusNot(
                pacienteId, data, hora, com.vitaclinica.agendamento.domain.enums.StatusAgendamento.CANCELADO))
            throw new com.vitaclinica.agendamento.exception.HorarioIndisponivelException(
                    data, hora, "Paciente já possui consulta agendada neste horário.");

        // Indisponibilidade
        boolean bloqueado = indispRepo.findByDataAndProfissional(data, profissionalId)
                .stream().anyMatch(i -> i.cobreHorario(hora));
        if (bloqueado)
            throw new com.vitaclinica.agendamento.exception.HorarioIndisponivelException(
                    data, hora, "Horário bloqueado pela clínica.");
    }

    private void validarData(LocalDate data) {
        if (data.isBefore(LocalDate.now()))
            throw new NegocioException("DATA_PASSADO",
                    "Não é possível agendar consultas em datas passadas.", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
