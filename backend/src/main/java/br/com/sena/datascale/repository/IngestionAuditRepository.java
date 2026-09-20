package br.com.sena.datascale.repository;

import br.com.sena.datascale.entities.IngestionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IngestionAuditRepository extends JpaRepository<IngestionAudit, UUID> {
}
