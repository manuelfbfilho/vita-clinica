package com.vitaclinica.agendamento.controller;

import com.vitaclinica.agendamento.domain.PlanoSaude;
import com.vitaclinica.agendamento.exception.ApiResponse;
import com.vitaclinica.agendamento.repository.PlanoSaudeRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/planos-saude") @RequiredArgsConstructor
@Tag(name = "Planos de Saúde", description = "Planos conveniados com a clínica")
public class PlanoSaudeController {
    private final PlanoSaudeRepository repo;
    @GetMapping
    public ApiResponse<List<PlanoSaude>> listar() {
        return ApiResponse.ok(repo.findByAtivoTrueOrderByNomeAsc());
    }
}
