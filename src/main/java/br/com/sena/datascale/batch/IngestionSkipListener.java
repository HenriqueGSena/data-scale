package br.com.sena.datascale.batch;


import br.com.sena.datascale.dto.FinancialTransactionCsvRow;
import br.com.sena.datascale.entities.FinancialTransaction;
import br.com.sena.datascale.entities.ProcessingLog;
import br.com.sena.datascale.entities.enums.LogLevel;
import br.com.sena.datascale.repository.ProcessingLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class IngestionSkipListener implements SkipListener<FinancialTransactionCsvRow, FinancialTransaction> {

    private final ProcessingLogRepository processingLogRepository;
    private final UUID jobId;

    @Override
    public void onSkipInRead(Throwable t) {
        registrar(null, t);
    }

    @Override
    public void onSkipInProcess(FinancialTransactionCsvRow item, Throwable t) {
        registrar(item, t);
    }

    @Override
    public void onSkipInWrite(FinancialTransaction item, Throwable t) {
        log.error("Skip na escrita, jobId={}: {}", jobId, t.getMessage());
        salvar("Falha ao gravar no banco: " + t.getMessage());
    }

    private void registrar(FinancialTransactionCsvRow item, Throwable t) {
        String mensagem = item != null
                ? "Linha invalida (id=%s): %s".formatted(item.getId(), t.getMessage())
                : "Falha na leitura da linha: " + t.getMessage();
        log.warn("Skip no processamento, jobId={}: {}", jobId, mensagem);
        salvar(mensagem);
    }

    private void salvar(String mensagem) {
        processingLogRepository.save(ProcessingLog.builder()
                .jobId(jobId)
                .level(LogLevel.WARN)
                .message(mensagem)
                .build());
    }
}
