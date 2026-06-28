package com.vitaclinica.agendamento.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler centralizado de exceções.
 * Garante respostas JSON consistentes para todos os erros da API.
 *
 * Hierarquia de tratamento:
 *   400 — Validação de campos (@Valid)
 *   401 — Credenciais inválidas
 *   403 — Acesso negado (role insuficiente)
 *   404 — Recurso não encontrado
 *   409 — Conflito (horário indisponível, duplicidade)
 *   422 — Regra de negócio violada
 *   500 — Erro interno não tratado
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ─────────────────────────────────────────────────────────────
    // 400 — Erros de validação de campos (@Valid / @Validated)
    // ─────────────────────────────────────────────────────────────
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> campos = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            String mensagem = error.getDefaultMessage();
            campos.put(campo, mensagem);
        });

        ApiResponse<?> response = ApiResponse.builder()
                .success(false)
                .status(400)
                .erro("DADOS_INVALIDOS")
                .mensagem("Existem erros nos campos informados. Verifique e tente novamente.")
                .campos(campos)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // ─────────────────────────────────────────────────────────────
    // 401 — Credenciais inválidas
    // ─────────────────────────────────────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<?> handleBadCredentials(BadCredentialsException ex) {
        return ApiResponse.erro(401, "CREDENCIAIS_INVALIDAS",
                "CPF ou senha incorretos. Verifique seus dados e tente novamente.");
    }

    // ─────────────────────────────────────────────────────────────
    // 403 — Sem permissão
    // ─────────────────────────────────────────────────────────────
    @ExceptionHandler({AccessDeniedException.class, AcessoNegadoException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<?> handleAccessDenied(RuntimeException ex) {
        return ApiResponse.erro(403, "ACESSO_NEGADO",
                ex.getMessage() != null ? ex.getMessage() : "Você não tem permissão para acessar este recurso.");
    }

    // ─────────────────────────────────────────────────────────────
    // 404 — Recurso não encontrado
    // ─────────────────────────────────────────────────────────────
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<?> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return ApiResponse.erro(404, "RECURSO_NAO_ENCONTRADO", ex.getMessage());
    }

    // ─────────────────────────────────────────────────────────────
    // 409 — Conflito de horário / duplicidade no banco
    // ─────────────────────────────────────────────────────────────
    @ExceptionHandler(HorarioIndisponivelException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<?> handleHorarioIndisponivel(HorarioIndisponivelException ex) {
        return ApiResponse.erro(409, "HORARIO_INDISPONIVEL", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<?> handleDataIntegrity(DataIntegrityViolationException ex) {
        String mensagem = "Operação violou restrição de integridade dos dados.";

        // Mensagens específicas para violações comuns
        String causa = ex.getMostSpecificCause().getMessage();
        if (causa != null) {
            if (causa.contains("uq_profissional_crm") || causa.contains("crm"))
                mensagem = "Já existe um profissional cadastrado com este CRM.";
            else if (causa.contains("uq_paciente_cpf") || causa.contains("uq_funcionario_cpf"))
                mensagem = "Já existe um cadastro com este CPF.";
            else if (causa.contains("uq_paciente_email"))
                mensagem = "Já existe um cadastro com este email.";
            else if (causa.contains("uq_profissional_horario_ativo"))
                mensagem = "Este profissional já possui um agendamento ativo neste horário.";
            else if (causa.contains("uq_paciente_horario_ativo"))
                mensagem = "Este paciente já possui um agendamento ativo neste horário.";
        }

        return ApiResponse.erro(409, "CONFLITO_DADOS", mensagem);
    }

    // ─────────────────────────────────────────────────────────────
    // 422 — Regra de negócio violada
    // ─────────────────────────────────────────────────────────────
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ApiResponse<?>> handleNegocio(NegocioException ex) {
        ApiResponse<?> response = ApiResponse.erro(
                ex.getHttpStatus().value(),
                ex.getCodigo(),
                ex.getMessage()
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    // ─────────────────────────────────────────────────────────────
    // 500 — Erros não esperados (log completo, mensagem genérica)
    // ─────────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<?> handleGeneric(Exception ex) {
        log.error("Erro interno não tratado: {}", ex.getMessage(), ex);
        return ApiResponse.erro(500, "ERRO_INTERNO",
                "Ocorreu um erro interno. Por favor, tente novamente ou entre em contato com o suporte.");
    }
}
