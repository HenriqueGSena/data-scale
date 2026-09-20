package br.com.sena.datascale.batch;


import br.com.sena.datascale.dto.FinancialTransactionCsvRow;
import br.com.sena.datascale.entities.FinancialTransaction;
import br.com.sena.datascale.repository.ProcessingLogRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.UUID;

@Configuration
public class IngestionBatchConfig {

    private static final int CHUNK_SIZE = 5_000;

    @Bean
    @StepScope
    public FlatFileItemReader<FinancialTransactionCsvRow> financialTransactionReader(
            @Value("#{jobParameters['filePath']}") String filePath) {

        return new FlatFileItemReaderBuilder<FinancialTransactionCsvRow>()
                .name("financialTransactionReader")
                .resource(new FileSystemResource(filePath))
                .linesToSkip(1)
                .delimited()
                .names("id", "data", "categoria", "valor", "descricao")
                .targetType(FinancialTransactionCsvRow.class)
                .strict(false)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<FinancialTransaction> financialTransactionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<FinancialTransaction>()
                .dataSource(dataSource)
                .sql("""
                        INSERT INTO financial_transaction (transaction_date, category, amount, description, created_at)
                        VALUES (:transactionDate, :category, :amount, :description, :createdAt)
                        """)
                .beanMapped()
                .build();
    }

    @Bean
    @StepScope
    public IngestionSkipListener ingestionSkipListener(
            ProcessingLogRepository processingLogRepository,
            @Value("#{jobParameters['jobId']}") String jobId) {
        return new IngestionSkipListener(processingLogRepository, UUID.fromString(jobId));
    }

    @Bean
    public Step ingestionStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<FinancialTransactionCsvRow> financialTransactionReader,
            ItemProcessor<FinancialTransactionCsvRow, FinancialTransaction> financialTransactionItemProcessor,
            JdbcBatchItemWriter<FinancialTransaction> financialTransactionWriter,
            IngestionSkipListener ingestionSkipListener,
            IngestionProgressChunkListener ingestionProgressChunkListener) {

        return new StepBuilder("ingestionStep", jobRepository)
                .<FinancialTransactionCsvRow, FinancialTransaction>chunk(CHUNK_SIZE, transactionManager)
                .reader(financialTransactionReader)
                .processor(financialTransactionItemProcessor)
                .writer(financialTransactionWriter)
                .faultTolerant()
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .listener(ingestionSkipListener)
                .listener(ingestionProgressChunkListener)
                .build();
    }

    @Bean
    public Job ingestionJob(
            JobRepository jobRepository,
            Step ingestionStep,
            IngestionJobExecutionListener ingestionJobExecutionListener) {

        return new JobBuilder("ingestionJob", jobRepository)
                .listener(ingestionJobExecutionListener)
                .start(ingestionStep)
                .build();
    }
}
