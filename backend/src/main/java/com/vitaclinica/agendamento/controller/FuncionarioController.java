package com.vitaclinica.agendamento.controller;

import com.vitaclinica.agendamento.dto.request.CadastrarFuncionarioRequest;
import com.vitaclinica.agendamento.dto.response.FuncionarioResponse;
import com.vitaclinica.agendamento.exception.ApiResponse;
import com.vitaclinica.agendamento.service.FuncionarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/funcionarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Funcionários", description = "Gestão de funcionários — requer ADMIN")
public class FuncionarioController {

    private final FuncionarioService service;

    @PostMapping
    @Operation(summary = "Cadastrar funcionário")
    public ResponseEntity<ApiResponse<FuncionarioResponse>> cadastrar(
            @Valid @RequestBody CadastrarFuncionarioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.cadastrar(req)));
    }

    @GetMapping
    @Operation(summary = "Listar funcionários")
    public ResponseEntity<ApiResponse<List<FuncionarioResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(service.listar()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar funcionário por ID")
    public ResponseEntity<ApiResponse<FuncionarioResponse>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.buscarPorId(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar funcionário")
    public ResponseEntity<ApiResponse<String>> desativar(@PathVariable Long id) {
        service.desativar(id);
        return ResponseEntity.ok(ApiResponse.ok("Funcionário desativado com sucesso."));
    }
}
