package com.vitaclinica.agendamento.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "clinica")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Clinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 18, unique = true)
    private String cnpj;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 20)
    private String whatsapp;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    // Endereço
    @Column(length = 10)
    private String cep;

    @Column(length = 200)
    private String logradouro;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 100)
    private String bairro;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String uf;

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
