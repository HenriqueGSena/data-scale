package br.com.sena.datascale.exception;

public class InvalidUploadException extends RuntimeException {

    public InvalidUploadException(String message) {
        super(message);
    }
}
