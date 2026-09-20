package br.com.sena.datascale.dto;

import java.math.BigDecimal;

public interface CategoryMonthAggregationProjection {
    String getCategory();
    Integer getYear();
    Integer getMonth();
    BigDecimal getTotal();
}
