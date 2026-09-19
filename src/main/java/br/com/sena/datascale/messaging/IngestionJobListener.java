package br.com.sena.datascale.messaging;

import br.com.sena.datascale.dto.IngestionJobMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestionJobListener {

    private final JobLauncher jobLauncher;
    private final Job ingestionJob;

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void handle(IngestionJobMessage message) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobId", message.jobId().toString())
                    .addString("filePath", message.filePath())
                    .toJobParameters();

            jobLauncher.run(ingestionJob, jobParameters);
        } catch (Exception e) {
            log.error("Falha ao iniciar o job de ingestao para jobId={}", message.jobId(), e);
            throw new AmqpRejectAndDontRequeueException("Falha ao iniciar job de ingestao", e);
        }
    }
}
