package br.com.sena.datascale.controller;

import br.com.sena.datascale.dto.CategoryMonthAggregation;
import br.com.sena.datascale.service.FinancialTransactionAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FinancialTransactionAggregationController {

    private final FinancialTransactionAggregationService service;

    @GetMapping("/api/transactions/aggregation/category-month")
    public ResponseEntity<List<CategoryMonthAggregation>> aggregate() {
        return ResponseEntity.ok(service.aggregateByCategoryAndMonth());
    }
}
