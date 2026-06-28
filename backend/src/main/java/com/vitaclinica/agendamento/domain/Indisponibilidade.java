package com.vitaclinica.agendamento.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "indisponibilidade")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Indisponibilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** NULL = bloqueia todos os profissionais */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id")
    private Profissional profissional;

    @Column(nullable = false)
    private LocalDate data;

    /** NULL junto com horaFim = dia inteiro bloqueado */
    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fim")
    private LocalTime horaFim;

    @Column(nullable = false, length = 300)
    private String motivo;

    @Column(name = "registrado_por_tipo", nullable = false, length = 20)
    private String registradoPorTipo;  // FUNCIONARIO | ADMIN

    @Column(name = "registrado_por_id", nullable = false)
    private Long registradoPorId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Verifica se esta indisponibilidade cobre o horário informado */
    public boolean cobreHorario(LocalTime horario) {
        if (horaInicio == null) return true;  // dia inteiro
        return !horario.isBefore(horaInicio) && horario.isBefore(horaFim);
    }
}
