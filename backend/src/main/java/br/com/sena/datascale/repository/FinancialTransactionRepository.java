package br.com.sena.datascale.repository;

import br.com.sena.datascale.dto.CategoryMonthAggregationProjection;
import br.com.sena.datascale.entities.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

    @Query(value = """
        SELECT * FROM financial_transaction
        WHERE (transaction_date, id) < (:cursorDate, :cursorId)
        ORDER BY transaction_date DESC, id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<FinancialTransaction> findPageAfterCursor(
            @Param("cursorDate") LocalDate cursorDate,
            @Param("cursorId") UUID cursorId,
            @Param("limit") int limit);

    @Query(value = """
        SELECT * FROM financial_transaction
        ORDER BY transaction_date DESC, id DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<FinancialTransaction> findFirstPage(@Param("limit") int limit);

    @Query(value = """
    SELECT category,
           EXTRACT(YEAR FROM transaction_date)::int  AS year,
           EXTRACT(MONTH FROM transaction_date)::int AS month,
           SUM(amount)                                AS total
    FROM financial_transaction
    GROUP BY category, EXTRACT(YEAR FROM transaction_date), EXTRACT(MONTH FROM transaction_date)
    ORDER BY category, year, month
    """, nativeQuery = true)
    List<CategoryMonthAggregationProjection> aggregateByCategoryAndMonth();
}
