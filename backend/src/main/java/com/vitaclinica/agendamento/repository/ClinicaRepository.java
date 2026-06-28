package com.vitaclinica.agendamento.repository;

import com.vitaclinica.agendamento.domain.Clinica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicaRepository extends JpaRepository<Clinica, Long> {}
