package com.vitaclinica.agendamento.repository;

import com.vitaclinica.agendamento.domain.Agendamento;
import com.vitaclinica.agendamento.domain.enums.StatusAgendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    /** Verifica conflito de profissional (exclui cancelados) */
    boolean existsByProfissionalIdAndDataConsultaAndHoraConsultaAndStatusNot(
            Long profissionalId, LocalDate data, LocalTime hora, StatusAgendamento status);

    /** Verifica conflito de paciente (exclui cancelados) */
    boolean existsByPacienteIdAndDataConsultaAndHoraConsultaAndStatusNot(
            Long pacienteId, LocalDate data, LocalTime hora, StatusAgendamento status);

    /** Horários já agendados para um profissional em uma data */
    @Query("""
        SELECT a.horaConsulta FROM Agendamento a
        WHERE a.profissional.id = :profissionalId
          AND a.dataConsulta = :data
          AND a.status NOT IN ('CANCELADO')
        """)
    List<LocalTime> findHorasOcupadasByProfissionalAndData(Long profissionalId, LocalDate data);

    /** Listagem do paciente (meus agendamentos) */
    Page<Agendamento> findByPacienteIdOrderByDataConsultaDescHoraConsultaDesc(
            Long pacienteId, Pageable pageable);

    /** Próximo agendamento do paciente (para dashboard) */
    @Query("""
        SELECT a FROM Agendamento a
        WHERE a.paciente.id = :pacienteId
          AND a.status IN ('AGENDADO','CONFIRMADO')
          AND (a.dataConsulta > :hoje OR (a.dataConsulta = :hoje AND a.horaConsulta >= :horaAtual))
        ORDER BY a.dataConsulta ASC, a.horaConsulta ASC
        """)
    List<Agendamento> findProximosAgendamentosPaciente(Long pacienteId, LocalDate hoje, LocalTime horaAtual, Pageable pageable);

    /** Agenda do dia para dashboard de funcionário */
    @Query("""
        SELECT a FROM Agendamento a
        JOIN FETCH a.paciente
        JOIN FETCH a.profissional p
        JOIN FETCH p.especialidade
        WHERE a.dataConsulta = :data
          AND a.status NOT IN ('CANCELADO')
        ORDER BY a.horaConsulta ASC
        """)
    List<Agendamento> findAgendaDia(LocalDate data);

    /** Próximos agendamentos futuros — dashboard de funcionário/admin */
    @Query("""
        SELECT a FROM Agendamento a
        JOIN FETCH a.paciente
        JOIN FETCH a.profissional p
        JOIN FETCH p.especialidade
        WHERE a.status IN ('AGENDADO','CONFIRMADO')
          AND (a.dataConsulta > :hoje OR (a.dataConsulta = :hoje AND a.horaConsulta >= :horaAtual))
        ORDER BY a.dataConsulta ASC, a.horaConsulta ASC
        """)
    List<Agendamento> findProximosAgendamentos(LocalDate hoje, LocalTime horaAtual, Pageable pageable);
}
