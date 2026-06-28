package com.vitaclinica.agendamento.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "especialidade")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Especialidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(length = 300)
    private String descricao;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
}
