package br.com.sena.datascale.service;

import br.com.sena.datascale.dto.IngestionStatusResponse;
import br.com.sena.datascale.entities.IngestionAudit;
import br.com.sena.datascale.exceptions.IngestionNotFoundException;
import br.com.sena.datascale.repository.IngestionAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IngestionStatusService {

    private final IngestionAuditRepository ingestionAuditRepository;

    public IngestionStatusResponse getStatus(UUID jobId) {
        IngestionAudit audit = ingestionAuditRepository.findById(jobId)
                .orElseThrow(() -> new IngestionNotFoundException("jobId nao encontrado: " + jobId));
        return toResponse(audit);
    }

    private IngestionStatusResponse toResponse(IngestionAudit audit) {
        return new IngestionStatusResponse(
                audit.getId(),
                audit.getFileName(),
                audit.getStatus(),
                audit.getTotalLinesRead(),
                audit.getLinesProcessed(),
                audit.getLinesWithError(),
                audit.getStartedAt(),
                audit.getFinishedAt(),
                audit.getErrorMessage()
        );
    }
}
