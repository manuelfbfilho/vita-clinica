package com.vitaclinica.agendamento.controller;

import com.vitaclinica.agendamento.dto.response.CepResponse;
import com.vitaclinica.agendamento.exception.ApiResponse;
import com.vitaclinica.agendamento.service.CepService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cep")
@RequiredArgsConstructor
@Tag(name = "CEP", description = "Consulta de endereço por CEP via ViaCEP")
public class CepController {

    private final CepService cepService;

    @GetMapping("/{cep}")
    @Operation(summary = "Buscar endereço por CEP", description = "Público. Consulta a API ViaCEP.")
    public ApiResponse<CepResponse> buscar(@PathVariable String cep) {
        return ApiResponse.ok(cepService.buscarCep(cep));
    }
}
