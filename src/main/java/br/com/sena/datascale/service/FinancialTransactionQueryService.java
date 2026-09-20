package br.com.sena.datascale.service;

import br.com.sena.datascale.dto.CursorPage;
import br.com.sena.datascale.dto.FinancialTransactionResponse;
import br.com.sena.datascale.dto.TransactionCursor;
import br.com.sena.datascale.entities.FinancialTransaction;
import br.com.sena.datascale.repository.FinancialTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialTransactionQueryService {

    private final FinancialTransactionRepository repository;

    public CursorPage<FinancialTransactionResponse> listByCursor(String cursor, int size) {
        List<FinancialTransaction> rows = (cursor == null)
                ? repository.findFirstPage(size + 1)
                : decodeAndFetch(cursor, size + 1);

        boolean hasNext = rows.size() > size;
        List<FinancialTransaction> page = hasNext ? rows.subList(0, size) : rows;

        String nextCursor = hasNext
                ? new TransactionCursor(
                page.get(page.size() - 1).getTransactionDate(),
                page.get(page.size() - 1).getId()
        ).encode()
                : null;

        return new CursorPage<>(page.stream().map(this::toResponse).toList(), nextCursor, hasNext);
    }

    private FinancialTransactionResponse toResponse(FinancialTransaction t) {
        return new FinancialTransactionResponse(
                t.getId(), t.getTransactionDate(), t.getCategory(), t.getAmount(), t.getDescription());
    }

    private List<FinancialTransaction> decodeAndFetch(String cursor, int limit) {
        TransactionCursor c = TransactionCursor.decode(cursor);
        return repository.findPageAfterCursor(c.transactionDate(), c.id(), limit);
    }
    // toResponse(...) mapeando para o DTO de saída
}
