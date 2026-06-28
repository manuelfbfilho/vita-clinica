package com.vitaclinica.agendamento.repository;

import com.vitaclinica.agendamento.domain.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    boolean existsByCrm(String crm);
    Optional<Profissional> findByCrm(String crm);

    /** Carrega profissional com especialidade eager — evita LazyInitializationException no mapper */
    @Query("SELECT p FROM Profissional p JOIN FETCH p.especialidade WHERE p.id = :id")
    Optional<Profissional> findByIdComEspecialidade(Long id);

    @Query("SELECT p FROM Profissional p JOIN FETCH p.especialidade e WHERE p.ativo = true ORDER BY p.nome")
    List<Profissional> findAllAtivosComEspecialidade();

    @Query("SELECT p FROM Profissional p JOIN FETCH p.especialidade e WHERE e.id = :especialidadeId AND p.ativo = true ORDER BY p.nome")
    List<Profissional> findByEspecialidadeIdAndAtivoTrue(Long especialidadeId);
}
