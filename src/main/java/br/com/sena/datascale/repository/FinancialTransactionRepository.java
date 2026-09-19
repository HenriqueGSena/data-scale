package br.com.sena.datascale.repository;

import br.com.sena.datascale.entities.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {
}
