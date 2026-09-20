package br.com.sena.datascale.service;


import br.com.sena.datascale.dto.IngestionJobMessage;
import br.com.sena.datascale.dto.UploadResponse;
import br.com.sena.datascale.entities.IngestionAudit;
import br.com.sena.datascale.entities.enums.IngestionStatus;
import br.com.sena.datascale.exception.InvalidUploadException;
import br.com.sena.datascale.repository.IngestionAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final IngestionAuditRepository ingestionAuditRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    public UploadResponse processUpload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidUploadException("Arquivo vazio.");
        }

        IngestionAudit audit = createAuditRecord(file);
        Path destination = storeFile(file, audit);
        publishJob(audit, destination, file.getOriginalFilename());

        log.info("Upload recebido: jobId={}, arquivo={}", audit.getId(), file.getOriginalFilename());

        return new UploadResponse(audit.getId(), audit.getStatus(), "Arquivo recebido, processamento iniciado.");
    }

    private IngestionAudit createAuditRecord(MultipartFile file) {
        IngestionAudit audit = IngestionAudit.builder()
                .fileName(file.getOriginalFilename())
                .status(IngestionStatus.RECEIVED)
                .build();
        return ingestionAuditRepository.save(audit);
    }

    private Path storeFile(MultipartFile file, IngestionAudit audit) {
        Path destination = Path.of(uploadDir, audit.getId() + "-" + file.getOriginalFilename());
        try {
            Files.createDirectories(destination.getParent());
            file.transferTo(destination);
            return destination;
        } catch (IOException e) {
            audit.setStatus(IngestionStatus.FAILED);
            audit.setErrorMessage("Falha ao salvar o arquivo: " + e.getMessage());
            ingestionAuditRepository.save(audit);
            throw new UncheckedIOException("Falha ao salvar o arquivo de upload", e);
        }
    }

    private void publishJob(IngestionAudit audit, Path destination, String originalFileName) {
        IngestionJobMessage message = new IngestionJobMessage(audit.getId(), destination.toString(), originalFileName);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
    }
}
