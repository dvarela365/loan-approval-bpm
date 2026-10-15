package com.portfolio.loan.process;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("validarSolicitudDelegate")
public class ValidarSolicitudDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(ValidarSolicitudDelegate.class);
    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("50000000");

    @Override
    public void execute(DelegateExecution execution) {
        BigDecimal monto = (BigDecimal) execution.getVariable("monto");
        if (monto.compareTo(MONTO_MAXIMO) > 0) {
            throw new SolicitudInvalidaException("El monto supera el máximo permitido de " + MONTO_MAXIMO);
        }
        log.info("Solicitud {} validada: {} por {}",
                execution.getVariable("loanId"), execution.getVariable("solicitante"), monto);
    }
}