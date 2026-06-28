package com.vitaclinica.agendamento.controller;

import com.vitaclinica.agendamento.dto.request.LoginRequest;
import com.vitaclinica.agendamento.dto.response.TokenResponse;
import com.vitaclinica.agendamento.exception.ApiResponse;
import com.vitaclinica.agendamento.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login e gestão de acesso")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica paciente ou funcionário pelo CPF + senha. Retorna JWT Bearer token.")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(req)));
    }

    @GetMapping("/status")
    @Operation(summary = "Status do servidor", description = "Verifica se a API está online")
    public ResponseEntity<ApiResponse<String>> status() {
        return ResponseEntity.ok(ApiResponse.ok("Vita Clínica API v1.0 — online"));
    }
}
