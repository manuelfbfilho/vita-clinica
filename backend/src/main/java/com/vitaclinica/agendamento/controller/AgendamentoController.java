package com.vitaclinica.agendamento.controller;

import com.vitaclinica.agendamento.domain.enums.StatusAgendamento;
import com.vitaclinica.agendamento.dto.request.*;
import com.vitaclinica.agendamento.dto.response.*;
import com.vitaclinica.agendamento.exception.ApiResponse;
import com.vitaclinica.agendamento.security.UsuarioAutenticado;
import com.vitaclinica.agendamento.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
@Tag(name = "Agendamentos", description = "Gestão de consultas")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final HorarioService     horarioService;

    @GetMapping("/horarios-disponiveis")
    @Operation(summary = "Horários disponíveis", description = "Público. Retorna slots do dia para um profissional.")
    public ApiResponse<List<SlotHorarioResponse>> horariosDisponiveis(
            @RequestParam Long profissionalId,
            @RequestParam LocalDate data) {
        return ApiResponse.ok(horarioService.calcularDisponibilidade(profissionalId, data));
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Criar agendamento")
    public ResponseEntity<ApiResponse<AgendamentoResponse>> criar(
            @Valid @RequestBody CriarAgendamentoRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(agendamentoService.criar(req, usuario)));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar agendamentos", description = "Requer FUNCIONARIO ou ADMIN. Suporta filtros.")
    public ApiResponse<Page<AgendamentoResponse>> listar(
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) Long profissionalId,
            @RequestParam(required = false) StatusAgendamento status,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dataConsulta,asc") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort.split(",")));
        return ApiResponse.ok(agendamentoService.listar(pacienteId, profissionalId, status, dataInicio, dataFim, pageable));
    }

    @GetMapping("/meus")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Meus agendamentos", description = "Agendamentos do paciente logado.")
    public ApiResponse<Page<AgendamentoResponse>> meusAgendamentos(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.ok(agendamentoService.meusAgendamentos(usuario.getUserId(), pageable));
    }

    @GetMapping("/agenda-dia")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Agenda do dia", description = "Todos os agendamentos de uma data.")
    public ApiResponse<List<AgendamentoResponse>> agendaDia(
            @RequestParam(required = false) LocalDate data) {
        return ApiResponse.ok(agendamentoService.agendaDia(data != null ? data : LocalDate.now()));
    }

    @GetMapping("/proximos")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Próximos agendamentos", description = "Agendamentos futuros para dashboard de funcionário/admin.")
    public ApiResponse<List<AgendamentoResponse>> proximos() {
        return ApiResponse.ok(agendamentoService.proximosAgendamentos());
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Buscar agendamento por ID")
    public ApiResponse<AgendamentoResponse> buscar(@PathVariable Long id) {
        return ApiResponse.ok(agendamentoService.buscarPorId(id));
    }

    @PatchMapping("/{id}/cancelar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancelar agendamento", description = "Motivo obrigatório (mínimo 10 caracteres).")
    public ApiResponse<AgendamentoResponse> cancelar(
            @PathVariable Long id,
            @Valid @RequestBody CancelarAgendamentoRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ApiResponse.ok(agendamentoService.cancelar(id, req, usuario));
    }

    @PatchMapping("/{id}/confirmar")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Confirmar agendamento", description = "Requer FUNCIONARIO.")
    public ApiResponse<AgendamentoResponse> confirmar(@PathVariable Long id) {
        return ApiResponse.ok(agendamentoService.confirmar(id));
    }

    @PatchMapping("/{id}/concluir")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Concluir agendamento", description = "Requer FUNCIONARIO.")
    public ApiResponse<AgendamentoResponse> concluir(@PathVariable Long id) {
        return ApiResponse.ok(agendamentoService.concluir(id));
    }
}
