package br.com.sena.datascale.batch;


import br.com.sena.datascale.dto.IngestionProgress;
import br.com.sena.datascale.entities.ProcessingLog;
import br.com.sena.datascale.entities.enums.IngestionStatus;
import br.com.sena.datascale.entities.enums.LogLevel;
import br.com.sena.datascale.repository.IngestionAuditRepository;
import br.com.sena.datascale.repository.ProcessingLogRepository;
import br.com.sena.datascale.service.IngestionProgressPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestionJobExecutionListener implements JobExecutionListener {

    private final IngestionAuditRepository ingestionAuditRepository;
    private final ProcessingLogRepository processingLogRepository;
    private final IngestionProgressPublisherService progressPublisher;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        UUID jobId = extrairJobId(jobExecution);
        ingestionAuditRepository.findById(jobId).ifPresent(audit -> {
            audit.setStatus(IngestionStatus.PROCESSING);
            ingestionAuditRepository.save(audit);
        });
        registrarLog(jobId, LogLevel.INFO, "Job iniciado.");
        progressPublisher.publish(jobId, new IngestionProgress(jobId, IngestionStatus.PROCESSING, 0, 0, 0));
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        UUID jobId = extrairJobId(jobExecution);

        long lidas = 0;
        long gravadas = 0;
        long comErro = 0;
        for (StepExecution step : jobExecution.getStepExecutions()) {
            lidas += step.getReadCount();
            gravadas += step.getWriteCount();
            comErro += step.getSkipCount();
        }

        // Cópias final: reatribuídas no for acima, então precisam ser "effectively final"
        // pra poderem ser capturadas pela lambda do ifPresent logo abaixo.
        final long totalLidas = lidas;
        final long totalGravadas = gravadas;
        final long totalComErro = comErro;
        final boolean sucesso = jobExecution.getStatus() == BatchStatus.COMPLETED;
        final IngestionStatus statusFinal = sucesso ? IngestionStatus.COMPLETED : IngestionStatus.FAILED;

        ingestionAuditRepository.findById(jobId).ifPresent(audit -> {
            audit.setStatus(statusFinal);
            audit.setTotalLinesRead(totalLidas);
            audit.setLinesProcessed(totalGravadas);
            audit.setLinesWithError(totalComErro);
            audit.setFinishedAt(LocalDateTime.now());
            if (!sucesso && !jobExecution.getAllFailureExceptions().isEmpty()) {
                audit.setErrorMessage(jobExecution.getAllFailureExceptions().get(0).getMessage());
            }
            ingestionAuditRepository.save(audit);
        });

        registrarLog(jobId, sucesso ? LogLevel.INFO : LogLevel.ERROR,
                "Job finalizado com status %s. Lidas=%d, gravadas=%d, com erro=%d."
                        .formatted(jobExecution.getStatus(), totalLidas, totalGravadas, totalComErro));

        // Evento terminal: garante que quem está ouvindo o SSE recebe o status final
        // e fecha a conexão, mesmo que o job tenha sido rápido demais pro chunk listener disparar.
        progressPublisher.publish(jobId, new IngestionProgress(jobId, statusFinal, totalLidas, totalGravadas, totalComErro));

        log.info("Job {} finalizado: status={}, lidas={}, gravadas={}, comErro={}",
                jobId, jobExecution.getStatus(), totalLidas, totalGravadas, totalComErro);
    }

    private UUID extrairJobId(JobExecution jobExecution) {
        return UUID.fromString(jobExecution.getJobParameters().getString("jobId"));
    }

    private void registrarLog(UUID jobId, LogLevel level, String mensagem) {
        processingLogRepository.save(ProcessingLog.builder()
                .jobId(jobId)
                .level(level)
                .message(mensagem)
                .build());
    }
}
