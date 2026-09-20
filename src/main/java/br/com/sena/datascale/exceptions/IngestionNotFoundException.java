package br.com.sena.datascale.exceptions;

public class IngestionNotFoundException extends RuntimeException {
    public IngestionNotFoundException(String message) {
        super(message);
    }
}
