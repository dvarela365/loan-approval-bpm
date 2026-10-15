package com.portfolio.loan.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;

    public LoanController(RuntimeService runtimeService, HistoryService historyService) {
        this.runtimeService = runtimeService;
        this.historyService = historyService;
    }

    @PostMapping
    public ResponseEntity<LoanStatus> crear(@Valid @RequestBody LoanRequest request) {
        String loanId = UUID.randomUUID().toString();
        runtimeService.startProcessInstanceByKey("loanApproval", loanId, Map.of(
                "loanId", loanId,
                "solicitante", request.solicitante(),
                "monto", request.monto()));
        return ResponseEntity.created(URI.create("/loans/" + loanId))
                .body(estado(loanId));
    }

    @GetMapping("/{loanId}")
    public LoanStatus estado(@PathVariable String loanId) {
        HistoricProcessInstance instancia = historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(loanId)
                .singleResult();
        if (instancia == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        String estado = instancia.getEndTime() == null ? "EN_REVISION"
                : switch (instancia.getEndActivityId()) {
            case "prestamoAprobado" -> "APROBADO";
            case "prestamoRechazado" -> "RECHAZADO";
            case "prestamoAprobadoAutomatico" -> "APROBADO_AUTOMATICO";
            default -> "FINALIZADO";
        };
        return new LoanStatus(loanId, estado);
    }

    record LoanRequest(@NotBlank String solicitante, @NotNull @Positive BigDecimal monto) {}

    record LoanStatus(String loanId, String estado) {}
}