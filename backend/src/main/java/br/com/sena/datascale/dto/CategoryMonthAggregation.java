package br.com.sena.datascale.dto;

import java.math.BigDecimal;

public record CategoryMonthAggregation(
        String category,
        int year,
        int month,
        BigDecimal total
) {
}
