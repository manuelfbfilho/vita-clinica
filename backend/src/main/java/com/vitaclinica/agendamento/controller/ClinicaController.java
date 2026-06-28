package com.vitaclinica.agendamento.controller;

import com.vitaclinica.agendamento.domain.Clinica;
import com.vitaclinica.agendamento.exception.*;
import com.vitaclinica.agendamento.repository.ClinicaRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/clinica") @RequiredArgsConstructor
@Tag(name = "Clínica", description = "Dados da clínica")
public class ClinicaController {
    private final ClinicaRepository repo;
    @GetMapping
    public ApiResponse<Clinica> buscar() {
        return ApiResponse.ok(repo.findAll().stream().findFirst()
                .orElseThrow(() -> new NegocioException("CLINICA_NAO_CONFIGURADA",
                        "Clínica não configurada.", HttpStatus.NOT_FOUND)));
    }
    @PutMapping @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Clinica> atualizar(@RequestBody Clinica clinica) {
        return ApiResponse.ok(repo.save(clinica));
    }
}
