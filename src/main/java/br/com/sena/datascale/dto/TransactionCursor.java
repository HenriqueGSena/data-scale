package br.com.sena.datascale.dto;

import br.com.sena.datascale.exception.InvalidCursorException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

public record TransactionCursor(LocalDate transactionDate, UUID id) {

    public String encode() {
        String raw = transactionDate + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static TransactionCursor decode(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", 2);
            return new TransactionCursor(LocalDate.parse(parts[0]), UUID.fromString(parts[1]));
        } catch (Exception e) {
            throw new InvalidCursorException("cursor invalido: " + cursor);
        }
    }
}
