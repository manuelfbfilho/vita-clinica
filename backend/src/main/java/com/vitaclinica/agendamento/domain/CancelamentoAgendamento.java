package com.vitaclinica.agendamento.domain;

import com.vitaclinica.agendamento.domain.enums.TipoCancelador;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancelamento_agendamento")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelamentoAgendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "agendamento_id", nullable = false, unique = true)
    private Agendamento agendamento;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelado_por_tipo", nullable = false, length = 20)
    private TipoCancelador canceladoPorTipo;

    @Column(name = "cancelado_por_id", nullable = false)
    private Long canceladoPorId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
