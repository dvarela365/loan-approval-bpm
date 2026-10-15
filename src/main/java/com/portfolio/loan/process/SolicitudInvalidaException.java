package com.portfolio.loan.process;

public class SolicitudInvalidaException extends RuntimeException {
    public SolicitudInvalidaException(String message) {
        super(message);
    }
}