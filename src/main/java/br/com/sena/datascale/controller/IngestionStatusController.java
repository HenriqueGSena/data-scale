package br.com.sena.datascale.controller;

import br.com.sena.datascale.dto.IngestionStatusResponse;
import br.com.sena.datascale.exceptions.IngestionNotFoundException;
import br.com.sena.datascale.service.IngestionStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class IngestionStatusController {

    private final IngestionStatusService ingestionStatusService;

    @GetMapping("/api/ingestion/{jobId}")
    public ResponseEntity<IngestionStatusResponse> getStatus(@PathVariable UUID jobId) {
        return ResponseEntity.ok(ingestionStatusService.getStatus(jobId));
    }

    @ExceptionHandler(IngestionNotFoundException.class)
    public ResponseEntity<Void> handleNotFound(IngestionNotFoundException e) {
        return ResponseEntity.notFound().build();
    }
}
