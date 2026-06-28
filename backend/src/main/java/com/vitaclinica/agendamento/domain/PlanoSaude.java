package com.vitaclinica.agendamento.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plano_saude")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanoSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String nome;

    @Column(length = 50)
    private String codigo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
}
