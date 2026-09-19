package br.com.sena.datascale.batch;

import br.com.sena.datascale.dto.IngestionProgress;
import br.com.sena.datascale.entities.enums.IngestionStatus;
import br.com.sena.datascale.service.IngestionProgressPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * NOTA: ChunkListener/ChunkContext aqui estão no pacote "classico" do Spring Batch.
 * Como o projeto está no Spring Batch 6.0.5 (que já reorganizou JobExecution, StepExecution
 * e JobExecutionListener, como vimos antes), se o compilador reclamar desse import, é o mesmo
 * tipo de ajuste que já fizemos — me manda o erro exato que eu corrijo o caminho certo.
 */
@Component
@RequiredArgsConstructor
public class IngestionProgressChunkListener implements ChunkListener {

    private final IngestionProgressPublisherService progressPublisher;

    @Override
    public void afterChunk(ChunkContext context) {
        StepExecution stepExecution = context.getStepContext().getStepExecution();
        UUID jobId = UUID.fromString(stepExecution.getJobParameters().getString("jobId"));

        IngestionProgress progress = new IngestionProgress(
                jobId,
                IngestionStatus.PROCESSING,
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount()
        );

        progressPublisher.publish(jobId, progress);
    }
}
