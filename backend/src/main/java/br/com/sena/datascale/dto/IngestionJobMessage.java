package br.com.sena.datascale.dto;

import java.io.Serializable;
import java.util.UUID;

public record IngestionJobMessage(
        UUID jobId,
        String filePath,
        String originalFileName
) implements Serializable {
}
