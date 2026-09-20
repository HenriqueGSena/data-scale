package br.com.sena.datascale.controller;

import br.com.sena.datascale.service.IngestionProgressStreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class IngestionProgressController {

    private final IngestionProgressStreamService ingestionProgressStreamService;

    @GetMapping(value = "/api/ingestion/{jobId}/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(@PathVariable UUID jobId) {
        return ingestionProgressStreamService.subscribe(jobId);
    }
}
