package com.vitaclinica.agendamento.controller;

import com.vitaclinica.agendamento.dto.request.*;
import com.vitaclinica.agendamento.dto.response.PacienteResponse;
import com.vitaclinica.agendamento.exception.*;
import com.vitaclinica.agendamento.security.UsuarioAutenticado;
import com.vitaclinica.agendamento.service.PacienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
@Tag(name = "Pacientes", description = "Gestão de pacientes")
public class PacienteController {

    private final PacienteService service;

    @PostMapping
    @Operation(summary = "Cadastrar paciente", description = "Público — cria conta de paciente.")
    public ResponseEntity<ApiResponse<PacienteResponse>> cadastrar(
            @Valid @RequestBody CadastrarPacienteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(service.cadastrar(req)));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar pacientes", description = "Requer FUNCIONARIO ou ADMIN.")
    public ResponseEntity<ApiResponse<Page<PacienteResponse>>> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(service.listar(busca, page, size)));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Buscar paciente por ID")
    public ResponseEntity<ApiResponse<PacienteResponse>> buscar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        if (usuario.isPaciente() && !usuario.getUserId().equals(id))
            throw new AcessoNegadoException("Pacientes só podem ver o próprio perfil.");
        return ResponseEntity.ok(ApiResponse.ok(service.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Atualizar paciente")
    public ResponseEntity<ApiResponse<PacienteResponse>> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarPacienteRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        if (usuario.isPaciente() && !usuario.getUserId().equals(id))
            throw new AcessoNegadoException("Pacientes só podem editar o próprio perfil.");
        return ResponseEntity.ok(ApiResponse.ok(service.atualizar(id, req)));
    }

    @PatchMapping("/{id}/ativar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ativar/desativar paciente", description = "Requer ADMIN.")
    public ResponseEntity<ApiResponse<String>> alterarStatus(
            @PathVariable Long id, @RequestParam boolean ativo) {
        service.alterarStatus(id, ativo);
        return ResponseEntity.ok(ApiResponse.ok("Status do paciente atualizado com sucesso."));
    }
}
