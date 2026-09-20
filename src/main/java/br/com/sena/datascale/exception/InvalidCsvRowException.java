package br.com.sena.datascale.exception;

public class InvalidCsvRowException extends RuntimeException {
    public InvalidCsvRowException(String message) {
        super(message);
    }
}
