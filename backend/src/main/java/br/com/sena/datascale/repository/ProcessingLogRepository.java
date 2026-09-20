package br.com.sena.datascale.repository;

import br.com.sena.datascale.entities.ProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcessingLogRepository extends JpaRepository<ProcessingLog, UUID> {

    List<ProcessingLog> findByJobIdOrderByCreatedAtAsc(UUID jobId);
}
