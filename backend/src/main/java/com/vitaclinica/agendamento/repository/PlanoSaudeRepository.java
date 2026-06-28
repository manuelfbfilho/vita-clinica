package com.vitaclinica.agendamento.repository;

import com.vitaclinica.agendamento.domain.PlanoSaude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlanoSaudeRepository extends JpaRepository<PlanoSaude, Long> {
    List<PlanoSaude> findByAtivoTrueOrderByNomeAsc();
    List<PlanoSaude> findByNomeContainingIgnoreCase(String nome);
}
