package com.portfolio.loan;

import com.portfolio.loan.process.SolicitudInvalidaException;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestcontainersConfig.class)
class LoanApprovalProcessTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired RuntimeService runtimeService;
    @Autowired TaskService taskService;
    @Autowired HistoryService historyService;

    @Test
    void montoChicoSeApruebaAutomaticamente() {
        ProcessInstance instancia = iniciar("300000");

        assertThat(instancia.isEnded()).isTrue();
        assertThat(finDe(instancia)).isEqualTo("prestamoAprobadoAutomatico");
    }

    @Test
    void montoGrandeVaAlAnalistaYSeAprueba() {
        ProcessInstance instancia = iniciar("2000000");

        Task tarea = tareaDe(instancia);
        assertThat(tarea.getTaskDefinitionKey()).isEqualTo("revisionAnalista");

        taskService.complete(tarea.getId(), Map.of("aprobado", true));

        assertThat(finDe(instancia)).isEqualTo("prestamoAprobado");
    }

    @Test
    void montoGrandeRechazadoPorElAnalista() {
        ProcessInstance instancia = iniciar("2000000");

        taskService.complete(tareaDe(instancia).getId(), Map.of("aprobado", false));

        assertThat(finDe(instancia)).isEqualTo("prestamoRechazado");
    }

    @Test
    void montoExcesivoNoDejaInstanciaCreada() {
        String loanId = UUID.randomUUID().toString();

        assertThatThrownBy(() -> iniciar(loanId, "99000000"))
                .isInstanceOf(SolicitudInvalidaException.class);

        assertThat(historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(loanId).count()).isZero();
    }

    private ProcessInstance iniciar(String monto) {
        return iniciar(UUID.randomUUID().toString(), monto);
    }

    private ProcessInstance iniciar(String loanId, String monto) {
        return runtimeService.startProcessInstanceByKey("loanApproval", loanId, Map.of(
                "loanId", loanId,
                "solicitante", "Test",
                "monto", new BigDecimal(monto)));
    }

    private Task tareaDe(ProcessInstance instancia) {
        return taskService.createTaskQuery().processInstanceId(instancia.getId()).singleResult();
    }

    private String finDe(ProcessInstance instancia) {
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instancia.getId()).singleResult().getEndActivityId();
    }
}