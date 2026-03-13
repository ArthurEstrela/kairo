package com.skill.kairo.infrastructure.config;

import com.skill.kairo.domain.exception.InvalidChallengeConfigException;
import com.skill.kairo.domain.exception.OutOfLivesException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 0. Erros de validação de @Valid nos @RequestBody (campo a campo)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Dados de Entrada Inválidos");
        problemDetail.setProperty("timestamp", Instant.now());

        List<Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "message", fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Valor inválido"
                ))
                .toList();
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    // 1. Apanha a nossa regra de negócio de "Sem Vidas"
    @ExceptionHandler(OutOfLivesException.class)
    public ProblemDetail handleOutOfLivesException(OutOfLivesException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Vidas Esgotadas");
        problemDetail.setType(URI.create("https://api.kairo.com/errors/out-of-lives"));
        problemDetail.setProperty("timestamp", Instant.now());
        // Aqui o frontend Next.js sabe que deve mostrar o pop-up de "Comprar Premium"
        problemDetail.setProperty("actionRequired", "UPGRADE_PLAN_OR_WAIT"); 
        return problemDetail;
    }

    // 2. Apanha erros de configuração de Desafios (Domain Exception)
    @ExceptionHandler(InvalidChallengeConfigException.class)
    public ProblemDetail handleInvalidChallengeConfigException(InvalidChallengeConfigException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setTitle("Configuração de Desafio Inválida");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    // 3. Apanha as nossas validações gerais (ex: strings vazias, valores negativos nos Value Objects)
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Dados Inválidos");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    // 4. Fallback de Segurança (Se algo inesperado rebentar, não expomos o código ao utilizador)
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        // Num cenário real, enviaríamos o 'ex' para o Sentry ou Datadog aqui
        ex.printStackTrace(); // Apenas para debug local
        
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno no servidor.");
        problemDetail.setTitle("Erro Interno");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}