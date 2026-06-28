package com.vitaclinica.agendamento.domain;

import com.vitaclinica.agendamento.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "agendamento")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profissional_id", nullable = false)
    private Profissional profissional;

    /** NULL quando o próprio paciente agendou online */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    /** Data separada da hora conforme requisito do sistema */
    @Column(name = "data_consulta", nullable = false)
    private LocalDate dataConsulta;

    /** Hora separada da data conforme requisito do sistema */
    @Column(name = "hora_consulta", nullable = false)
    private LocalTime horaConsulta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_atendimento", nullable = false, length = 20)
    @Builder.Default
    private TipoAtendimento tipoAtendimento = TipoAtendimento.PRESENCIAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_consulta", nullable = false, length = 20)
    @Builder.Default
    private TipoConsulta tipoConsulta = TipoConsulta.CONSULTA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    @Builder.Default
    private FormaPagamento formaPagamento = FormaPagamento.PARTICULAR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plano_saude_id")
    private PlanoSaude planoSaude;

    @Column(name = "necessita_nf", nullable = false)
    @Builder.Default
    private Boolean necessitaNf = false;

    @Column(name = "nf_enviar_email", nullable = false)
    @Builder.Default
    private Boolean nfEnviarEmail = false;

    @Column(length = 500)
    private String observacao;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─────────────────────────────────────────────
    // Relacionamento com cancelamento (lazy, opcional)
    // ─────────────────────────────────────────────
    @OneToOne(mappedBy = "agendamento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CancelamentoAgendamento cancelamento;

    /** Cancela o agendamento e registra o motivo */
    public void cancelar(String motivo, TipoCancelador canceladoPorTipo, Long canceladoPorId) {
        this.status = StatusAgendamento.CANCELADO;
        this.cancelamento = CancelamentoAgendamento.builder()
                .agendamento(this)
                .motivo(motivo)
                .canceladoPorTipo(canceladoPorTipo)
                .canceladoPorId(canceladoPorId)
                .build();
    }
}
