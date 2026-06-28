package com.vitaclinica.agendamento.controller;

import com.vitaclinica.agendamento.domain.Especialidade;
import com.vitaclinica.agendamento.exception.ApiResponse;
import com.vitaclinica.agendamento.repository.EspecialidadeRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/especialidades") @RequiredArgsConstructor
@Tag(name = "Especialidades", description = "Especialidades médicas")
public class EspecialidadeController {
    private final EspecialidadeRepository repo;
    @GetMapping
    public ApiResponse<List<Especialidade>> listar() {
        return ApiResponse.ok(repo.findByAtivoTrueOrderByNomeAsc());
    }
}
