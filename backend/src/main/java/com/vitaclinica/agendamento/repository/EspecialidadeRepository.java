package com.vitaclinica.agendamento.repository;

import com.vitaclinica.agendamento.domain.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EspecialidadeRepository extends JpaRepository<Especialidade, Long> {
    List<Especialidade> findByAtivoTrueOrderByNomeAsc();
}
