package br.com.sena.datascale.dto;

import br.com.sena.datascale.entities.enums.IngestionStatus;
import java.util.UUID;

public record UploadResponse(
        UUID jobId,
        IngestionStatus status,
        String message
) {
}
