package br.com.sena.datascale.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// dto/FinancialTransactionResponse.java
public record FinancialTransactionResponse(
        UUID id,
        LocalDate transactionDate,
        String category,
        BigDecimal amount,
        String description,
        LocalDateTime createdAt
) {
}
