package br.com.sena.datascale.controller;

import br.com.sena.datascale.dto.IngestionProgress;
import br.com.sena.datascale.entities.IngestionAudit;
import br.com.sena.datascale.entities.enums.IngestionStatus;
import br.com.sena.datascale.repository.IngestionAuditRepository;
import br.com.sena.datascale.service.IngestionProgressPublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
public class IngestionProgressController {

    private static final long EMITTER_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(30);

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final StringRedisTemplate redisTemplate;
    private final IngestionProgressPublisherService progressPublisher;
    private final IngestionAuditRepository ingestionAuditRepository;
    private final ObjectMapper objectMapper;

    @GetMapping(value = "/api/ingestion/{jobId}/progress", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamProgress(@PathVariable UUID jobId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        ChannelTopic topic = new ChannelTopic(progressPublisher.channel(jobId));

        MessageListener listener = (message, pattern) -> handleMessage(emitter, message);
        redisMessageListenerContainer.addMessageListener(listener, topic);

        Runnable cleanup = () -> redisMessageListenerContainer.removeMessageListener(listener, topic);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        enviarEstadoInicial(emitter, jobId);

        return emitter;
    }

    private void handleMessage(SseEmitter emitter, Message message) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        enviarEFecharSeTerminal(emitter, payload);
    }

    /**
     * Ao conectar, manda o último estado conhecido imediatamente (não espera o próximo chunk):
     * primeiro tenta a chave no Redis (job em andamento ou já finalizado recentemente);
     * se não achar (job nunca chegou a publicar, ou a chave já expirou), cai pro snapshot
     * da tabela ingestion_audit.
     */
    private void enviarEstadoInicial(SseEmitter emitter, UUID jobId) {
        String cached = redisTemplate.opsForValue().get(progressPublisher.progressKey(jobId));
        if (cached != null) {
            enviarEFecharSeTerminal(emitter, cached);
            return;
        }

        ingestionAuditRepository.findById(jobId).ifPresentOrElse(
                audit -> enviarEFecharSeTerminal(emitter, auditParaJson(audit)),
                () -> emitter.completeWithError(new IllegalArgumentException("jobId nao encontrado: " + jobId))
        );
    }

    private void enviarEFecharSeTerminal(SseEmitter emitter, String payloadJson) {
        try {
            emitter.send(SseEmitter.event().name("progress").data(payloadJson));
            IngestionProgress progress = objectMapper.readValue(payloadJson, IngestionProgress.class);
            if (isTerminal(progress.status())) {
                emitter.complete();
            }
        } catch (IOException e) {
            log.warn("Falha ao enviar evento SSE, encerrando conexao", e);
            emitter.completeWithError(e);
        }
    }

    private String auditParaJson(IngestionAudit audit) {
        IngestionProgress progress = new IngestionProgress(
                audit.getId(),
                audit.getStatus(),
                audit.getTotalLinesRead(),
                audit.getLinesProcessed(),
                audit.getLinesWithError()
        );
        return objectMapper.writeValueAsString(progress);
    }

    private boolean isTerminal(IngestionStatus status) {
        return status == IngestionStatus.COMPLETED || status == IngestionStatus.FAILED;
    }
}
