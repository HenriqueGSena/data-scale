package br.com.sena.datascale.batch;


import br.com.sena.datascale.dto.FinancialTransactionCsvRow;
import br.com.sena.datascale.entities.FinancialTransaction;
import br.com.sena.datascale.exceptions.InvalidCsvRowException;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class FinancialTransactionItemProcessor
        implements ItemProcessor<FinancialTransactionCsvRow, FinancialTransaction> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public FinancialTransaction process(FinancialTransactionCsvRow row) {
        String categoria = validarCategoria(row.getCategoria());
        LocalDate data = validarData(row.getData());
        BigDecimal valor = validarValor(row.getValor());

        return FinancialTransaction.builder()
                .transactionDate(data)
                .category(categoria)
                .amount(valor)
                .description(row.getDescricao())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private String validarCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            throw new InvalidCsvRowException("categoria vazia ou ausente");
        }
        return categoria.trim();
    }

    private LocalDate validarData(String data) {
        try {
            return LocalDate.parse(data, DATE_FORMAT);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new InvalidCsvRowException("data invalida: " + data);
        }
    }

    private BigDecimal validarValor(String valor) {
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidCsvRowException("valor invalido: " + valor);
        }
    }
}
