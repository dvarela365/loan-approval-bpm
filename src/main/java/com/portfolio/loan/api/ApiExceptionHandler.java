package com.portfolio.loan.api;

import com.portfolio.loan.process.SolicitudInvalidaException;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(SolicitudInvalidaException.class)
    ProblemDetail solicitudInvalida(SolicitudInvalidaException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(422), ex.getMessage());
    }

    @ExceptionHandler(FlowableObjectNotFoundException.class)
    ProblemDetail noEncontrado(FlowableObjectNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
}