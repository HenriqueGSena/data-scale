package br.com.sena.datascale.dto;

import br.com.sena.datascale.entities.enums.IngestionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record IngestionStatusResponse(
        UUID jobId,
        String fileName,
        IngestionStatus status,
        long totalLinesRead,
        long linesProcessed,
        long linesWithError,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        String errorMessage
) {
}
