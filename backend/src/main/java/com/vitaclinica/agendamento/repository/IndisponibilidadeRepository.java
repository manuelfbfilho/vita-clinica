package com.vitaclinica.agendamento.repository;

import com.vitaclinica.agendamento.domain.Indisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IndisponibilidadeRepository extends JpaRepository<Indisponibilidade, Long> {

    /**
     * Retorna todas as indisponibilidades que afetam um profissional em uma data:
     *  - Bloqueios específicos do profissional
     *  - Bloqueios gerais da clínica (profissional_id IS NULL)
     */
    @Query("""
        SELECT i FROM Indisponibilidade i
        WHERE i.data = :data
          AND (i.profissional.id = :profissionalId OR i.profissional IS NULL)
        """)
    List<Indisponibilidade> findByDataAndProfissional(LocalDate data, Long profissionalId);

    List<Indisponibilidade> findByDataBetweenOrderByDataAsc(LocalDate inicio, LocalDate fim);
}
