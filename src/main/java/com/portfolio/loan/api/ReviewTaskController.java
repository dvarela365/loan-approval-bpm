package com.portfolio.loan.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.flowable.engine.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class ReviewTaskController {

    private final TaskService taskService;

    public ReviewTaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<ReviewTask> pendientes() {
        return taskService.createTaskQuery()
                .taskCandidateGroup("analistas")
                .includeProcessVariables()
                .orderByTaskCreateTime().asc()
                .list().stream()
                .map(t -> new ReviewTask(
                        t.getId(),
                        (String) t.getProcessVariables().get("loanId"),
                        (String) t.getProcessVariables().get("solicitante"),
                        (BigDecimal) t.getProcessVariables().get("monto"),
                        t.getCreateTime()))
                .toList();
    }

    @PostMapping("/{taskId}/decision")
    public ResponseEntity<Void> decidir(@PathVariable String taskId, @Valid @RequestBody Decision decision) {
        taskService.complete(taskId, Map.of("aprobado", decision.aprobado()));
        return ResponseEntity.noContent().build();
    }

    record ReviewTask(String taskId, String loanId, String solicitante, BigDecimal monto, Date creada) {}

    record Decision(@NotNull Boolean aprobado) {}
}