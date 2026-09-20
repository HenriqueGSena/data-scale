package br.com.sena.datascale.service;

import br.com.sena.datascale.dto.CategoryMonthAggregation;
import br.com.sena.datascale.repository.FinancialTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialTransactionAggregationService {

    private final FinancialTransactionRepository repository;

    public List<CategoryMonthAggregation> aggregateByCategoryAndMonth() {
        return repository.aggregateByCategoryAndMonth().stream()
                .map(p -> new CategoryMonthAggregation(p.getCategory(), p.getYear(), p.getMonth(), p.getTotal()))
                .toList();
    }
}
