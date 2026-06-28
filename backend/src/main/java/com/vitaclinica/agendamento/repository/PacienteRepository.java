package com.vitaclinica.agendamento.repository;

import com.vitaclinica.agendamento.domain.Paciente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByCpf(String cpf);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);

    /** Busca por nome ou CPF (case-insensitive, paginado) */
    @Query("""
        SELECT p FROM Paciente p
        WHERE p.ativo = true
          AND (LOWER(p.nomeCompleto) LIKE LOWER(CONCAT('%', :termo, '%'))
            OR p.cpf LIKE CONCAT('%', :termo, '%'))
        ORDER BY p.nomeCompleto
        """)
    Page<Paciente> buscarPorNomeOuCpf(String termo, Pageable pageable);

    @Query("SELECT p FROM Paciente p WHERE p.ativo = true ORDER BY p.nomeCompleto")
    Page<Paciente> findAllAtivos(Pageable pageable);
}
