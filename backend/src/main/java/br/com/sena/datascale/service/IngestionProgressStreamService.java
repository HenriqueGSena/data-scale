package br.com.sena.datascale.service;

import br.com.sena.datascale.dto.IngestionProgress;
import br.com.sena.datascale.entities.IngestionAudit;
import br.com.sena.datascale.entities.enums.IngestionStatus;
import br.com.sena.datascale.repository.IngestionAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionProgressStreamService {

    private static final long EMITTER_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(30);

    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final StringRedisTemplate redisTemplate;
    private final IngestionProgressPublisherService ingestionProgressPublisherService;
    private final IngestionAuditRepository ingestionAuditRepository;
    private final ObjectMapper objectMapper;


    public SseEmitter subscribe(UUID jobId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        ChannelTopic topic = new ChannelTopic(ingestionProgressPublisherService.channel(jobId));

        MessageListener listener = (message, pattern) -> onMessage(emitter, message);
        redisMessageListenerContainer.addMessageListener(listener, topic);

        Runnable cleanup = () -> redisMessageListenerContainer.removeMessageListener(listener, topic);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        sendInitialState(emitter, jobId);

        return emitter;
    }

    private void onMessage(SseEmitter emitter, Message message) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        sendAndCloseIfTerminal(emitter, payload);
    }

    private void sendInitialState(SseEmitter emitter, UUID jobId) {
        String cached = redisTemplate.opsForValue().get(ingestionProgressPublisherService.progressKey(jobId));
        if (cached != null) {
            sendAndCloseIfTerminal(emitter, cached);
            return;
        }

        ingestionAuditRepository.findById(jobId).ifPresentOrElse(
                audit -> sendAndCloseIfTerminal(emitter, auditToJson(audit)),
                () -> emitter.completeWithError(new IllegalArgumentException("jobId nao encontrado: " + jobId))
        );
    }

    private void sendAndCloseIfTerminal(SseEmitter emitter, String payloadJson) {
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

    private String auditToJson(IngestionAudit audit) {
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
