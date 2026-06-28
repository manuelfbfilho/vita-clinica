package com.vitaclinica.agendamento.domain;

import com.vitaclinica.agendamento.domain.enums.RoleUsuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "funcionario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RoleUsuario role = RoleUsuario.FUNCIONARIO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
