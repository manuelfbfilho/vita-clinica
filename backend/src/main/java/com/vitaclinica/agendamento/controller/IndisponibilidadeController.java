package com.vitaclinica.agendamento.controller;

import com.vitaclinica.agendamento.domain.Indisponibilidade;
import com.vitaclinica.agendamento.dto.request.CriarIndisponibilidadeRequest;
import com.vitaclinica.agendamento.exception.*;
import com.vitaclinica.agendamento.repository.*;
import com.vitaclinica.agendamento.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/indisponibilidades")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Indisponibilidades", description = "Bloqueio de horários — requer FUNCIONARIO")
public class IndisponibilidadeController {

    private final IndisponibilidadeRepository repo;
    private final ProfissionalRepository      profissionalRepo;

    @PostMapping
    @Operation(summary = "Bloquear horário")
    public ResponseEntity<ApiResponse<Indisponibilidade>> criar(
            @Valid @RequestBody CriarIndisponibilidadeRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        Indisponibilidade ind = new Indisponibilidade();
        if (req.getProfissionalId() != null)
            ind.setProfissional(profissionalRepo.findById(req.getProfissionalId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", req.getProfissionalId())));
        ind.setData(req.getData());
        ind.setHoraInicio(req.getHoraInicio());
        ind.setHoraFim(req.getHoraFim());
        ind.setMotivo(req.getMotivo());
        ind.setRegistradoPorTipo(usuario.isAdmin() ? "ADMIN" : "FUNCIONARIO");
        ind.setRegistradoPorId(usuario.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(repo.save(ind)));
    }

    @GetMapping
    @Operation(summary = "Listar bloqueios de horário")
    public ApiResponse<List<Indisponibilidade>> listar() {
        return ApiResponse.ok(repo.findAll());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover bloqueio")
    public ApiResponse<String> remover(@PathVariable Long id) {
        repo.deleteById(id);
        return ApiResponse.ok("Bloqueio removido.");
    }
}
