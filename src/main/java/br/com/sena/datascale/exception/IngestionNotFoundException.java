package br.com.sena.datascale.exception;

public class IngestionNotFoundException extends RuntimeException {
    public IngestionNotFoundException(String message) {
        super(message);
    }
}
