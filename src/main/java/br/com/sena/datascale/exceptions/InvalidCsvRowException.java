package br.com.sena.datascale.exceptions;

public class InvalidCsvRowException extends RuntimeException {
    public InvalidCsvRowException(String message) {
        super(message);
    }
}
