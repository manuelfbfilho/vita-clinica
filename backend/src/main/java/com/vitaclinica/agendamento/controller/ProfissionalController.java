package com.vitaclinica.agendamento.controller;

import com.vitaclinica.agendamento.dto.request.CadastrarProfissionalRequest;
import com.vitaclinica.agendamento.dto.response.ProfissionalResponse;
import com.vitaclinica.agendamento.exception.ApiResponse;
import com.vitaclinica.agendamento.service.ProfissionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/profissionais")
@RequiredArgsConstructor
@Tag(name = "Profissionais", description = "Médicos e profissionais de saúde")
public class ProfissionalController {

    private final ProfissionalService service;

    @PostMapping
    @Operation(summary = "Cadastrar profissional", description = "Requer ADMIN.")
    public ResponseEntity<ApiResponse<ProfissionalResponse>> cadastrar(
            @Valid @RequestBody CadastrarProfissionalRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.cadastrar(req)));
    }

    @GetMapping
    @Operation(summary = "Listar profissionais", description = "Público.")
    public ApiResponse<List<ProfissionalResponse>> listar(
            @RequestParam(required = false) Long especialidadeId) {
        return ApiResponse.ok(especialidadeId != null
                ? service.listarPorEspecialidade(especialidadeId)
                : service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar profissional por ID")
    public ApiResponse<ProfissionalResponse> buscar(@PathVariable Long id) {
        return ApiResponse.ok(service.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar profissional", description = "Requer ADMIN.")
    public ApiResponse<String> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ApiResponse.ok("Profissional desativado.");
    }
}
