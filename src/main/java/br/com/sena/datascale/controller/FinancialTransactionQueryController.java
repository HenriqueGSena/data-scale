package br.com.sena.datascale.controller;

import br.com.sena.datascale.dto.CursorPage;
import br.com.sena.datascale.dto.FinancialTransactionResponse;
import br.com.sena.datascale.service.FinancialTransactionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FinancialTransactionQueryController {

    private final FinancialTransactionQueryService financialTransactionQueryService;

    @GetMapping("/api/transactions")
    public ResponseEntity<CursorPage<FinancialTransactionResponse>> list(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(financialTransactionQueryService.listByCursor(cursor, size));
    }
}
