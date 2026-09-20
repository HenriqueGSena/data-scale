package br.com.sena.datascale.dto;

import br.com.sena.datascale.entities.enums.IngestionStatus;

import java.util.UUID;

public record IngestionProgress(
        UUID jobId,
        IngestionStatus status,
        long linesRead,
        long linesProcessed,
        long linesWithError
) {
}
